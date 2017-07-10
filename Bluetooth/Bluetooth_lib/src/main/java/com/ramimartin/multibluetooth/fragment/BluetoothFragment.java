package com.ramimartin.multibluetooth.fragment;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ramimartin.multibluetooth.bluetooth.manager.BluetoothManager;
import com.ramimartin.multibluetooth.bus.BluetoothCommunicatorBytes;
import com.ramimartin.multibluetooth.bus.BluetoothCommunicatorObject;
import com.ramimartin.multibluetooth.bus.BluetoothCommunicatorString;
import com.ramimartin.multibluetooth.bus.BondedDevice;
import com.ramimartin.multibluetooth.bus.ClientConnectionFail;
import com.ramimartin.multibluetooth.bus.ClientConnectionSuccess;
import com.ramimartin.multibluetooth.bus.ServeurConnectionFail;
import com.ramimartin.multibluetooth.bus.ServeurConnectionSuccess;

import de.greenrobot.event.EventBus;

/**
 * Created by Rami MARTIN on 13/04/2014.
 */
public abstract class BluetoothFragment extends Fragment {

    protected BluetoothManager mBluetoothManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBluetoothManager = new BluetoothManager(getActivity());
        checkBluetoothAviability();
        mBluetoothManager.setUUIDappIdentifier(setUUIDappIdentifier());
        mBluetoothManager.setNbrClientMax(myNbrClientMax());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!EventBus.getDefault().isRegistered(this))
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        closeAllConnexion();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothManager.REQUEST_DISCOVERABLE_CODE) {
            if (resultCode == BluetoothManager.BLUETOOTH_REQUEST_REFUSED) {
                getActivity().finish();
            } else if (resultCode == BluetoothManager.BLUETOOTH_REQUEST_ACCEPTED) {
                onBluetoothStartDiscovery();
            } else {
                getActivity().finish();
            }
        }
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

    public boolean isConnected(){
        return mBluetoothManager.isConnected;
    }

    public void scanAllBluetoothDevice(){
        mBluetoothManager.scanAllBluetoothDevice();
    }

    public void disconnectClient(){
        mBluetoothManager.disconnectClient(true);
    }

    public void disconnectServer(){
        mBluetoothManager.disconnectServer(true);
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

    public void setMessageMode(BluetoothManager.MessageMode messageMode){
        mBluetoothManager.setMessageMode(messageMode);
    }

    public void sendMessageStringToAll(String message){
        mBluetoothManager.sendStringMessageForAll(message);
    }
    public void sendMessageString(String adressMacTarget, String message){
        mBluetoothManager.sendStringMessage(adressMacTarget, message);
    }
    public void sendMessageObjectToAll(Object message){
        mBluetoothManager.sendObjectForAll(message);
    }
    public void sendMessageObject(String adressMacTarget, Object message){
        mBluetoothManager.sendObject(adressMacTarget, message);
    }
    public void sendMessageBytesForAll(byte[] message){
        mBluetoothManager.sendBytesForAll(message);
    }
    public void sendMessageBytes(String adressMacTarget, byte[] message){
        mBluetoothManager.sendBytes(adressMacTarget, message);
    }

    public abstract String setUUIDappIdentifier();
    public abstract int myNbrClientMax();
    public abstract void onBluetoothDeviceFound(BluetoothDevice device);
    public abstract void onClientConnectionSuccess();
    public abstract void onClientConnectionFail();
    public abstract void onServeurConnectionSuccess();
    public abstract void onServeurConnectionFail();
    public abstract void onBluetoothStartDiscovery();
    public abstract void onBluetoothMsgStringReceived(String message);
    public abstract void onBluetoothMsgObjectReceived(Object message);
    public abstract void onBluetoothMsgBytesReceived(byte[] message);
    public abstract void onBluetoothNotAviable();

    public void onEventMainThread(BluetoothDevice device){
        if(!mBluetoothManager.isNbrMaxReached()){
            onBluetoothDeviceFound(device);
            createServeur(device.getAddress());
        }
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

    public void onEventMainThread(BluetoothCommunicatorString event){
        onBluetoothMsgStringReceived(event.mMessageReceive);
    }

    public void onEventMainThread(BluetoothCommunicatorObject event){
        onBluetoothMsgObjectReceived(event.mObject);
    }

    public void onEventMainThread(BluetoothCommunicatorBytes event){
        onBluetoothMsgBytesReceived(event.mBytesReceive);
    }

    public void onEventMainThread(BondedDevice event){
        //mBluetoothManager.sendMessage("BondedDevice");
    }
}
