package com.carl.touch;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class LockScreenActivity extends Activity {

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mLockScreenAdmin;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mDevicePolicyManager = (DevicePolicyManager) this
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        mLockScreenAdmin = new ComponentName(this, LockScreenAdmin.class);
        if (mDevicePolicyManager.isAdminActive(mLockScreenAdmin)) {
        } else {
            Log.d(TAG, "Request lockScreen admin" );
            Intent intent = new Intent(
                    DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    mLockScreenAdmin);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "request admin");
            startActivityForResult(intent,1);
        }
        Log.i(TAG, "Already got lock admin");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}