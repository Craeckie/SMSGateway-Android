package de.sanemind.smsgateway;

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import de.sanemind.smsgateway.model.UserChat;

public class ContactsLoader {

    private final static String name_column = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY:
            ContactsContract.Contacts.DISPLAY_NAME;
    public static void updateUserChat(Context context, UserChat chat) {
        if (chat.isUpdatedFromContacts())
            return;

        long ID = 0;
        boolean foundContact = false;
        // Try to find name, if only number available
        if (PhoneNumberUtils.isGlobalPhoneNumber(chat.getName())) {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(chat.getName()));
            Cursor cursor = context.getContentResolver().query(uri, new String[] {BaseColumns._ID,
                    name_column}, null, null, null);
            try {
                int id_column = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                int name_column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                if (cursor.moveToFirst()) {
                    ID = cursor.getLong(id_column);
                    String name = cursor.getString(name_column);

                    String number = chat.getName();

                    chat.setName(name);
                    chat.addPhoneNumber(number, 2);
                    foundContact = true;
                }
            } catch (Exception e) {
                Log.v("SMSGateway", e.getMessage());
            }
            cursor.close();
        }
        // Try to find ID using name
        if (!foundContact) {
            CursorLoader loader = new android.content.CursorLoader(context,
                    ContactsContract.Contacts.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?",
                    new String[]{"%" + chat.getName() + "%"},
                    null);
            Cursor cursor = loader.loadInBackground();
            if (cursor.getCount() > 0) {
                int id_column = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                int name_column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int hasPhoneNumber_column = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
                int phoneNumber_column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);


                if (cursor.moveToFirst()) {
                    ID = cursor.getLong(id_column);
                    String name = cursor.getString(name_column);
                    String hasPhoneNumber = cursor.getString(hasPhoneNumber_column);
                    foundContact = true;
                }
                cursor.close();
            }
        }
        if (foundContact) {
            Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + ID, null, null);

            while (phones.moveToNext()) {
                String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                int priority;
                switch (type) {
                    case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                        priority = 1;
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                        priority = 5;
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                        priority = 10;
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                        priority = 15;
                        break;
                    default:
                        priority = 20;
                        break;
                }

                chat.addPhoneNumber(number, priority);
                foundContact = true;
            }
            phones.close();
        }

        if (foundContact && chat.getPictureUri() == null) {
            Cursor cur = context.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + "=" + ID + " AND "
                            + ContactsContract.Data.MIMETYPE + "='"
                            + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
                    null);
            if (cur != null && cur.moveToFirst()) {
                Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, ID);
                Uri picture_uri = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
                chat.setPictureUri(picture_uri);
            }
        }

        chat.setUpdatedFromContacts();
    }


//    android.content.CursorLoader loader =
//
//    Cursor cursor = loader.loadInBackground();
//    int indexID = cursor.getColumnIndex(ContactsContract.Contacts._ID);
//    int indexName = cursor.getColumnIndex(Build.VERSION.SDK_INT
//            >= Build.VERSION_CODES.HONEYCOMB ?
//            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
//            ContactsContract.Contacts.DISPLAY_NAME);
//    int indexPicture = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI);
//        do {
//        String name = cursor.getString(indexName);
////            long id = cursor.getLong(indexID);
////            Uri path = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, indexID);
////            Uri picturePath = Uri.withAppendedPath(path, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
//
//    } while (cursor.moveToNext());


    private final String[] PROJECTION =
            {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    Build.VERSION.SDK_INT
                            >= Build.VERSION_CODES.HONEYCOMB ?
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                            ContactsContract.Contacts.DISPLAY_NAME

            };
    private final String SELECTION =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?" :
                    ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";
}
