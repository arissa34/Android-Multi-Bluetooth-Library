package com.ramimartin.multibluetooth.bluetooth.manager;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.util.Log;

import com.ramimartin.multibluetooth.bluetooth.client.BluetoothClient;
import com.ramimartin.multibluetooth.bluetooth.server.BluetoothServer;
import com.ramimartin.multibluetooth.bus.BondedDevice;
import com.ramimartin.multibluetooth.bus.NbrMaxConnected;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

/**
 * Created by Rami MARTIN on 13/04/2014.
 */
public class BluetoothManager extends BroadcastReceiver {

    public enum TypeBluetooth {
        Client,
        Server,
        None;
    }
    public enum MessageMode {
        Serialized,
        String,
        Bytes;
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
    private ExecutorService mMessageSenderQueue;

    private ArrayList<String> mAdressListServerWaitingConnection;
    private HashMap<String, BluetoothServer> mServeurWaitingConnectionList;
    private ArrayList<BluetoothServer> mServeurConnectedList;
    private HashMap<String, Thread> mServeurThreadList;
    private SerialExecutor mSerialExecutor;
    private int mNbrClientConnection;
    public TypeBluetooth mType;
    private int mTimeDiscoverable;
    public boolean isConnected;
    private boolean mBluetoothIsEnableOnStart;
    private String mBluetoothNameSaved;
    private String mUuiDappIdentifier;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private BluetoothManager.MessageMode mMessageMode;
    private boolean mIsTimerCanceled;
    private Thread mThreadClient;

    public BluetoothManager(Activity activity) {
        mActivity = activity;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothNameSaved = mBluetoothAdapter.getName();
        mBluetoothIsEnableOnStart = mBluetoothAdapter.isEnabled();
        mType = TypeBluetooth.None;
        isConnected = false;
        mNbrClientConnection = 0;
        mMessageMode = MessageMode.Bytes;
        mAdressListServerWaitingConnection = new ArrayList<>();
        mServeurWaitingConnectionList = new HashMap<>();
        mServeurConnectedList = new ArrayList<>();
        mServeurThreadList = new HashMap<>();
        mSerialExecutor = new SerialExecutor(Executors.newSingleThreadExecutor());
    }

    public void setUUIDappIdentifier(String uuiDappIdentifier) {
        mUuiDappIdentifier = uuiDappIdentifier;
    }

    public void setMessageMode(MessageMode messageMode) {
        this.mMessageMode = messageMode;
    }

    public void selectServerMode() {
        mType = TypeBluetooth.Server;
        setServerBluetoothName();
    }

    private void setServerBluetoothName() {
        Resources res = mActivity.getResources();
        int placeLeft = (getNbrClientMax() - mNbrClientConnection);
        String appName = getApplicationName();
        //String blthName = res.getQuantityString(R.plurals.blth_name, placeLeft, placeLeft, android.os.Build.MODEL, appName);
        //mBluetoothAdapter.setName(blthName);
        mBluetoothAdapter.setName("Server " + (getNbrClientMax() - mNbrClientConnection) + " places available " + android.os.Build.MODEL);
    }

    public String getApplicationName() {
        ApplicationInfo applicationInfo = mActivity.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : mActivity.getString(stringId);
    }

    public void selectClientMode() {
        mType = TypeBluetooth.Client;
    }

    public void resetMode() {
        mType = TypeBluetooth.None;
    }

    public String getYourBtMacAddress() {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.getAddress().equals("02:00:00:00:00:00")) {
                return android.provider.Settings.Secure.getString(mActivity.getContentResolver(), "bluetooth_address");
            }
            return mBluetoothAdapter.getAddress();
        }
        return null;
    }

    public void setNbrClientMax(int nbrClientMax) {
        if (nbrClientMax <= BLUETOOTH_NBR_CLIENT_MAX) {
            BLUETOOTH_NBR_CLIENT_MAX = nbrClientMax;
        }
    }

    public int getNbrClientMax() {
        return BLUETOOTH_NBR_CLIENT_MAX;
    }

    public boolean isNbrMaxReached() {
        return mNbrClientConnection == getNbrClientMax();
    }

    public void incrementNbrConnection() {
        mNbrClientConnection = mNbrClientConnection + 1;
        Log.e("", "===> incrementNbrConnection mNbrClientConnection : " + mNbrClientConnection);
        setServerBluetoothName();
        if (mNbrClientConnection == getNbrClientMax()) {
            Log.e("", "===> incrementNbrConnection mNbrClientConnection OK");
            EventBus.getDefault().post(new NbrMaxConnected());
            resetAllOtherWaitingThreadServer();
        }
    }

    public void decrementNbrConnection() {
        if (mNbrClientConnection == 0) {
            return;
        }
        mNbrClientConnection = mNbrClientConnection - 1;
        if (mNbrClientConnection == 0) {
            isConnected = false;
        }
        Log.e("", "===> decrementNbrConnection mNbrClientConnection : " + mNbrClientConnection);
        setServerBluetoothName();
    }

    public void setTimeDiscoverable(int timeInSec) {
        mTimeDiscoverable = timeInSec;
        BLUETOOTH_REQUEST_ACCEPTED = mTimeDiscoverable;
    }

    public boolean checkBluetoothAviability() {
        if (mBluetoothAdapter == null) {
            return false;
        } else {
            return true;
        }
    }

    public void cancelDiscovery() {
        mIsTimerCanceled = false;
        if (isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            cancelDiscoveryTimer();
        }
    }

    public boolean isDiscovering() {
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

    public void scanAllBluetoothDeviceForServer() {
        mIsTimerCanceled = true;
        if(null == mTimerTask){
            mTimerTask = createTimer();
        }else{
            mTimerTask.run();
        }
        if (mTimer != null) {
            return;
        }
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(mTimerTask, 0, 4000);
    }

    public TimerTask createTimer(){
        return new TimerTask() {

            @Override
            public void run() {
                if (!mIsTimerCanceled) {
                    cancelDiscoveryTimer();
                }
                Log.e("", "===> TimerTask startDiscovery");
                if(mBluetoothAdapter != null){
                    mBluetoothAdapter.startDiscovery();
                }
            }
        };
    }


    public void cancelDiscoveryTimer() {
        if (mTimer == null) {
            return;
        }
        mTimerTask = null;
        mTimer.cancel();
        mTimer = null;
    }

    public BluetoothManager.TypeBluetooth getTypeBluetooth() {
        return mType;
    }

    private void resetAllOtherWaitingThreadServer() {
        cancelDiscovery();
        for (Iterator<Map.Entry<String, BluetoothServer>> it = mServeurWaitingConnectionList.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, BluetoothServer> bluetoothServerMap = it.next();
            if (!bluetoothServerMap.getValue().isConnected()) {
                Log.e("", "===> resetWaitingThreadServer BluetoothServer : " + bluetoothServerMap.getKey());
                bluetoothServerMap.getValue().closeConnection();
                Thread serverThread = mServeurThreadList.get(bluetoothServerMap.getKey());
                serverThread.interrupt();
                mServeurThreadList.remove(bluetoothServerMap.getKey());
                it.remove();
            }
        }
    }

    public void createClient(String addressMac) {
        if (mType == TypeBluetooth.Client) {
            IntentFilter bondStateIntent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            mActivity.registerReceiver(this, bondStateIntent);
            mBluetoothClient = new BluetoothClient(mBluetoothAdapter, mUuiDappIdentifier, addressMac, mActivity, mMessageMode);
            mThreadClient = new Thread(mBluetoothClient);
            mThreadClient.start();
        }
    }

    public void onClientConnectionSuccess(){
        if (mType == TypeBluetooth.Client) {
            isConnected = true;
            cancelDiscovery();
        }
    }

    public boolean createServeur(String address) {
        if (mType == TypeBluetooth.Server && !mAdressListServerWaitingConnection.contains(address)) {
            BluetoothServer mBluetoothServer = new BluetoothServer(mBluetoothAdapter, mUuiDappIdentifier, address, mActivity, mMessageMode);
            Thread threadServer = new Thread(mBluetoothServer);
            threadServer.start();
            setServerWaitingConnection(address, mBluetoothServer, threadServer);
            IntentFilter bondStateIntent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            mActivity.registerReceiver(this, bondStateIntent);
            return true;
        } else {
            return false;
        }
    }

    public void setServerWaitingConnection(String address, BluetoothServer bluetoothServer, Thread threadServer) {
        mAdressListServerWaitingConnection.add(address);
        mServeurWaitingConnectionList.put(address, bluetoothServer);
        mServeurThreadList.put(address, threadServer);
    }

    public void onServerConnectionSuccess(String addressClientConnected) {
        for (Map.Entry<String, BluetoothServer> bluetoothServerMap : mServeurWaitingConnectionList.entrySet()) {
            if (addressClientConnected.equals(bluetoothServerMap.getValue().getClientAddress())) {
                isConnected = true;
                mServeurConnectedList.add(bluetoothServerMap.getValue());
                Log.e("", "===> onServerConnectionSuccess address : " + addressClientConnected);
                incrementNbrConnection();
                return;
            }
        }
    }

    public void onServerConnectionFailed(String addressClientConnectionFailed) {
        int index = 0;
        for (BluetoothServer bluetoothServer : mServeurConnectedList) {
            if (addressClientConnectionFailed.equals(bluetoothServer.getClientAddress())) {
                mServeurConnectedList.get(index).closeConnection();
                mServeurConnectedList.remove(index);
                mServeurWaitingConnectionList.get(addressClientConnectionFailed).closeConnection();
                mServeurWaitingConnectionList.remove(addressClientConnectionFailed);
                mServeurThreadList.get(addressClientConnectionFailed).interrupt();
                mServeurThreadList.remove(addressClientConnectionFailed);
                mAdressListServerWaitingConnection.remove(addressClientConnectionFailed);
                decrementNbrConnection();
                Log.e("", "===> onServerConnectionFailed address : " + addressClientConnectionFailed);
                return;
            }
            index++;
        }
    }

    public synchronized void sendStringMessageForAll(final String message) {
        Log.e("", "===> sendMessageForAll ");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mType != null && isConnected) {
                    if (mServeurConnectedList != null) {
                        for (BluetoothServer bluetoothServer : mServeurConnectedList) {
                            bluetoothServer.writeString(message);
                        }
                    }
                    if (mBluetoothClient != null) {
                        mBluetoothClient.writeString(message);
                    }
                }
            }
        };
        mSerialExecutor.execute(runnable);
    }

    public void sendObjectForAll(final Object obj) {
        Log.e("", "===> sendObjectForAll ");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if (mType != null && isConnected) {
                    if (mServeurConnectedList != null) {
                        for (int i = 0; i < mServeurConnectedList.size(); i++) {
                            mServeurConnectedList.get(i).writeSerialized(obj);
                        }
                    }
                    if (mBluetoothClient != null) {
                        mBluetoothClient.writeSerialized(obj);
                    }
                }
            }
        };
        mSerialExecutor.execute(runnable);
    }

    public void sendBytesForAll(final byte[] message) {
        Log.e("", "===> sendBytesForAll ");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if (mType != null && isConnected) {
                    if (mServeurConnectedList != null) {
                        for (int i = 0; i < mServeurConnectedList.size(); i++) {
                            mServeurConnectedList.get(i).writeBytes(message);
                        }
                    }
                    if (mBluetoothClient != null) {
                        mBluetoothClient.writeBytes(message);
                    }
                }
            }
        };
        mSerialExecutor.execute(runnable);
    }

    public void sendStringMessage(final String adressMacTarget, final String message) {
        Log.e("", "===> sendMessage ");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mType != null && isConnected) {
                    if (mServeurConnectedList != null) {
                        for (BluetoothServer bluetoothServer : mServeurConnectedList) {
                            if (bluetoothServer.getClientAddress().equals(adressMacTarget)) {
                                bluetoothServer.writeString(message);
                            }
                        }
                    }
                    if (mBluetoothClient != null) {
                        mBluetoothClient.writeString(message);
                    }
                }
            }
        };
        mSerialExecutor.execute(runnable);
    }

    public void sendObject(final String adressMacTarget, final Object obj) {
        Log.e("", "===> sendObject ");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mType != null && isConnected) {
                    if (mServeurConnectedList != null) {
                        for (BluetoothServer bluetoothServer : mServeurConnectedList) {
                            if (bluetoothServer.getClientAddress().equals(adressMacTarget)) {
                                bluetoothServer.writeSerialized(obj);
                            }
                        }
                    }
                    if (mBluetoothClient != null) {
                        mBluetoothClient.writeSerialized(obj);
                    }
                }
            }
        };
        mSerialExecutor.execute(runnable);
    }

    public void sendBytes(final String adressMacTarget, final byte[] message) {
        Log.e("", "===> sendObject ");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mType != null && isConnected) {
                    if (mServeurConnectedList != null) {
                        for (BluetoothServer bluetoothServer : mServeurConnectedList) {
                            if (bluetoothServer.getClientAddress().equals(adressMacTarget)) {
                                bluetoothServer.writeBytes(message);
                            }
                        }
                    }
                    if (mBluetoothClient != null) {
                        mBluetoothClient.writeBytes(message);
                    }
                }
            }
        };
        mSerialExecutor.execute(runnable);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("", "===> onReceive for BluetoothDevice ");
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
            if ((mType == TypeBluetooth.Client && !isConnected)
                    || (mType == TypeBluetooth.Server && !mAdressListServerWaitingConnection.contains(device.getAddress()))) {

                EventBus.getDefault().post(device);
            }
        }
        if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
            //Log.e("", "===> ACTION_BOND_STATE_CHANGED");
            int prevBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);
            int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
            if (prevBondState == BluetoothDevice.BOND_BONDING) {
                // check for both BONDED and NONE here because in some error cases the bonding fails and we need to fail gracefully.
                if (bondState == BluetoothDevice.BOND_BONDED || bondState == BluetoothDevice.BOND_NONE) {
                    //Log.e("", "===> BluetoothDevice.BOND_BONDED");
                    EventBus.getDefault().post(new BondedDevice());
                }
            }
        }
        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {

        }

    }

    public void disconnectClient(boolean cancelDiscovery) {
        mType = TypeBluetooth.None;
        if(cancelDiscovery) cancelDiscovery();
        resetClient();
    }

    public void disconnectServer(boolean cancelDiscovery) {
        resetMode();
        if(cancelDiscovery) cancelDiscovery();
        resetAllThreadServer();
        isConnected = false;
    }

    public void resetClient() {
        if (mBluetoothClient != null) {
            mBluetoothClient.closeConnection();
            if(null != mThreadClient){
                mThreadClient.interrupt();
            }
            mBluetoothClient = null;
            isConnected = false;
        }
    }

    public void resetAllThreadServer(){
        for (Iterator<Map.Entry<String, BluetoothServer>> it = mServeurWaitingConnectionList.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, BluetoothServer> bluetoothServerMap = it.next();
            bluetoothServerMap.getValue().closeConnection();
            Thread serverThread = mServeurThreadList.get(bluetoothServerMap.getKey());
            serverThread.interrupt();
            mServeurThreadList.remove(bluetoothServerMap.getKey());
            it.remove();
        }
        mServeurConnectedList.clear();
        mAdressListServerWaitingConnection.clear();
        mServeurWaitingConnectionList.clear();
        mServeurThreadList.clear();
        mNbrClientConnection = 0;
    }

    public void closeAllConnexion() {
        mBluetoothAdapter.setName(mBluetoothNameSaved);

        cancelDiscovery();

        if (mType != null) {
            resetAllThreadServer();
            resetClient();
        }

        try {
            mActivity.unregisterReceiver(this);
        } catch (Exception e) {
        }

        if (!mBluetoothIsEnableOnStart) {
            mBluetoothAdapter.disable();
        }

        mBluetoothAdapter = null;
    }
}