package gattaca.digitalhealth.com.sensoremulationapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by as on 05.03.2016.
 */
public class BluetoothSender {
    BluetoothAdapter mBluetoothAdapter;
    static final String LOG_TAG = BluetoothSender.class.getSimpleName();
    Context mContext;
    Activity mActivity;

    BluetoothSender(Context context) {
        mContext = context;
    }

    void send(byte[] data) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(LOG_TAG, "Device does not support Bluetooth");
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //mContext.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }

    }
}
