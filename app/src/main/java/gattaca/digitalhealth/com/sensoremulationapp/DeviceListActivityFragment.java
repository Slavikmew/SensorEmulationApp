package gattaca.digitalhealth.com.sensoremulationapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;


/**
 * A placeholder fragment containing a simple view.
 */
public class DeviceListActivityFragment extends Fragment {
    BluetoothAdapter mBluetoothAdapter;
    ArrayAdapter<String> mViewAdapter;
    static final String LOG_TAG = DeviceListActivityFragment.class.getSimpleName();
    private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 2;
    final static UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    MyHandler mHandler;
    BluetoothSocket mSocket;
    ConnectedThread communicationThread;
    DataAtom data;

    public class MyHandler extends Handler {
        public void handleMessage (Message msg) {
            //!! написать обработчик принятия сообщения
        }
    }

    public DeviceListActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_device_list, container, false);
        ListView deviceList = (ListView)rootView.findViewById(R.id.deviceListView);
        mViewAdapter = new ArrayAdapter<String>(getActivity(), R.layout.textview, new ArrayList<String>());
        deviceList.setAdapter(mViewAdapter);
        data = (DataAtom)getActivity().getIntent().getSerializableExtra("gattaca.digitalhealth.com.sensoremulationapp.DataAtom");
        //Toast.makeText(getActivity(), data.getPulse() + " " + data.getTemperatute(), Toast.LENGTH_LONG);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(LOG_TAG, "Device does not support Bluetooth");
            return rootView;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        mHandler = new MyHandler();

        getPermission();
        (new AcceptThread()).start();
        //BluetoothSender

        return rootView;
    }

    void showToast(String s){
        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e(LOG_TAG, "We want to start discovery");
                } else {
                    showToast("WTF??");
                }
                return;
            }
        }
    }

    public void getPermission() {
        int hasPermission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION_PERMISSIONS);
        }/* else {
            mBluetoothAdapter.startDiscovery();
        }*/
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        static final String NAME = "Great_server";
        DataAtom mData;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    mSocket = socket;
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Unable to close server socket");
                    }
                    communicationThread = new ConnectedThread(mSocket);
                    //communicationThread.start();
                    //DataAtom mData = new DataAtom(36.6, 60);
                    mData = data;
                    communicationThread.write(mData.getBytes());

                    try {
                        mmServerSocket.close();
                    } catch(IOException e) {}

                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        static final int JUST_READ = 322;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(JUST_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}

