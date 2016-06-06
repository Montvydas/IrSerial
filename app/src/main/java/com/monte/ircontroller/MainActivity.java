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
import com.monte.ircontroller.protocols.IrSerial;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private TextView minFreqText;
    private TextView maxFreqText;
    private EditText transmitFreq;
    private EditText transmitData;
    private IRcontroller irController;
    private ConsumerIrManager manager;
    private EditText baudRate;

    private Spinner protocolSpinner;

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

        irController = new IRcontroller(this);
        manager = ConsumerIrManager.getSupportConsumerIrManager(this);

        mySerial = new IrSerial(this, IrSerial.DEFAULT_FREQ, 3600);

        protocolSpinner = (Spinner) findViewById(R.id.protocolSpinner);
        protocolSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.protocols_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        protocolSpinner.setAdapter(adapter);

        minFreqText.setText("Min Freq= " + irController.getMinFreq() + " Hz");
        maxFreqText.setText("Max Freq= " + irController.getMaxFreq() + " Hz");
    }

    public void sendIR (View view){
        int freq;
//        int data;
        int baud;
        try {
            freq = Integer.parseInt(transmitFreq.getText().toString());
//            data = Integer.parseInt(transmitData.getText().toString());
            baud = Integer.parseInt(baudRate.getText().toString());
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), "Enter INT in the correct range!", Toast.LENGTH_SHORT).show();
            return;
        }

//        data = Integer.valueOf(String.valueOf(data), 16);

        // Device model
        String PhoneModel = android.os.Build.MODEL;
        // Android version
        String AndroidVersion = android.os.Build.VERSION.RELEASE;

        Log.e("Phone Model", PhoneModel);
        Log.e("AndroidVersion", AndroidVersion);

        if (PhoneModel == "SM-G925F"){
            //add additional delay???
        }

//        irController.sendNEC(0x5343);

        mySerial.send(transmitData.getText().toString());

//        for (int i = 0; i < 3; i++)
//            irController.sendRS232(data, freq, baud, 8);
//        return;
//        if (irController.sendRS232(data, freq, baud, 8))
//            return;

//        switch (protocol){
//            case 0:     //Sony
//                irController.sendSony(data);
//                break;
//            case 1:     //NEC
//                irController.sendNEC(data);
//                break;
//            case 2:     //Samsung
//                irController.sendSamsung(data);
//                break;
//            case 3:     //LG
//                irController.sendLG(data);
//                break;
//            case 4:     //DISH
//                irController.sendDISH(data);
//                break;
//            case 5:     //JVC
//                irController.sendJVC(data, false);
//                break;
//        }

//        IrCommand necCommand = IrCommand.NEC.buildNEC(32, 0x723F56);
//        for (int i = 0; i < necCommand.pattern.length; i++)
//            System.out.print(necCommand.pattern);
//        Log.e("Pattern", necCommand.pattern.toString());

//        manager.transmit(necCommand);

//        irController.transmit(necCommand.frequency, necCommand.pattern);

//        Toast.makeText(getApplicationContext(), "Sending!", Toast.LENGTH_SHORT).show();
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

    private int protocol = 0;
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        protocol = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        protocol = 0;
    }
}