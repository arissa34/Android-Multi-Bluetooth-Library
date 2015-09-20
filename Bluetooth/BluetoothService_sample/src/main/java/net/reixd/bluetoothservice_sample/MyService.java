package net.reixd.bluetoothservice_sample;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.ramimartin.multibluetooth.service.BluetoothService;

public class MyService extends BluetoothService {

    public final static String TAG = "MyService";

    public MyService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        int RET = super.onStartCommand(intent, flags, startId);

        setTimeDiscoverable(com.ramimartin.multibluetooth.bluetooth.mananger.BluetoothManager.BLUETOOTH_TIME_DICOVERY_3600_SEC);
        selectServerMode();
        scanAllBluetoothDevice();

        return RET;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int myNbrClientMax() {
        return 0;
    }

    @Override
    public void onBluetoothDeviceFound(BluetoothDevice device) {
        Log.e(TAG, "onBluetoothDeviceFound :" + device.getAddress() + " " + device.getName());
    }

    @Override
    public void onClientConnectionSuccess() {
        Log.e(TAG,"onClientConnectionSuccess" );
    }

    @Override
    public void onClientConnectionFail() {
        Log.e(TAG,"onClientConnectionFail" );
    }

    @Override
    public void onServeurConnectionSuccess() {
        Log.e(TAG,"onServeurConnectionSuccess" );
    }

    @Override
    public void onServeurConnectionFail() {
        Log.e(TAG, "onServeurConnectionFail" );
    }

    @Override
    public void onBluetoothStartDiscovery() {
        Log.e(TAG, "onBluetoothStartDiscovery");
    }

    @Override
    public void onBluetoothCommunicator(String messageReceive) {
        Log.e(TAG,"onBluetoothCommunicator: " +  messageReceive );
    }

    @Override
    public void onBluetoothNotAviable() {
        Log.e(TAG, "onBluetoothNotAviable" );
    }
}
