package com.ramimartin.multibluetooth.bluetooth.mananger;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.ramimartin.multibluetooth.bluetooth.client.BluetoothClient;
import com.ramimartin.multibluetooth.bluetooth.server.BluetoothServer;
import com.ramimartin.multibluetooth.bus.BondedDevice;

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
        Server,
        None;
    }

    public static final int REQUEST_DISCOVERABLE_CODE = 114;

    public static int BLUETOOTH_REQUEST_ACCEPTED;
    public static final int BLUETOOTH_REQUEST_REFUSED = 0; // NE PAS MODIFIER LA VALEUR

    public static final int BLUETOOTH_TIME_DICOVERY_60_SEC = 60;
    public static final int BLUETOOTH_TIME_DICOVERY_120_SEC = 120;
    public static final int BLUETOOTH_TIME_DICOVERY_300_SEC = 300;
    public static final int BLUETOOTH_TIME_DICOVERY_600_SEC = 600;
    public static final int BLUETOOTH_TIME_DICOVERY_900_SEC = 900;
    public static final int BLUETOOTH_TIME_DICOVERY_1200_SEC = 1200;
    public static final int BLUETOOTH_TIME_DICOVERY_3600_SEC = 3600;

    private static int BLUETOOTH_NBR_CLIENT_MAX = 7;

    private Activity mActivity;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothClient mBluetoothClient;

    private ArrayList<String> mAdressListServerWaitingConnection;
    private HashMap<String, BluetoothServer> mServeurWaitingConnectionList;
    private ArrayList<BluetoothServer> mServeurConnectedList;
    private HashMap<String, Thread> mServeurThreadList;
    private int mNbrClientConnection;
    public TypeBluetooth mType;
    private int mTimeDiscoverable;
    public boolean isConnected;
    private boolean mBluetoothIsEnableOnStart;
    private String mBluetoothNameSaved;

    public BluetoothManager(Activity activity) {
        mActivity = activity;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothNameSaved = mBluetoothAdapter.getName();
        mBluetoothIsEnableOnStart = mBluetoothAdapter.isEnabled();
        mType = TypeBluetooth.None;
        isConnected = false;
        mNbrClientConnection = 0;
        mAdressListServerWaitingConnection = new ArrayList<String>();
        mServeurWaitingConnectionList = new HashMap<String, BluetoothServer>();
        mServeurConnectedList = new ArrayList<BluetoothServer>();
        mServeurThreadList = new HashMap<String, Thread>();
        //setTimeDiscoverable(BLUETOOTH_TIME_DICOVERY_300_SEC);
    }

    public void selectServerMode(){
        startDiscovery();
        mType = TypeBluetooth.Server;
        setServerBluetoothName();
    }

    private void setServerBluetoothName(){
        mBluetoothAdapter.setName("Server " + (getNbrClientMax()-mNbrClientConnection) + " places available " + android.os.Build.MODEL);
    }

    public void selectClientMode(){
        startDiscovery();
        mType = TypeBluetooth.Client;
        mBluetoothAdapter.setName("Client "+android.os.Build.MODEL);
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

    public boolean isNbrMaxReached(){
        return mNbrClientConnection == getNbrClientMax();
    }

    public void setServerWaitingConnection(String address, BluetoothServer bluetoothServer, Thread threadServer){
        mAdressListServerWaitingConnection.add(address);
        mServeurWaitingConnectionList.put(address, bluetoothServer);
        mServeurThreadList.put(address, threadServer);
    }

    public void incrementNbrConnection(){
        mNbrClientConnection = mNbrClientConnection +1;
        setServerBluetoothName();
        if(mNbrClientConnection == getNbrClientMax()){
            //resetWaitingThreadServer();
        }
        Log.e("", "===> incrementNbrConnection mNbrClientConnection : "+mNbrClientConnection);
    }

    private void resetWaitingThreadServer(){
        for(Map.Entry<String, Thread> bluetoothThreadServerMap : mServeurThreadList.entrySet()){
            if(mAdressListServerWaitingConnection.contains(bluetoothThreadServerMap.getKey())){
                Log.e("", "===> resetWaitingThreadServer Thread : "+bluetoothThreadServerMap.getKey());
                bluetoothThreadServerMap.getValue().interrupt();
            }
        }
        for(Map.Entry<String, BluetoothServer> bluetoothServerMap : mServeurWaitingConnectionList.entrySet()){
            Log.e("", "===> resetWaitingThreadServer BluetoothServer : " + bluetoothServerMap.getKey());
            bluetoothServerMap.getValue().closeConnection();
            //mServeurThreadList.remove(bluetoothServerMap.getKey());
        }
        mAdressListServerWaitingConnection.clear();
        mServeurWaitingConnectionList.clear();
    }

    public void decrementNbrConnection(){
        if(mNbrClientConnection ==0){
            return;
        }
        mNbrClientConnection = mNbrClientConnection -1;
        if(mNbrClientConnection ==0){
            isConnected = false;
        }
        Log.e("", "===> decrementNbrConnection mNbrClientConnection : "+mNbrClientConnection);
        setServerBluetoothName();
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
        if(isDiscovering()) {
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
            if (mBluetoothAdapter.isEnabled() && isDiscovering()) {
                Log.e("", "===> mBluetoothAdapter.isDiscovering()");
                return;
            } else {
                Log.e("", "===> startDiscovery");
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
        if(mType == TypeBluetooth.Client) {
            IntentFilter bondStateIntent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            mActivity.registerReceiver(this, bondStateIntent);
            mBluetoothClient = new BluetoothClient(mBluetoothAdapter, addressMac);
            new Thread(mBluetoothClient).start();
        }
    }

    public void createServeur(String address){
        if(mType == TypeBluetooth.Server && !mAdressListServerWaitingConnection.contains(address)) {
            BluetoothServer mBluetoothServer = new BluetoothServer(mBluetoothAdapter, address);
            Thread threadServer = new Thread(mBluetoothServer);
            threadServer.start();
            setServerWaitingConnection(address, mBluetoothServer, threadServer);
            IntentFilter bondStateIntent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            mActivity.registerReceiver(this, bondStateIntent);
            Log.e("", "===> createServeur address : " + address);
        }
    }

    public void onServerConnectionSuccess(String addressClientConnected){
        for(Map.Entry<String, BluetoothServer> bluetoothServerMap : mServeurWaitingConnectionList.entrySet()){
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

    public void sendMessage(String message) {
        if(mType != null && isConnected){
            if(mServeurConnectedList!= null){
                for(int i=0; i < mServeurConnectedList.size(); i++){
                    mServeurConnectedList.get(i).write(message);
                }
            }
            if(mBluetoothClient != null){
                mBluetoothClient.write(message);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
            if((mType == TypeBluetooth.Client && !isConnected)
                    || (mType == TypeBluetooth.Server && !mAdressListServerWaitingConnection.contains(device.getAddress()))){

                EventBus.getDefault().post(device);
            }
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

    public void disconnectClient(){
        mType = TypeBluetooth.None;
        cancelDiscovery();
        resetClient();
    }

    public void disconnectServer(){
        mType = TypeBluetooth.None;
        cancelDiscovery();
        resetServer();
    }

    public void resetServer(){
        if(mServeurConnectedList != null){
            for(int i=0; i < mServeurConnectedList.size(); i++) {
                mServeurConnectedList.get(i).closeConnection();
            }
        }
        mServeurConnectedList.clear();
    }

    public void resetClient(){
        if(mBluetoothClient != null){
            mBluetoothClient.closeConnexion();
            mBluetoothClient = null;
        }
    }

    public void closeAllConnexion(){
        mBluetoothAdapter.setName(mBluetoothNameSaved);

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
