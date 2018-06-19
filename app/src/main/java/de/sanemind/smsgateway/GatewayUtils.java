package de.sanemind.smsgateway;

import android.content.Context;
import android.telephony.PhoneNumberUtils;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            String addressLine;
            if (identifier.equalsIgnoreCase("TG") || identifier.equalsIgnoreCase("SG") || identifier.equalsIgnoreCase("FB")) {
                addressLine = lines[1];
            } else {
                // TODO: Just assume it's TG for now
                addressLine = lines[0];
                identifier = "TG";
            }
            String address = null;
            if (addressLine.matches("^From: (.*)$")) {
                address = addressLine.substring("From: ".length());

            } else if (addressLine.matches("^To: (.*)$")) {
                address = addressLine.substring("To: ".length());
                isSent = true;
            } else {
                return null;
            }
            if (address != null) {
                String messageBody = lines[2];
                for (int i = 3; i < lines.length; i++) {
                    messageBody += "\n" + lines[i];
                }
                String[] addressParts = address.split("@");
                if (addressParts.length == 2) {
                    String userIdentifier = addressParts[0];
                    String userName = userIdentifier.replace('_', ' ');
                    String groupIdentifier = addressParts[1];
                    String groupName = groupIdentifier.replace('_', ' ');
                    message = new GroupMessage(
                            date,
                            messageBody,
                            identifier,
                            ChatList.get_or_create_group(context, groupName, groupIdentifier),
                            ChatList.get_or_create_user(context, userName, userIdentifier, null),
                            isSent);
                } else {
                    Pattern namePattern = Pattern.compile("^(.+) \\(\\+?([0-9]+)\\)$");
                    Matcher nameMatch = namePattern.matcher(address);
                    String phoneNumber = null;
                    if (nameMatch.matches()) {
                        address = nameMatch.group(1);
                        phoneNumber = nameMatch.group(2);
                    }
                    String name = address.replace('_', ' ');
                    String nameIdentifier = address;
                    if (phoneNumber == null && !PhoneNumberUtils.isGlobalPhoneNumber(address)) {
                        phoneNumber = address;
                        nameIdentifier = null;
                    }
                    GroupChat groupChat = ChatList.find_group(context, address);
                    if (isSent && groupChat != null) {
                        message = new GroupMessage(
                                date,
                                messageBody,
                                identifier,
                                groupChat,
                                ChatList.get_or_create_user(context, name, nameIdentifier, phoneNumber),
                                isSent);
                    } else {
                        phoneNumber = null;
                        nameIdentifier = address;
                        if (PhoneNumberUtils.isGlobalPhoneNumber(address)) {
                            phoneNumber = address;
                            nameIdentifier = null;
                        }
                        message = new UserMessage(
                                date,
                                messageBody,
                                identifier,
                                ChatList.get_or_create_user(context, name, nameIdentifier, phoneNumber),
                                isSent);
                    }
                }
            }
        }
        return message;
    }
}
