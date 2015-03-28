package com.ramimartin.bluetooth.bluetooth.mananger;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.ramimartin.bluetooth.bluetooth.client.BluetoothClient;
import com.ramimartin.bluetooth.bluetooth.server.BluetoothServer;
import com.ramimartin.bluetooth.BuildConfig;
import com.ramimartin.bluetooth.bus.BondedDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by Rami MARTIN on 13/04/2014.
 */
public class BluetoothManager extends BroadcastReceiver {

    public enum TypeBluetooth{
        Client,
        Server;
    }

    public static final int REQUEST_DISCOVERABLE_CODE = 114;

    public static int BLUETOOTH_REQUEST_ACCEPTED;
    public static final int BLUETOOTH_REQUEST_REFUSED = 0; // NE PAS MODIFIER LA VALEUR

    public static final int BLUETOOTH_TIME_DICOVERY_60_SEC = 60;
    public static final int BLUETOOTH_TIME_DICOVERY_120_SEC = 120;
    public static final int BLUETOOTH_TIME_DICOVERY_300_SEC = 300;

    private static int BLUETOOTH_NBR_CLIENT_MAX = 7;

    private Activity mActivity;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothClient mBluetoothClient;
    private BluetoothServer mBluetoothServer;

    private ArrayList<String> mAdressListServerWaitingConnection;
    private HashMap<String, BluetoothServer> mServeurWaitingConnectionList;
    private ArrayList<BluetoothServer> mServeurConnectedList;
    private HashMap<String, Thread> mServeurThreadList;
    private int mNbrClientConnection;
    public TypeBluetooth mType;
    private int mTimeDiscoverable;
    public boolean isConnected;
    private boolean mBluetoothIsEnableOnStart;

    public BluetoothManager(Activity activity) {
        mActivity = activity;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothIsEnableOnStart = mBluetoothAdapter.isEnabled();
        isConnected = false;
        mNbrClientConnection = 0;
        mAdressListServerWaitingConnection = new ArrayList<String>();
        mServeurWaitingConnectionList = new HashMap<String, BluetoothServer>();
        mServeurConnectedList = new ArrayList<BluetoothServer>();
        mServeurThreadList = new HashMap<String, Thread>();
        setTimeDiscoverable(BLUETOOTH_TIME_DICOVERY_300_SEC);
    }

    public String getYourBtMacAddress(){
        if(mBluetoothAdapter != null){
            return mBluetoothAdapter.getAddress();
        }
        return null;
    }

    public void setNbrClientMax(int nbrClientMax){
        if(nbrClientMax <= BLUETOOTH_NBR_CLIENT_MAX){
            BLUETOOTH_NBR_CLIENT_MAX = nbrClientMax;
        }
    }

    public int getNbrClientMax(){
        return BLUETOOTH_NBR_CLIENT_MAX;
    }

    public void setServerWaitingConnection(String address, BluetoothServer bluetoothServer, Thread threadServer){
        mAdressListServerWaitingConnection.add(address);
        mServeurWaitingConnectionList.put(address, bluetoothServer);
        mServeurThreadList.put(address, threadServer);
    }

    public void incrementNbrConnection(){
        mNbrClientConnection = mNbrClientConnection +1;
        if(mNbrClientConnection == getNbrClientMax()){
            //TODO clear waiting list
        }
    }

    public void decrementNbrConnection(){
        if(mNbrClientConnection ==0){
            return;
        }
        mNbrClientConnection = mNbrClientConnection -1;
        if(mNbrClientConnection ==0){
            isConnected = false;
        }
    }

    public void setTimeDiscoverable(int timeInSec){
        mTimeDiscoverable = timeInSec;
        BLUETOOTH_REQUEST_ACCEPTED = mTimeDiscoverable;
    }

    public boolean checkBluetoothAviability(){
        if (mBluetoothAdapter == null) {
            return false;
        }else{
            return true;
        }
    }

    public void cancelDiscovery(){
        if(mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    public boolean isDiscovering(){
        return mBluetoothAdapter.isDiscovering();
    }

    public void startDiscovery() {
        if (mBluetoothAdapter == null) {
            return;
        } else {
            if(BuildConfig.DEBUG) {
                Log.e("", " ==> mBluetoothAdapter.isEnabled() : " + mBluetoothAdapter.isEnabled());
                Log.e("", " ==> mBluetoothAdapter.isDiscovering() : " + mBluetoothAdapter.isDiscovering());
            }
            if (mBluetoothAdapter.isEnabled() && mBluetoothAdapter.isDiscovering()) {
                return;
            } else {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, mTimeDiscoverable);
                mActivity.startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_CODE);
            }
        }
    }

    public void scanAllBluetoothDevice() {
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mActivity.registerReceiver(this, intentFilter);
        mBluetoothAdapter.startDiscovery();
    }

    public void createClient(String addressMac) {
        mType = TypeBluetooth.Client;
        IntentFilter bondStateIntent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mActivity.registerReceiver(this, bondStateIntent);
        mBluetoothClient = new BluetoothClient(mBluetoothAdapter, addressMac);
        new Thread(mBluetoothClient).start();
    }

    public void createServeur(String address){
        if(mType == TypeBluetooth.Server && !mAdressListServerWaitingConnection.contains(address)) {
            Log.e("", "===> createServeur address : "+address);
            mBluetoothServer = new BluetoothServer(mBluetoothAdapter, address);
            Thread threadServer = new Thread(mBluetoothServer);
            threadServer.start();
            setServerWaitingConnection(address, mBluetoothServer, threadServer);
            IntentFilter bondStateIntent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            mActivity.registerReceiver(this, bondStateIntent);
        }else{
            Log.e("", "===> CANT createServeur address : "+address);
        }
    }

    public void onServerConnectionSuccess(String addressClientConnected){
        for(Map.Entry<String, BluetoothServer> bluetoothServerMap : mServeurWaitingConnectionList.entrySet()){
//        for(BluetoothServer bluetoothServer : mServeurWaitingConnectionList){
            if(addressClientConnected.equals(bluetoothServerMap.getValue().getClientAddress())){
                mServeurConnectedList.add(bluetoothServerMap.getValue());
                incrementNbrConnection();
                Log.e("", "===> onServerConnectionSuccess address : "+addressClientConnected);
                return;
            }
        }
    }

    public void onServerConnectionFailed(String addressClientConnectionFailed){
        int index = 0;
        for(BluetoothServer bluetoothServer : mServeurConnectedList){
            if(addressClientConnectionFailed.equals(bluetoothServer.getClientAddress())){
                mServeurConnectedList.get(index).closeConnection();
                mServeurConnectedList.remove(index);
                mServeurWaitingConnectionList.get(addressClientConnectionFailed).closeConnection();
                mServeurWaitingConnectionList.remove(addressClientConnectionFailed);
                mServeurThreadList.get(addressClientConnectionFailed).interrupt();
                mServeurThreadList.remove(addressClientConnectionFailed);
                mAdressListServerWaitingConnection.remove(addressClientConnectionFailed);
                decrementNbrConnection();
                Log.e("", "===> onServerConnectionFailed address : "+addressClientConnectionFailed);
                return;
            }
            index++;
        }
    }

    public void selectServerMode(){
        mType = TypeBluetooth.Server;
    }

    public void sendMessage(String message) {
        if(mType != null && isConnected){
            if(mType == TypeBluetooth.Server && mBluetoothServer != null){
                for(int i=0; i < mServeurConnectedList.size(); i++){
                    mServeurConnectedList.get(i).write(message);
                }
            }else if(mType == TypeBluetooth.Client && mBluetoothClient != null){
                mBluetoothClient.write(message);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            EventBus.getDefault().post(device);
        }
        if(intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
            //Log.e("", "===> ACTION_BOND_STATE_CHANGED");
            int prevBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);
            int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
            if (prevBondState == BluetoothDevice.BOND_BONDING)
            {
                // check for both BONDED and NONE here because in some error cases the bonding fails and we need to fail gracefully.
                if (bondState == BluetoothDevice.BOND_BONDED || bondState == BluetoothDevice.BOND_NONE )
                {
                    //Log.e("", "===> BluetoothDevice.BOND_BONDED");
                    EventBus.getDefault().post(new BondedDevice());
                }
            }
        }
    }

    public void resetServer(){
        if(mType == TypeBluetooth.Server && mBluetoothServer != null){
            for(int i=0; i < mServeurConnectedList.size(); i++) {
                mServeurConnectedList.get(i).closeConnection();
            }
        }
        mServeurConnectedList.clear();
    }

    public void resetClient(){
        if(mType == TypeBluetooth.Client && mBluetoothClient != null){
            mBluetoothClient.closeConnexion();
            mBluetoothClient = null;
        }
    }

    public void closeAllConnexion(){
        if(BuildConfig.DEBUG){
            Log.e("","===> Bluetooth Lib Destroy");
        }
        try{
            mActivity.unregisterReceiver(this);
        }catch(Exception e){}

        cancelDiscovery();

        if(!mBluetoothIsEnableOnStart){
            mBluetoothAdapter.disable();
        }

        mBluetoothAdapter = null;

        if(mType != null){
            resetServer();
            resetClient();
        }
    }
}
