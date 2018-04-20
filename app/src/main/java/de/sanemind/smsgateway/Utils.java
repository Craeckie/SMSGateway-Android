package de.sanemind.smsgateway;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.Date;

public class Utils {
    public static CharSequence formatRelativeTime(Context context, long time) {
        long daymiliseconds = (24 * 60 * 60 * 1000);
        long date = (time - (long) (Math.IEEEremainder(time, daymiliseconds))) / daymiliseconds;
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTimeInMillis(time);

//        int hour1 = dateCal.get(Calendar.HOUR);
//        int hour2 = dateCal.get(Calendar.HOUR_OF_DAY);
//        dateCal.set(Calendar.HOUR, 0);
//        dateCal.set(Calendar.MINUTE, 0);
//        Calendar curDateCal = Calendar.getInstance();
//        curDateCal.setTime(new Date());
//        int hour_1 = curDateCal.get(Calendar.HOUR);
//        int hour_2 = curDateCal.get(Calendar.HOUR_OF_DAY);
//        curDateCal.set(Calendar.HOUR, 0);
        CharSequence timeStr;
//        long dateTime = dateCal.getTimeInMillis();

        long curTime = new Date().getTime();
        long curDate = (curTime - (long) (Math.IEEEremainder(curTime, daymiliseconds))) / daymiliseconds;
        long dateDiff = Math.abs(curDate - date);
        if (DateUtils.isToday(time)) {
            if (Math.abs(time - new Date().getTime()) < 60 * 1000) {
                timeStr = "Now";
            } else {
                timeStr = DateUtils.getRelativeTimeSpanString(time);
            }
        } else if (dateDiff <= 1) {
            timeStr = "Yesterday " + DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_TIME);
        } else if (dateDiff <= 7) {
            timeStr = DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_TIME);
        } else {
            timeStr = DateUtils.getRelativeDateTimeString(context, time, 10000, 1000000, 0);
        }

        CharSequence testTimeStr = DateUtils.getRelativeDateTimeString(context, time, 10000, 1000000, 0);
        return timeStr;
    }

    private static String countryCode = null;

    public static String getCountryCode(Context context) {
        if (countryCode == null) {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //getNetworkCountryIso
            String CountryID = manager.getSimCountryIso().toUpperCase().trim();
            String[] rl = context.getResources().getStringArray(R.array.CountryCodes);
            for (int i = 0; i < rl.length; i++) {
                String[] g = rl[i].split(",");
                if (g[1].trim().equals(CountryID)) {
                    countryCode = g[0];
                    break;
                }
            }
        }
        return countryCode;
    }
}
