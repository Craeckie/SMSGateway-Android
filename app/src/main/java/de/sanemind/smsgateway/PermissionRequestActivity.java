package de.sanemind.smsgateway;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public abstract class PermissionRequestActivity extends AppCompatActivity {

    public void requestPermissions() {
        String gatewayNumber = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("edit_text_preference_phone_gateway", null);
        if (gatewayNumber != null && !MessageList.isRefreshedFromSMSInbox() && getPermission(Manifest.permission.READ_SMS, getString(R.string.request_read_sms_permission), READ_SMS_PERMISSIONS_REQUEST)
                && getPermission(Manifest.permission.READ_CONTACTS, getString(R.string.request_read_contact_permission),  READ_CONTACTS_PERMISSIONS_REQUEST)) {
            MessageList.refreshFromSMSInbox(getApplicationContext());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 42) {
            String gatewayNumber = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("edit_text_preference_phone_gateway", null);
            if (gatewayNumber != null && !MessageList.isRefreshedFromSMSInbox() && getPermission(Manifest.permission.READ_SMS, getString(R.string.request_read_sms_permission), READ_SMS_PERMISSIONS_REQUEST)
                    && getPermission(Manifest.permission.READ_CONTACTS, getString(R.string.request_read_contact_permission),  READ_CONTACTS_PERMISSIONS_REQUEST)) {
                MessageList.refreshFromSMSInbox(getApplicationContext());
            }
        }
        else
            super.onActivityResult(requestCode, resultCode, data);
    }



    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 2;


    protected boolean getPermission(String permission, String message, int request_id) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[] {permission},
                        request_id);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Make sure it's our original READ_SMS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Read SMS permission granted", Toast.LENGTH_SHORT).show();
                getPermission(Manifest.permission.READ_CONTACTS, getString(R.string.request_read_contact_permission), READ_CONTACTS_PERMISSIONS_REQUEST);


            } else {
                Toast.makeText(getApplicationContext(), "Read SMS permission denied :(", Toast.LENGTH_SHORT).show();

            }
        } else if (requestCode == READ_CONTACTS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Read contacts permission granted", Toast.LENGTH_SHORT).show();
                if (getPermission(Manifest.permission.READ_SMS, getString(R.string.request_read_sms_permission), READ_SMS_PERMISSIONS_REQUEST)) {
                    MessageList.refreshFromSMSInbox(getApplicationContext());
                }
            } else {
                Toast.makeText(getApplicationContext(), "Read contacts permission denied :(", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
