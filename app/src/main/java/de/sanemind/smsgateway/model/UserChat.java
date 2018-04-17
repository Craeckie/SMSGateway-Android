package de.sanemind.smsgateway.model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.telephony.PhoneNumberUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UserChat extends BaseChat {
    public UserChat(String name, String nameIdentifier) {
        super(name, nameIdentifier);
        this.phoneNumbers = new HashSet<>();
        this.nameIdentifier = nameIdentifier;
    }

    private Set<PhoneNumber> phoneNumbers;

    private PhoneNumber mostImportantPhoneNumber;
    private int mostImportantPriority = Integer.MAX_VALUE;
    private Uri pictureUri;

    public Set<PhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void addPhoneNumber(String phoneNumber, int priority) {
        PhoneNumber curNum = null;
        for (PhoneNumber num : phoneNumbers) {
            if (num.equals(phoneNumber)) {
                num.priority = Math.min(priority, num.priority);
                curNum = num;
            }
        }
        if (curNum == null) {
            curNum = new PhoneNumber(phoneNumber, priority);
            this.phoneNumbers.add(curNum);
        }
        if (priority < mostImportantPriority) {
            mostImportantPriority = priority;
            mostImportantPhoneNumber = curNum;
        }
    }

    public boolean hasPhoneNumber(String phoneNumber) {
        return phoneNumbers.contains(new PhoneNumber(phoneNumber, -1));
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

    @Override
    public String toString() {
        return name;
    }

    public class PhoneNumber implements Comparable<PhoneNumber> {
        private String number;
        private int priority;

        public PhoneNumber(String number, int priority) {
            this.number = number;
            this.priority = priority;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
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

