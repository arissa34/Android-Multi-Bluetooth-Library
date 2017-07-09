package com.ramimartin.multibluetooth.bluetooth.client;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.ramimartin.multibluetooth.bluetooth.BluetoothRunnable;
import com.ramimartin.multibluetooth.bluetooth.manager.BluetoothManager;
import com.ramimartin.multibluetooth.bus.ClientConnectionFail;
import com.ramimartin.multibluetooth.bus.ClientConnectionSuccess;

import java.io.IOException;
import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * Created by Rami on 16/06/2017.
 */
public class BluetoothClient extends BluetoothRunnable {

    private static final String TAG = BluetoothClient.class.getSimpleName();

    private UUID mUUID;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothConnector mBluetoothConnector;

    private boolean KEEP_TRYING_CONNEXION;

    public BluetoothClient(BluetoothAdapter bluetoothAdapter, String uuiDappIdentifier, String adressMacServer, Activity activity, BluetoothManager.MessageMode messageMode) {
        super(bluetoothAdapter, uuiDappIdentifier, activity, messageMode);
        mServerAddress = adressMacServer;
        mUUID = UUID.fromString(uuiDappIdentifier + "-" + mMyAdressMac.replace(":", ""));
        KEEP_TRYING_CONNEXION = true;
    }

    @Override
    public void waitForConnection() {

        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mServerAddress);

        while (mInputStream == null && CONTINUE_READ_WRITE && KEEP_TRYING_CONNEXION) {
            mBluetoothConnector = new BluetoothConnector(mBluetoothDevice, false, mBluetoothAdapter, mUUID);

            try {
                mSocket = mBluetoothConnector.connect().getUnderlyingSocket();
                mInputStream = mSocket.getInputStream();
            } catch (IOException e1) {
                Log.e("", "===> mSocket IOException : "+ e1.getMessage());
                //EventBus.getDefault().post(new ClientConnectionFail(mServerAddress));
                e1.printStackTrace();
            }
        }

        if (mSocket == null) {
            Log.e("", "===> mSocket IS NULL");
            return;
        }
    }

    @Override
    public void intiObjReader() throws IOException {
    }

    @Override
    public void onConnectionSucess() {
        EventBus.getDefault().post(new ClientConnectionSuccess());
    }

    @Override
    public void onConnectionFail() {
        EventBus.getDefault().post(new ClientConnectionFail(mServerAddress));
    }

    @Override
    public void closeConnection() {
        KEEP_TRYING_CONNEXION = false;
        super.closeConnection();
    }
}
