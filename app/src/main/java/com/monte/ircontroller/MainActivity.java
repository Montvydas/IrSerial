package com.monte.ircontroller;

/**
 * Created by monte on 26/05/16.
 */

import android.nfc.FormatException;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.monte.ircontroller.library.*;
import com.monte.ircontroller.protocols.IRcontroller;
import com.monte.ircontroller.protocols.IrSerial;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    private TextView freqRangeText;

    private EditText transmitFreq;
    private EditText transmitData;
    private EditText baudRate;

    private IrSerial irSerial;
    private RadioGroup radioFormatGroup;

    private int transmitFormat = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //textView for knowing the available frequencies
        freqRangeText = (TextView) findViewById(R.id.freqRangeText);

        //edit Text fields, that can be modified
        transmitFreq = (EditText) findViewById(R.id.transmitFrequencyText);
        transmitData = (EditText) findViewById(R.id.transmitDataText);
        baudRate = (EditText) findViewById(R.id.baudRateText);

        //radio group for checking for the format of the data
        radioFormatGroup = (RadioGroup) findViewById(R.id.radioFormatGroup);
        radioFormatGroup.setOnCheckedChangeListener(this);

        //initialise the Infrared Serial library
        irSerial = new IrSerial(this, IrSerial.DEFAULT_FREQ, 3600);

        //set the text for min and max frequencies
        freqRangeText.setText(irSerial.getMinFreq() / 1000.0 + " kHz" + " ----- " +
                irSerial.getMinFreq() / 1000.0 + " kHz");
    }

    public void sendIR(View view) {
        int freq;
        int baud;
        try {//an exception will occur if the entered frequency or baud rate is non-numerical values
            freq = Integer.parseInt(transmitFreq.getText().toString());
            baud = Integer.parseInt(baudRate.getText().toString());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Enter only Integer values", Toast.LENGTH_SHORT).show();
            return;
        }

        //checks the correct regions for transmission
        if (!checkRegions(freq, baud)){
            return;
        }

        //change to a new baud rate and frequency
        irSerial.begin(freq, baud);

        switch (transmitFormat) {
            case 0: //Transmit hex data; Firstly get String (hex), then translate that to int (dec) and then send
                String dataHex = transmitData.getText().toString();
                if (dataHex.length() > 2){
                    Toast.makeText(this, "Value must be within 00 to FF", Toast.LENGTH_SHORT).show();
                    break;
                }
                try {   //an exception will occur if not hex value will be entered
                    irSerial.send(hexToDec(dataHex));
                } catch (Exception e){
                    Toast.makeText(this, "Enter Hex Value", Toast.LENGTH_SHORT).show();
                }

                break;
            case 1: //Transmit dec data; Parse the data in the field to String and then to int; then Send;
                int data;
                try {   //exception will occur if not a number is entered
                    data = Integer.parseInt(transmitData.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(this, "Value must be numerical", Toast.LENGTH_SHORT).show();
                    break;
                }

                //make sure the data is within the range
                if (data < 0 || data > 255){
                    Toast.makeText(this, "Value must be within range 0 to 255", Toast.LENGTH_SHORT).show();
                    break;
                }
                irSerial.send(data);
                break;
            case 2: //simplest transmission - get the text as string and then transmit it;
                irSerial.send(transmitData.getText().toString());
                break;
        }
        //this is used to clear the field
//        transmitData.getText().clear();
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

    //Radio button changes will be detected here
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radioHex:         //determine which of the radio buttons are checked;
                transmitFormat = 0;     //by default hex is checked;
                break;
            case R.id.radioDec:
                transmitFormat = 1;
                break;
            case R.id.radioString:
                transmitFormat = 2;
                break;
        }
    }

    //need to parse hex data to dec data for transmission
    public static int hexToDec(String hex) {
        return Integer.parseInt(hex, 16);
    }

    private boolean checkRegions (int freq, int baud){
        if (freq < irSerial.getMinFreq() || freq > irSerial.getMaxFreq()){
            Toast.makeText(this, "Enter Frequency in the correct range!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (baud <= 0){
            Toast.makeText(this, "Baud rate is negative or zero?!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (baud > IrSerial.MAX_BAUD){
            Toast.makeText(this, "Baud rate might too big, thus you might not receive any data", Toast.LENGTH_SHORT).show();
        }

        //check if the text field is empty
        if (transmitData.getText().toString().isEmpty()){
            Toast.makeText(this, "Trying to send nothing..?", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}