package com.ramimartin.multibluetooth.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.IBinder;

import com.ramimartin.multibluetooth.bluetooth.mananger.BluetoothManager;
import com.ramimartin.multibluetooth.bus.BluetoothCommunicator;
import com.ramimartin.multibluetooth.bus.BondedDevice;
import com.ramimartin.multibluetooth.bus.ClientConnectionFail;
import com.ramimartin.multibluetooth.bus.ClientConnectionSuccess;
import com.ramimartin.multibluetooth.bus.ServeurConnectionFail;
import com.ramimartin.multibluetooth.bus.ServeurConnectionSuccess;

import de.greenrobot.event.EventBus;

/**
 * Created by rei on 6/29/15.
 */
public abstract class BluetoothService extends Service {

    protected BluetoothManager mBluetoothManager;

    public BluetoothService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        btIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(btIntent);

        mBluetoothManager = new BluetoothManager(this);
        checkBluetoothAviability();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        closeAllConnexion();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        mBluetoothManager.setNbrClientMax(myNbrClientMax());

        return START_NOT_STICKY;

    }

    public void closeAllConnexion(){
        mBluetoothManager.closeAllConnexion();
    }

    public void checkBluetoothAviability(){
        if(!mBluetoothManager.checkBluetoothAviability()){
            onBluetoothNotAviable();
        }
    }

    public void setTimeDiscoverable(int timeInSec){
        mBluetoothManager.setTimeDiscoverable(timeInSec);
    }

    public void startDiscovery(){
        mBluetoothManager.startDiscovery();
    }

    public void scanAllBluetoothDevice(){
        mBluetoothManager.scanAllBluetoothDevice();
    }

    public void disconnectClient(){
        mBluetoothManager.disconnectClient();
    }

    public void disconnectServer(){
        mBluetoothManager.disconnectServer();
    }

    public void createServeur(String address){
        mBluetoothManager.createServeur(address);
    }

    public void selectServerMode(){
        mBluetoothManager.selectServerMode();
    }
    public void selectClientMode(){
        mBluetoothManager.selectClientMode();
    }

    public BluetoothManager.TypeBluetooth getTypeBluetooth(){
        return mBluetoothManager.mType;
    }

    public BluetoothManager.TypeBluetooth getBluetoothMode(){
        return mBluetoothManager.mType;
    }

    public void createClient(String addressMac){
        mBluetoothManager.createClient(addressMac);
    }

    public void sendMessage(String message){
        mBluetoothManager.sendMessage(message);
    }

    public boolean isConnected(){
        return mBluetoothManager.isConnected;
    }

    public abstract int myNbrClientMax();
    public abstract void onBluetoothDeviceFound(BluetoothDevice device);
    public abstract void onClientConnectionSuccess();
    public abstract void onClientConnectionFail();
    public abstract void onServeurConnectionSuccess();
    public abstract void onServeurConnectionFail();
    public abstract void onBluetoothStartDiscovery();
    public abstract void onBluetoothCommunicator(String messageReceive);
    public abstract void onBluetoothNotAviable();

    public void onEventMainThread(BluetoothDevice device){
        onBluetoothDeviceFound(device);
        createServeur(device.getAddress());
    }

    public void onEventMainThread(ClientConnectionSuccess event){
        mBluetoothManager.isConnected = true;
        onClientConnectionSuccess();
    }

    public void onEventMainThread(ClientConnectionFail event){
        mBluetoothManager.isConnected = false;
        onClientConnectionFail();
    }

    public void onEventMainThread(ServeurConnectionSuccess event){
        mBluetoothManager.isConnected = true;
        mBluetoothManager.onServerConnectionSuccess(event.mClientAdressConnected);
        onServeurConnectionSuccess();
    }

    public void onEventMainThread(ServeurConnectionFail event){
        mBluetoothManager.onServerConnectionFailed(event.mClientAdressConnectionFail);
        onServeurConnectionFail();
    }

    public void onEventMainThread(BluetoothCommunicator event){
        onBluetoothCommunicator(event.mMessageReceive);
    }

    public void onEventMainThread(BondedDevice event){
        //mBluetoothManager.sendMessage("BondedDevice");
    }

}