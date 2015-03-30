package com.ramimartin.sample.multibluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.ramimartin.multibluetooth.activity.BluetoothFragmentActivity;
import com.ramimartin.multibluetooth.bluetooth.mananger.BluetoothManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends BluetoothFragmentActivity implements DiscoveredDialogFragment.DiscoveredDialogListener {

    @InjectView(R.id.listview)
    ListView mListView;
    ArrayAdapter<String> mAdapter;
    List<String> mListLog;

    @InjectView(R.id.communication)
    EditText mEditText;
    @InjectView(R.id.send)
    ImageButton mSendBtn;

    @InjectView(R.id.client)
    ToggleButton mClientToggleBtn;
    @InjectView(R.id.serveur)
    ToggleButton mServerToggleBtn;

    @InjectView(R.id.connect)
    Button mConnectBtn;
    @InjectView(R.id.disconnect)
    Button mDisconnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mListLog = new ArrayList<String>();
        mAdapter = new ArrayAdapter<String>(this, R.layout.item_console, mListLog);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public int myNbrClientMax() {
        return 7;
    }

    @OnClick(R.id.serveur)
    public void serverType() {
        setLogText("===> Start Server ! Your mac address : " + mBluetoothManager.getYourBtMacAddress());
        setTimeDiscoverable(BluetoothManager.BLUETOOTH_TIME_DICOVERY_3600_SEC);
        selectServerMode();
        mServerToggleBtn.setChecked(true);
        mClientToggleBtn.setChecked(false);
        mConnectBtn.setEnabled(true);
        mConnectBtn.setText("Scan Devices");
    }

    @OnClick(R.id.client)
    public void clientType() {
        setLogText("===> Start Client ! Your mac address : " + mBluetoothManager.getYourBtMacAddress());
        setTimeDiscoverable(BluetoothManager.BLUETOOTH_TIME_DICOVERY_120_SEC);
        selectClientMode();
        mServerToggleBtn.setChecked(false);
        mClientToggleBtn.setChecked(true);
        mConnectBtn.setEnabled(true);
    }

    @OnClick(R.id.connect)
    public void connect() {
        setLogText("===> Start Scanning devices ...");
        if (getTypeBluetooth() == BluetoothManager.TypeBluetooth.Client) {
            showDiscoveredDevicesDialog();
        }
        scanAllBluetoothDevice();
    }

    @OnClick(R.id.disconnect)
    public void disconnect() {
        setLogText("===> Disconnect");
        disconnectClient();
        disconnectServer();
    }

    @OnClick(R.id.send)
    public void send() {
        if (isConnected()) {
            sendMessage(mEditText.getText().toString());
            setLogText("===> Send : " + mEditText.getText().toString());
        }
    }

    @Override
    public void onBluetoothStartDiscovery() {
        setLogText("===> Start discovering ! Your mac address : " + mBluetoothManager.getYourBtMacAddress());
    }

    @Override
    public void onBluetoothDeviceFound(BluetoothDevice device) {
        if(getTypeBluetooth() == BluetoothManager.TypeBluetooth.Server) {
            setLogText("===> Device detected and Thread Server created for this address : " + device.getAddress());
        }else{
            setLogText("===> Device detected : "+ device.getAddress());
        }
    }

    @Override
    public void onClientConnectionSuccess() {
        setLogText("===> Client Connexion success !");
        mEditText.setText("Client");
        mSendBtn.setEnabled(true);
        mConnectBtn.setEnabled(false);
        mDisconnect.setEnabled(true);
    }

    @Override
    public void onClientConnectionFail() {
        setLogText("===> Client Connexion fail !");
        mServerToggleBtn.setChecked(false);
        mClientToggleBtn.setChecked(false);
        mDisconnect.setEnabled(false);
        mConnectBtn.setEnabled(false);
        mConnectBtn.setText("Connect");
        mEditText.setText("");
    }

    @Override
    public void onServeurConnectionSuccess() {
        setLogText("===> Serveur Connexion success !");
        mEditText.setText("Server");
        mDisconnect.setEnabled(true);
    }

    @Override
    public void onServeurConnectionFail() {
        setLogText("===> Serveur Connexion fail !");
    }

    @Override
    public void onBluetoothCommunicator(String messageReceive) {
        setLogText("===> receive msg : " + messageReceive);
    }

    @Override
    public void onBluetoothNotAviable() {
        setLogText("===> Bluetooth not aviable on this device");
        mSendBtn.setEnabled(false);
        mClientToggleBtn.setEnabled(false);
        mServerToggleBtn.setEnabled(false);
        mConnectBtn.setEnabled(false);
        mDisconnect.setEnabled(false);
    }

    public void setLogText(String text) {
        mListLog.add(text);
        mAdapter.notifyDataSetChanged();
        mListView.setSelection(mListView.getCount() - 1);
    }

    private void showDiscoveredDevicesDialog() {
        String tag = DiscoveredDialogFragment.class.getSimpleName();
        DiscoveredDialogFragment fragment = DiscoveredDialogFragment.newInstance();
        fragment.setListener(this);
        showDialogFragment(fragment, tag);
    }

    private void showDialogFragment(DialogFragment dialogFragment, String tag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(dialogFragment, tag);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onDeviceSelectedForConnection(String addressMac) {
        setLogText("===> Connect to " + addressMac);
        createClient(addressMac);
    }

    @Override
    public void onScanClicked() {
        scanAllBluetoothDevice();
    }
}
