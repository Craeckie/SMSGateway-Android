package de.sanemind.smsgateway.model;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.telephony.PhoneNumberUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.sanemind.smsgateway.Utils;

public class UserChat extends BaseChat {
    public UserChat(String name, String nameIdentifier) {
        super(name, nameIdentifier);
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
                num.priority = Math.min(priority, num.priority);
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserChat) {
            UserChat u = (UserChat) obj;
            return ! Collections.disjoint(phoneNumbers, u.phoneNumbers);
        } else return super.equals(obj);
    }

    @Override
    public String getIdentifier() {
        if (mostImportantPhoneNumber != null)
            return mostImportantPhoneNumber.getNumber();
        else
            return name;
    }

    public class PhoneNumber implements Comparable<PhoneNumber> {
        private String number;
        private int priority;

        public PhoneNumber(Context context, String number, int priority) {
            setNumber(context, number);
            this.priority = priority;
        }

        public String getNumber() {
            return "+" + number;
        }

        public void setNumber(Context context, String number) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                number = PhoneNumberUtils.normalizeNumber(number);
            }
            if (number.startsWith("0"))
                number = Utils.getCountryCode(context) + number.substring(1);
            else if (number.startsWith("+"))
                number = number.substring(1);
            this.number = number;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PhoneNumber)
                return PhoneNumberUtils.compare(number, ((PhoneNumber)obj).getNumber());
            else if (obj instanceof String)
                return PhoneNumberUtils.compare(number, ((String)obj));
            else
                return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return number.hashCode();
        }

        @Override
        public String toString() {
            return number;
        }

        @Override
        public int compareTo(@NonNull PhoneNumber o) {
            return priority - o.priority;
        }
    }

}

