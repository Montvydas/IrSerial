package com.monte.ircontroller;

/**
 * Created by monte on 26/05/16.
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.monte.ircontroller.library.*;
import com.monte.ircontroller.protocols.IRcontroller;
import com.monte.ircontroller.protocols.IrSerial;

public class MainActivity extends AppCompatActivity{
    private TextView minFreqText;
    private TextView maxFreqText;
    private EditText transmitFreq;
    private EditText transmitData;
    private IRcontroller irController;
    private EditText baudRate;

    private IrSerial mySerial;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        minFreqText = (TextView) findViewById(R.id.minFreqText);
        maxFreqText = (TextView) findViewById(R.id.maxFreqText);

        transmitFreq = (EditText) findViewById(R.id.transmitFrequencyText);
        transmitData = (EditText) findViewById(R.id.transmitDataText);
        baudRate = (EditText) findViewById(R.id.baudRateText);

        //Usw these libraries if you want
//        irController = new IRcontroller(this);
//        ConsumerIrManager manager = ConsumerIrManager.getSupportConsumerIrManager(this);

        mySerial = new IrSerial(this, IrSerial.DEFAULT_FREQ, 3600);

        minFreqText.setText("Min Freq= " + irController.getMinFreq() + " Hz");
        maxFreqText.setText("Max Freq= " + irController.getMaxFreq() + " Hz");
    }

    public void sendIR (View view){
        int freq;
//        int data;
        int baud;
        try {
            freq = Integer.parseInt(transmitFreq.getText().toString());
            baud = Integer.parseInt(baudRate.getText().toString());
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), "Enter INT in the correct range!", Toast.LENGTH_SHORT).show();
            return;
        }

        mySerial.begin(freq, baud);
        mySerial.send(transmitData.getText().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}