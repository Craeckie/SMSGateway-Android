package de.sanemind.smsgateway;

import android.content.Context;
import android.widget.Toast;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.GroupMessage;
import de.sanemind.smsgateway.model.UserMessage;

public class GatewayUtils {
    private static Pattern namePattern = Pattern.compile("^(.+) \\(\\+?([0-9]+)\\)$");
    private static Pattern oldNumberPattern = Pattern.compile("^(From|To): (.*) \\(([0-9+]+)\\)$");
    private static Pattern oldFromGroupPattern = Pattern.compile("^From: (.*) \\(([0-9+]+)\\)@(.*)$");

    public static BaseMessage tryParseGatewayMessage(Context context, String body, Date date, boolean isSent) {
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
            boolean isEdit = false;
            isSent = true;
            int ID = -1;
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
                } else if (line.matches("^ID: ([0-9]+)$")) {
                    try {
                        ID = Integer.parseInt(line.substring("ID: ".length()));
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Invalid " + line, Toast.LENGTH_LONG);
                    }
                } else if (line.matches("^Type: (.+)$")) {
                    type = line.substring("Type: ".length()).toLowerCase();
                } else if (line.matches("^From: (.+)$")) {
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
                } else if (line.matches("^To: (.+)$")) {
                    to = line.substring("To: ".length());
                    // TODO: temporarily support old format for messages sent to groups
//                    Matcher matcherNumber = oldNumberPattern.matcher(line);
//                    if (phone == null && matcherNumber.matches()) {
//                        to = matcherNumber.group(2);
//                        phone = matcherNumber.group(3);
//                        if (ChatList.find_group(context, to, false) != null)
//                            type = "group";
//                    }
                } else if (line.matches("^Group: (.+)$")) {
                    to = line.substring("Group: ".length());
                    type = "group";
                } else if (line.matches("^Channel: (.+)$")) {
                    to = line.substring("Channel: ".length());
                    type = "channel";
                } else if (line.matches("^Phone: (.+)$")) {
                    phone = line.substring("Phone: ".length());
                } else if (line.matches("^Edit: (.+)$")) {
                    if (line.substring("Edit: ".length()).equalsIgnoreCase("true"))
                        isEdit = true;
                }
//                } else { // Unknown header, assume that message starts for now..
//                    messageStarted = true;
//                    messageBody = line;
//                }
            }

            if (from != null || to != null) {
                String user = isSent ? to : from;
                if (type != null && !type.equals("user")) {
                    if (type.equals("channel") && user != null) { // Someone sent to a channel
                        message = new GroupMessage(
                                ID,
                                date,
                                messageBody,
                                identifier,
                                ChatList.get_or_create_group(context, user, user, true),
                                null,
                                isSent,
                                isEdit);
                    } else if (type.equals("group")) {
                        if (isSent && to != null) { // I wrote to a group
                            message = new GroupMessage(
                                    ID,
                                    date,
                                    messageBody,
                                    identifier,
                                    ChatList.get_or_create_group(context, to, to, false),
                                    ChatList.get_meUser(context), // TODO: correct?
                                    isSent,
                                    isEdit);
                        } else if (!isSent && to != null && from != null) { // Somebody sent to a group
                            message = new GroupMessage(
                                    ID,
                                    date,
                                    messageBody,
                                    identifier,
                                    ChatList.get_or_create_group(context, to, to, false),
                                    ChatList.get_or_create_user(context, from, from, phone),
                                    isSent,
                                    isEdit);
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
                                ChatList.get_or_create_user(context, to, to, phone),
                                isSent,
                                isEdit);
                    } else if (!isSent && from != null){ // Somebody sent me a message
                        message = new UserMessage(
                                ID,
                                date,
                                messageBody,
                                identifier,
                                ChatList.get_or_create_user(context, from, from, phone),
                                isSent,
                                isEdit);
                    } else {
                        Toast.makeText(context,"Invalid message:\n" + body, Toast.LENGTH_LONG);
                    }
                }
            }
        }
        return message;
    }
}
