package de.sanemind.smsgateway;

import android.content.Context;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.GroupChat;
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
            String name = null;
            String group = null;
            String phone = null;
            String messageBody = "";
            boolean messageStarted = false;
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (messageStarted) {
                    if (!messageBody.isEmpty())
                        messageBody += "\n";
                    messageBody += line;
                } else if (line.isEmpty()) {
                    messageStarted = true;
                } else if (line.matches("^From: (.*)$")) {
                    name = line.substring("From: ".length());
                    // TODO: temporarily support old format for messages sent to groups
                    if (phone == null && group == null) {
                        Matcher matcherGroup = oldFromGroupPattern.matcher(line);
                        Matcher matcherNumber = oldNumberPattern.matcher(line);
                        if (matcherGroup.matches()) {
                            name = matcherGroup.group(1);
                            phone = matcherGroup.group(2);
                            group = matcherGroup.group(3);
                        } else if (matcherNumber.matches()) {
                            name = matcherNumber.group(2);
                            phone = matcherNumber.group(3);
                        }
                    }
                } else if (line.matches("^To: (.*)$")) {
                    name = line.substring("To: ".length());
                    Matcher matcherNumber = oldNumberPattern.matcher(line);
                    if (phone == null && matcherNumber.matches()) {
                        name = matcherNumber.group(2);
                        phone = matcherNumber.group(3);
                    }
                    isSent = true;
                } else if (line.matches("^Group: (.*)$")) {
                    group = line.substring("Group: ".length());
                } else if (line.matches("^Phone: (.*)$")) {
                    phone = line.substring("Phone: ".length());
                } else { // Unknown header, assume that message starts for now..
                    messageStarted = true;
                    messageBody = line;
                }
            }

            if (name != null) {
                if (group != null && !isSent) { // Somebody sent to a group
                    message = new GroupMessage(
                            date,
                            messageBody,
                            identifier,
                            ChatList.get_or_create_group(context, group, group),
                            ChatList.get_or_create_user(context, name, name, phone),
                            isSent);
                } else {
                    GroupChat groupChat = ChatList.find_group(context, name);
                    if (isSent && groupChat != null) { // I sent to a group
                        message = new GroupMessage(
                                date,
                                messageBody,
                                identifier,
                                groupChat,
                                ChatList.get_or_create_user(context, name, name, phone),
                                isSent);
                    }
                    else { // Somebody sent me a message
                        message = new UserMessage(
                                date,
                                messageBody,
                                identifier,
                                ChatList.get_or_create_user(context, name, name, phone),
                                isSent);
                    }
                }
            }
        }
        return message;
    }
}
