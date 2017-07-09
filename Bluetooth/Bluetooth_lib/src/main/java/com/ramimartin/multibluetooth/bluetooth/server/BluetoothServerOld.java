package com.ramimartin.multibluetooth.bluetooth.server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.ramimartin.multibluetooth.bus.BluetoothCommunicatorString;
import com.ramimartin.multibluetooth.bus.BluetoothCommunicatorObject;
import com.ramimartin.multibluetooth.bus.ServeurConnectionFail;
import com.ramimartin.multibluetooth.bus.ServeurConnectionSuccess;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * Created by Rami MARTIN on 13/04/2014.
 */
public class BluetoothServerOld implements Runnable {

    private static final String TAG = BluetoothServerOld.class.getSimpleName();

    private boolean CONTINUE_READ_WRITE = true;

    private UUID mUUID;
    public String mClientAddress;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket mServerSocket;
    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private OutputStreamWriter mOutputStreamWriter;
    private ObjectOutputStream mObjectOutputStream;
    private ObjectInputStream mObjectInputStream;
    private boolean mIsConnected;

    public BluetoothServerOld(BluetoothAdapter bluetoothAdapter, String uuiDappIdentifier, String clientAddress) {
        mBluetoothAdapter = bluetoothAdapter;
        mClientAddress = clientAddress;
        mUUID = UUID.fromString(uuiDappIdentifier + "-" + mClientAddress.replace(":", ""));
        mIsConnected = false;
    }

    @Override
    public void run() {
        try {
            mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("BLTServer", mUUID);
            mSocket = mServerSocket.accept();

            int bufferSize = 1024;
            int bytesRead = -1;
            byte[] buffer = new byte[bufferSize];

            EventBus.getDefault().post(new ServeurConnectionSuccess(mClientAddress));

            mOutputStreamWriter = new OutputStreamWriter(mSocket.getOutputStream());
            mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
            mInputStream = mSocket.getInputStream();

            mIsConnected = true;

            while (CONTINUE_READ_WRITE) {

                synchronized (this) {

                    try {
                        mObjectInputStream = new ObjectInputStream(mInputStream);

                        Object messageObj = mObjectInputStream.readUnshared();    // read from the object stream,
                        EventBus.getDefault().post(new BluetoothCommunicatorObject(messageObj));

                    } catch (ClassNotFoundException e) {
                        Log.e(TAG, "Error ObjectInputStream ClassNotFoundException: " + e.getLocalizedMessage());
                    } catch (IOException e) {
                        Log.e(TAG, "Error ObjectInputStream IOException getCause : " + e.getMessage());
                    }
                    final StringBuilder sb = new StringBuilder();
                    bytesRead = mInputStream.read(buffer);
                    if (bytesRead != -1) {
                        String result = "";
                        while ((bytesRead == bufferSize) && (buffer[bufferSize] != 0)) {
                            result = result + new String(buffer, 0, bytesRead);
                            bytesRead = mInputStream.read(buffer);
                        }
                        result = result + new String(buffer, 0, bytesRead);
                        sb.append(result);
                    }
                    EventBus.getDefault().post(new BluetoothCommunicatorString(sb.toString()));
                }

            }
        } catch (IOException e) {
            Log.e("", "===> ERROR thread server : " + e.getMessage());
            EventBus.getDefault().post(new ServeurConnectionFail(mClientAddress));
        }
    }

    public void write(String message) {
        try {
            if (mOutputStreamWriter != null) {
                mOutputStreamWriter.write(message);
                mOutputStreamWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeSerialized(Object obj) {
        try {
            mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
            mObjectOutputStream.writeUnshared(obj);
            mObjectOutputStream.reset();
        } catch (Exception e) {
            Log.e(TAG, "Error ObjectOutputStream: " + e.getLocalizedMessage());
        }
    }

    public void closeConnection() {
        if (mSocket != null) {
            try {
                mInputStream.close();
                mInputStream = null;
                mOutputStreamWriter.close();
                mOutputStreamWriter = null;
                mObjectOutputStream.close();
                mObjectOutputStream = null;
                mObjectInputStream.close();
                mObjectInputStream = null;
                mSocket.close();
                mSocket = null;
                mServerSocket.close();
                mServerSocket = null;
                CONTINUE_READ_WRITE = false;
            } catch (Exception e) {
            }
            CONTINUE_READ_WRITE = false;
        }
    }

    public boolean isConnected() {
        return mIsConnected;
    }
}
