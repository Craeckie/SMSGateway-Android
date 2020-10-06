package de.sanemind.smsgateway.model;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import android.telephony.PhoneNumberUtils;

import de.sanemind.smsgateway.Utils;

public class PhoneNumber implements Comparable<PhoneNumber> {
    private String number;
    private int priority;

    public PhoneNumber(Context context, String number, int priority) {
        setNumber(context, number);
        this.priority = priority;
    }

    public String getNumber(boolean prependPlus) {
        if (prependPlus)
            return "+" + number;
        else
            return number;
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
            return PhoneNumberUtils.compare(number, ((PhoneNumber)obj).getNumber(false));
        else if (obj instanceof String)
            return PhoneNumberUtils.compare(number, ((String)obj));
        else
            return super.equals(obj);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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
