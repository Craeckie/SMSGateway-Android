package de.sanemind.smsgateway;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.macasaet.fernet.Key;
import com.macasaet.fernet.StringValidator;
import com.macasaet.fernet.Token;

import org.json.JSONArray;
import org.json.JSONException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.Buttons;
import de.sanemind.smsgateway.model.ChatList;
import de.sanemind.smsgateway.model.GroupMessage;
import de.sanemind.smsgateway.model.MessageStatus;
import de.sanemind.smsgateway.model.UserMessage;

public class GatewayUtils {
    private static Pattern namePattern = Pattern.compile("^(.+) \\(\\+?([0-9]+)\\)$");
    private static Pattern oldNumberPattern = Pattern.compile("^(From|To): (.*) \\(([0-9+]+)\\)$");
    private static Pattern oldFromGroupPattern = Pattern.compile("^From: (.*) \\(([0-9+]+)\\)@(.*)$");

    private static Pattern idPattern = Pattern.compile("^ID: ([0-9]+)$");
    private static Pattern datePattern = Pattern.compile("^Date: ([0-9]+)$");
    private static Pattern headerPattern = Pattern.compile("^([A-Za-z]+): (.*)$");

    private static class UTF16Validator implements StringValidator {
        public Charset getCharset() {
            return Charset.forName("UTF-16BE");
        }
    }
    private static class UTF8Validator implements StringValidator {
        public Charset getCharset() {
            return Charset.forName("UTF-8");
        }
    }

    private static Key key = null;
    private static final StringValidator utf8Validator = new UTF8Validator();
    private static final StringValidator utf16Validator = new UTF16Validator();
    private static String decryptBody(Context context, String body) {
        boolean isUTF8 = false;
        try {
            if (key == null) {
                String keyString = PreferenceManager.getDefaultSharedPreferences(context).getString("edit_text_preference_sms_key", null);
                if (keyString == null)
                    return body;
                key = new Key(keyString);
            }
            if (body.startsWith("%8%")) {
                body = body.substring("%8%".length());
                isUTF8 = true;
            }

            Token token = Token.fromString(body);
            if (isUTF8)
                body = token.validateAndDecrypt(key, utf8Validator);
            else
                body = token.validateAndDecrypt(key, utf16Validator);

        } catch (Exception e) {
            Log.w("GW-decrypt", e);
            Log.d("GW-decrypt", body);
            String debug = "Exception when decrypting: " + e + "\n";
            if (isUTF8)
                debug += "(is UTF8)\n";
            body = debug + body;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            String debug = "Throwable when decrypting: " + throwable.getMessage() + "\n";
            if (isUTF8)
                debug += "(is UTF8)\n";
            body = debug + body;
        }
        return body;
    }

    public static BaseMessage tryParseGatewayMessage(Context context, String body, Date receivedDate, boolean isSent, String phoneNumber) {
        if (body.startsWith("%8%")
                || body.startsWith("gAAA")) //TODO: temporarily
            body = decryptBody(context, body);
        BaseMessage message = null;

        String[] lines = body.split("\n");
        if (lines.length > 2) {
            String identifier = lines[0].trim();
            String from = null;
            String to = null;
            String type = null;
//            String group = null;
//            String channel = null;
            String phone = null;
            String messageBody = "";
            MessageStatus status = MessageStatus.FORWARDED;
            Buttons buttons = null;
//            boolean isEdit = false;
            Date date = receivedDate;
            isSent = true;
            int ID = -1;
            Map<String, String> otherHeaders = new HashMap<>();

            ChatList chatList = Messengers.listForIdentifier(context, identifier);
            if (chatList == null) {
                chatList = Messengers.getSMS(context);
                identifier = "SMS";
            } else if (identifier.equalsIgnoreCase("EM")) {
                type = "group";
            }
            boolean messageStarted = false;
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (messageStarted) {
                    if (!messageBody.isEmpty())
                        messageBody += "\n";
                    messageBody += line;
                } else if (line.isEmpty()) {
                    messageStarted = true;
                    messageBody = "";
                } else if (line.startsWith("Type: ")) {
                    type = line.substring("Type: ".length()).toLowerCase();
                } else if (line.startsWith("From: ")) {
                    from = line.substring("From: ".length());
                    isSent = false;
                    // TODO: temporarily support old format for messages sent to groups
                    if (phone == null && type == null) {
                        Matcher matcherGroup = oldFromGroupPattern.matcher(line);
                        Matcher matcherNumber = oldNumberPattern.matcher(line);
                        if (matcherGroup.matches()) {
                            from = matcherGroup.group(1);
                            phone = matcherGroup.group(2);
                            to = matcherGroup.group(3);
                            type = "group";
                        } else if (matcherNumber.matches()) {
                            from = matcherNumber.group(2);
                            phone = matcherNumber.group(3);
                            type = "user";
                        }
                    }
                } else if (line.startsWith("To: ")) {
                    to = line.substring("To: ".length());
                    // TODO: temporarily support old format for messages sent to groups
//                    Matcher matcherNumber = oldNumberPattern.matcher(line);
//                    if (phone == null && matcherNumber.matches()) {
//                        to = matcherNumber.group(2);
//                        phone = matcherNumber.group(3);
//                        if (ChatList.find_group(context, to, false) != null)
//                            type = "group";
//                    }
                } else if (line.startsWith("Group: ")) {
                    to = line.substring("Group: ".length());
                    type = "group";
                } else if (line.startsWith("Channel: ")) {
                    to = line.substring("Channel: ".length());
                    type = "channel";
                } else if (line.startsWith("Phone: ")) {
                    phone = line.substring("Phone: ".length());
                } else if (line.startsWith("Edit: ")) {
                    if (line.substring("Edit: ".length()).equalsIgnoreCase("true")) {
                        status = MessageStatus.EDITED;
                    }
                } else if (line.startsWith("Status: ")) {
                    String statusString = line.substring("Status: ".length());
                    if (statusString.equalsIgnoreCase("processed")) {
                        // Ignore the messages just indicating that this message was about to be sent to TG
                        return new UserMessage(Long.MIN_VALUE, new Date(0), "", "", null, false, MessageStatus.FORWARDED);
                    } else if (statusString.equalsIgnoreCase("deleted")) {
                        status = MessageStatus.DELETED;
                    } else if (statusString.equalsIgnoreCase("edited")) {
                        status = MessageStatus.EDITED;
                    }
                } else if (line.startsWith("Date: ")) {
                    Matcher dateMatch = datePattern.matcher(line);
                    if (dateMatch.matches())
                        date = new java.util.Date(Long.parseLong(dateMatch.group(1)) * 1000L);
                } else if (line.startsWith("Buttons: ")) {
                    String buttonsStr = line.substring("Buttons: ".length());
                    buttons = new Buttons();
                    try {
                        JSONArray arr = new JSONArray(buttonsStr);
                        for (int j = 0; j < arr.length(); j++) {
                            JSONArray data_row = arr.getJSONArray(j);
                            ArrayList<String> items = new ArrayList<>();
                            for (int k = 0; k < data_row.length(); k++) {
                                items.add(data_row.getString(k));
                            }
                            buttons.addRow(items);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    Log.v("GU", buttons.toString());

                } else if (idPattern.matcher(line).matches()) { // For performance the last check
                    try {
                        ID = Integer.parseInt(line.substring("ID: ".length()));
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Invalid " + line, Toast.LENGTH_LONG);
                    }
                } else {
                    Matcher matcher = headerPattern.matcher(line);
                    if (matcher.matches()) {
                        otherHeaders.put(matcher.group(1), matcher.group(2));
                    }
                }
//                } else { // Unknown header, assume that message starts for now..
//                    messageStarted = true;
//                    messageBody = line;
//                }
            }

            if (!messageBody.isEmpty() && !messageBody.equals("\n") && (from != null || to != null)) {
                String user = isSent ? to : from;
                if (type != null && !type.equals("user")) {
                    if (type.equals("channel") && user != null) { // Someone sent to a channel
                        message = new GroupMessage(
                                ID,
                                date,
                                messageBody,
                                identifier,
                                chatList.get_or_create_group(context, user, user, true),
                                null,
                                isSent,
                                status,
                                otherHeaders);
                    } else if (type.equals("group")) {
                        if (isSent && to != null) { // I wrote to a group
                            message = new GroupMessage(
                                    ID,
                                    date,
                                    messageBody,
                                    identifier,
                                    chatList.get_or_create_group(context, to, to, false),
                                    chatList.get_meUser(context), // TODO: correct?
                                    isSent,
                                    status,
                                    otherHeaders);
                        } else if (!isSent && to != null && from != null) { // Somebody sent to a group
                            message = new GroupMessage(
                                    ID,
                                    date,
                                    messageBody,
                                    identifier,
                                    chatList.get_or_create_group(context, to, to, false),
                                    chatList.get_or_create_user(context, from, from, phone),
                                    isSent,
                                    status,
                                    otherHeaders);
                        } else {
                            Toast.makeText(context, "Invalid group message:\n" + body, Toast.LENGTH_LONG);
                        }
                    } else {
                        Toast.makeText(context, "Unknown type: " + type, Toast.LENGTH_LONG);
                    }
                } else { // normal user message
                    if (isSent && to != null) { // I wrote to a user
                        message = new UserMessage(
                                ID,
                                date,
                                messageBody,
                                identifier,
                                chatList.get_or_create_user(context, to, to, phone),
                                isSent,
                                status,
                                otherHeaders);
                    } else if (!isSent && from != null){ // Somebody sent me a message
                        message = new UserMessage(
                                ID,
                                date,
                                messageBody,
                                identifier,
                                chatList.get_or_create_user(context, from, from, phone),
                                isSent,
                                status,
                                otherHeaders);
                    } else {
                        Toast.makeText(context,"Invalid message:\n" + body, Toast.LENGTH_LONG);
                    }
                }
                if (message != null && buttons != null) {
                    message.setButtons(buttons);
                }
            }
        }
        if (message == null) {
            message = new UserMessage(
                    -1,
                    receivedDate,
                    body,
                    "SMS",
                    Messengers.getSMS(context).get_or_create_user(context, phoneNumber, phoneNumber, phoneNumber),
                    isSent,
                    MessageStatus.SENT);
        }
        return message;
    }
}


