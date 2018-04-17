package de.sanemind.smsgateway;

import android.content.Context;

import java.util.Date;

import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.GroupChat;
import de.sanemind.smsgateway.model.GroupMessage;
import de.sanemind.smsgateway.model.UserMessage;

public class GatewayUtils {

    public static BaseMessage tryParseGatewayMessage(Context context, String body, Date date, boolean isSent) {
        BaseMessage message = null;
        String[] lines = body.split("\n");
        if (lines.length > 2) {
            String identifier = lines[0].trim();
            if (identifier.equalsIgnoreCase("TG") || identifier.equalsIgnoreCase("SG") || identifier.equalsIgnoreCase("FB")) {
                String addressLine = lines[1];
                String address = null;
                if (addressLine.matches("^From: (.*)$")) {
                    address = addressLine.substring("From: ".length());

                } else if (addressLine.matches("^To: (.*)$")) {
                    address = addressLine.substring("To: ".length());
                    isSent = true;
                }
                if (address != null) {
                    String messageBody = lines[2];
                    for (int i = 3; i < lines.length; i++) {
                        messageBody += "\n" + lines[i];
                    }
                    String[] addressParts = address.split("@");
                    if (addressParts.length == 2) {
                        String userName = addressParts[0];
                        String groupIdentifier = addressParts[1];
                        String groupName = groupIdentifier.replace('_', ' ');
                        message = new GroupMessage(
                                date,
                                messageBody,
                                identifier,
                                ChatList.get_or_create_group(context, groupName, groupIdentifier),
                                userName,
                                isSent);
                    } else {
                        String name = address.replace('_', ' ');
                        GroupChat groupChat = ChatList.find_group(context, address);
                        if (isSent && groupChat != null) {
                            message = new GroupMessage(
                                    date,
                                    messageBody,
                                    identifier,
                                    groupChat,
                                    name,
                                    isSent);
                        } else {
                            message = new UserMessage(
                                    date,
                                    messageBody,
                                    identifier,
                                    ChatList.get_or_create_user(context, name, address),
                                    isSent);
                        }
                    }

                }
            }
        }
        return message;
    }
}
