package gattaca.digitalhealth.com.sensoremulationapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class SetDataFragment extends Fragment {
    EditText mBodyTemperatureSetView, mPulseSetView;
    Button mSendButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_set_data, container, false);
        mBodyTemperatureSetView = (EditText)rootView.findViewById(R.id.body_temperature_view);
        mPulseSetView = (EditText)rootView.findViewById(R.id.pulse_view);
        mSendButton = (Button)rootView.findViewById(R.id.send_button);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataAtom currentData = new DataAtom(Double.parseDouble(mBodyTemperatureSetView.getText().toString()), Integer.parseInt(mPulseSetView.getText().toString()));
                //String res = mBodyTemperatureSetView.getText().toString() + " " + mPulseSetView.getText().toString();
                DataAtom dataAfterSerialization = new DataAtom(currentData.getBytes());
                BluetoothSender sender = new BluetoothSender(getActivity());
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), dataAfterSerialization.getPulse() + " " + dataAfterSerialization.getTemperatute(), Toast.LENGTH_SHORT);
                toast.show();
                //sender.send(currentData.getBytes());
                Intent deviceList = new Intent(getActivity(), DeviceListActivity.class);
                deviceList.putExtra(Constants.EXTRA_DATA, currentData);
                startActivity(deviceList);
            }
        });
        return rootView;
    }
}
