package com.ramimartin.sample.multibluetooth;

import android.bluetooth.BluetoothDevice;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.ramimartin.sample.multibluetooth.R;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by Rami on 29/03/2015.
 */
public class DiscoveredDialogFragment extends DialogFragment {

    public static DiscoveredDialogFragment newInstance() {
        return new DiscoveredDialogFragment();
    }

    public interface DiscoveredDialogListener {
        public void onDeviceSelectedForConnection(String addressMac);

        public void onScanClicked();
    }

    @InjectView(R.id.listview)
    ListView mListView;
    private SimpleAdapter mAdapter;
    private ArrayList<HashMap<String, String>> mListDevice;
    private DiscoveredDialogListener mDiscoveredDialogListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_devices_discovered, null);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
        getDialog().setCanceledOnTouchOutside(false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListDevice = new ArrayList<HashMap<String, String>>();
        mAdapter = new SimpleAdapter(getActivity(), mListDevice,
                android.R.layout.two_line_list_item,
                new String[]{"Name", "AddressMac"},
                new int[]{android.R.id.text1, android.R.id.text2});
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mDiscoveredDialogListener.onDeviceSelectedForConnection(mListDevice.get(position).get("AddressMac"));
                dismiss();
            }
        });
    }

    public void setListener(DiscoveredDialogListener discoveredDialogListener) {
        mDiscoveredDialogListener = discoveredDialogListener;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.scan)
    public void scan() {
        mDiscoveredDialogListener.onScanClicked();
    }

    public void onEventMainThread(BluetoothDevice device) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("Name", device.getName());
        item.put("AddressMac", device.getAddress());
        if(!mListDevice.contains(item)){
            mListDevice.add(item);
        }

        mAdapter.notifyDataSetChanged();
    }
}
