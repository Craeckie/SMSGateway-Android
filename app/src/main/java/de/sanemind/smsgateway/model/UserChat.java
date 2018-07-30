package de.sanemind.smsgateway.model;

import android.content.Context;
import android.net.Uri;

import java.util.HashSet;
import java.util.Set;

public class UserChat extends BaseChat {
    public UserChat(ChatList chatList, String name, String nameIdentifier) {
        super(chatList, name, nameIdentifier);
        this.phoneNumbers = new HashSet<>();
        this.nameIdentifier = nameIdentifier;
    }

    @Override
    public String getDisplayName() {
        if (name != null)
            return name;
        if (nameIdentifier != null)
            return nameIdentifier;
        if (mostImportantPhoneNumber != null)
            return mostImportantPhoneNumber.getNumber();
        return null;
    }

    private Set<PhoneNumber> phoneNumbers;

    private PhoneNumber mostImportantPhoneNumber;
    private int mostImportantPriority = Integer.MAX_VALUE;
    private Uri pictureUri;

    public Set<PhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    public PhoneNumber addPhoneNumber(Context context, String phoneNumber, int priority) {
        PhoneNumber curNum = null;
        for (PhoneNumber num : phoneNumbers) {
            if (num.equals(phoneNumber)) {
                num.setPriority(Math.min(priority, num.getPriority()));
                curNum = num;
            }
        }
        if (curNum == null) {
            curNum = new PhoneNumber(context, phoneNumber, priority);
            this.phoneNumbers.add(curNum);
        }
        if (priority < mostImportantPriority) {
            mostImportantPriority = priority;
            mostImportantPhoneNumber = curNum;
        }
        return curNum;
    }

    public boolean hasPhoneNumber(Context context, String phoneNumber) {
        return phoneNumbers.contains(new PhoneNumber(context, phoneNumber, -1));
    }

    public PhoneNumber getMostImportantPhoneNumber() {
        return mostImportantPhoneNumber;
    }

    public Uri getPictureUri() {
        return pictureUri;
    }

    public void setPictureUri(Uri pictureUri) {
        this.pictureUri = pictureUri;
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (obj instanceof UserChat) {
//            UserChat u = (UserChat) obj;
//            return ! Collections.disjoint(phoneNumbers, u.phoneNumbers);
//        } else return super.equals(obj);
//    }

    @Override
    public String getIdentifier() {
        if (mostImportantPhoneNumber != null)
            return mostImportantPhoneNumber.getNumber();
        else
            return name;
    }
}


