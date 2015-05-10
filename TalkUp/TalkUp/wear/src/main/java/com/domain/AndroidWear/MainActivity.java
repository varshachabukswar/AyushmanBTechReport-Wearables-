package com.domain.AndroidWear;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;





import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    //Add google Api client
    GoogleApiClient googleClient;

    private TextView mTextView;
    EditText editText2 = null;
    private SensorManager sMgr;
    TextView sensorsData;
    SensorManager mSensorManager;
    Sensor mSensor;
    int S;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);


        editText2 = (EditText) findViewById(R.id.editText2);


        /*editText2 was causing duplication error and NUlL pointer exception too*/
        /* watch_view_stub is present in the activity_main.xml so we were able to access it */
        /* to access UI out of the activity_main we used setOnLayoutInflatedListener */
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                editText2 = (EditText) stub.findViewById(R.id.editText2);
                sensorsData = (TextView) stub.findViewById(R.id.hello);
            }
        });

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

        //Add code here to implementing message sender
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void buttonOnClick(View v) {
        Button button = (Button) v;
        ((Button) v).setText("Welcome in the TalkUp Code");
        mTextView.setText("We are trying to connect to the Handheld Device");
        //String message = ":) \n";
        /* findViewById
        There is a slight problem here that when we findViewById then we can only access the Id from activity_main
        * So now there are two editText views one in round_activity and other in activity_main.
        * We have to access the one in round_activity
        * */

        //editText2.setText("Set to :) ");

        editText2.setText("<WearableData>\n" +
                " <name>Hemant</name>\n" +
                " <age>23</age>\n" +
                "<parameter>HeartRate</parameter>\n" +
                "<time>11</time>\n" +
                "<value>82</value>\n" +
                "<unit>beats per minute</unit>\n" +
                "</WearableData>\n");

        String message = "" + editText2.getText() + "\n";

        new SendToDataLayerThread(googleClient, "/message_path", message).start();
    }


    // Connect to the data layer when the Activity starts
    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorEventListener, mSensor, 1000);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorEventListener);
    }


    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            S += (int) sensorEvent.values[0];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };



    // Send a message when the data layer connection is successful.
    @Override
    public void onConnected(Bundle connectionHint) {
        String message = "Hello wearable\n Via the data layer from wear able side ";
        //Requires a new thread to avoid blocking the UI
        //editText1.setText("We are connected :) ");
        Toast.makeText(getApplicationContext(), "onConnected", Toast.LENGTH_LONG).show();
        new SendToDataLayerThread(googleClient, "/message_path", message).start();
    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();
    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            // Display message in UI
            mTextView.setText(message);
            editText2.setText("<WearableData>\n" +
                    " <name>Hemant</name>\n" +
                    " <age>23</age>\n" +
                    "<parameter>HeartRate</parameter>\n" +
                    "<time>2015-05-08 10:15:00</time>\n" +
                    "<value>82</value>\n" +
                    "<unit>beats per minute</unit>\n" +
                    "</WearableData>\n");

            String Rmessage = "" + editText2.getText() + "\n";

            new SendToDataLayerThread(googleClient, "/message_path", Rmessage).start();

        }
    }

    public void onStartTimer(View view) {

        //TextView sensorsData = (TextView)findViewById(R.id.hello);

        sMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        List<Sensor> list = sMgr.getSensorList(Sensor.TYPE_ALL);

        //Heart beat Sensor
        Sensor mHeartRateSensor;
        SensorManager mSensorManager;
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

/*
        if (mSensorManager != null){

            Context context = getApplicationContext();
            CharSequence text = "Sensor Activated!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }*/


        //Sensors list
        StringBuilder data = new StringBuilder();
        for (Sensor sensor : list) {
            data.append("Details about the sensor are : ------------------------------------------ \n");
            data.append(sensor.getName() + "\n");
            data.append(sensor.getVendor() + "\n");
            data.append(sensor.getVersion() + "\n");
            data.append(" \n");

        }
        sensorsData.setText(data);
        editText2.setText(data);
    }
}


