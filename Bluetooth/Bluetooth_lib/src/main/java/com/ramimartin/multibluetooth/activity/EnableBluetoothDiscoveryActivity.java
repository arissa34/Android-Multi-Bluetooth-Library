package com.ramimartin.multibluetooth.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ramimartin.multibluetooth.R;
import com.ramimartin.multibluetooth.bluetooth.mananger.BluetoothManager;

public class EnableBluetoothDiscoveryActivity extends Activity {

    public static final String TAG = "EnableBTADiscovery";
    private int mTimeDiscoverable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable_bluetooth_discovery);

        mTimeDiscoverable = getIntent().getIntExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1);

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, mTimeDiscoverable);
        startActivityForResult(discoverableIntent, BluetoothManager.REQUEST_DISCOVERABLE_CODE);

    }

    //TODO
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult() " + Integer.toString(requestCode) + " " + Integer.toString(resultCode));

        switch (requestCode) {
            case BluetoothManager.REQUEST_DISCOVERABLE_CODE:
                Log.d(TAG, "BluetoothManager.REQUEST_DISCOVERABLE_CODE");

                if (resultCode == BluetoothManager.BLUETOOTH_REQUEST_REFUSED) {
                    Log.d(TAG, "BluetoothManager.BLUETOOTH_REQUEST_REFUSED");
                } else {
                    Log.d(TAG, "BluetoothManager.BLUETOOTH_REQUEST_ACCEPTED");

                    Log.e(TAG, "onBluetoothStartDiscovery()");
                    //onBluetoothStartDiscovery();

                    Intent intent1 = new Intent(getApplicationContext(),BluetoothManager.class);
                    intent1.setAction("onBluetoothStartDiscovery");
                    intent1.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, mTimeDiscoverable);
                    sendBroadcast(intent1);
                }
                break;
            default:
                Log.d(TAG, "requestCode := " + Integer.toString(requestCode));
                break;
        }

        finish();
    }

}

