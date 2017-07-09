package com.ramimartin.multibluetooth.bluetooth.server;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.util.Log;

import com.ramimartin.multibluetooth.bluetooth.BluetoothRunnable;
import com.ramimartin.multibluetooth.bluetooth.manager.BluetoothManager;
import com.ramimartin.multibluetooth.bus.ServeurConnectionFail;
import com.ramimartin.multibluetooth.bus.ServeurConnectionSuccess;

import java.io.IOException;
import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * Created by Rami on 16/06/2017.
 */
public class BluetoothServer extends BluetoothRunnable {

    private static final String TAG = BluetoothServer.class.getSimpleName();

    private UUID mUUID;
    private BluetoothServerSocket mServerSocket;

    public BluetoothServer(BluetoothAdapter bluetoothAdapter, String uuiDappIdentifier, String adressMacClient, Activity activity, BluetoothManager.MessageMode messageMode) {
        super(bluetoothAdapter, uuiDappIdentifier, activity, messageMode);
        mClientAddress = adressMacClient;
        mUUID = UUID.fromString(uuiDappIdentifier + "-" + mClientAddress.replace(":", ""));
    }

    @Override
    public void waitForConnection() {
        // NOTHING TO DO IN THE SERVER
    }

    @Override
    public void intiObjReader() throws IOException {
        mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("BLTServer", mUUID);
        mSocket = mServerSocket.accept();
        mInputStream = mSocket.getInputStream();
    }

    @Override
    public void onConnectionSucess() {
        EventBus.getDefault().post(new ServeurConnectionSuccess(mClientAddress));
    }

    @Override
    public void onConnectionFail() {
        EventBus.getDefault().post(new ServeurConnectionFail(mClientAddress));
    }

    @Override
    public void closeConnection() {
        super.closeConnection();
        try {
            mServerSocket.close();
            mServerSocket = null;
        } catch (Exception e) {
            Log.e("", "===+++> closeConnection Exception e : "+e.getMessage());
        }
    }
}
