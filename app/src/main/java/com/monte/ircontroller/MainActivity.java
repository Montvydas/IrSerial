package com.monte.ircontroller;

/**
 * Created by monte on 26/05/16.
 */

import android.Manifest;
import android.content.pm.ActivityInfo;
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

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView minFreqText;
    private TextView maxFreqText;
    private EditText transmitFreq;
    private EditText transmitData;
    private IRcontroller irController;
    private ConsumerIrManager manager;

    private Spinner protocolSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        minFreqText = (TextView) findViewById(R.id.minFreqText);
        maxFreqText = (TextView) findViewById(R.id.maxFreqText);

        transmitFreq = (EditText) findViewById(R.id.transmitFrequencyText);
        transmitData = (EditText) findViewById(R.id.transmitDataText);

        irController = new IRcontroller(this);

        manager = ConsumerIrManager.getSupportConsumerIrManager(this);

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
        int data;
        try {
            freq = Integer.parseInt(transmitFreq.getText().toString());
            data = Integer.parseInt(transmitData.getText().toString());
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), "Enter INT in the correct range!", Toast.LENGTH_SHORT).show();
            return;
        }

        data = Integer.valueOf(String.valueOf(data), 16);

        if (irController.sendRS232(data, freq))
            return;

        switch (protocol){
            case 0:     //Sony
                irController.sendSony(data);
                break;
            case 1:     //NEC
                irController.sendNEC(data);
                break;
            case 2:     //Samsung
                irController.sendSamsung(data);
                break;
            case 3:     //LG
                irController.sendLG(data);
                break;
            case 4:     //DISH
                irController.sendDISH(data);
                break;
            case 5:     //JVC
                irController.sendJVC(data, false);
                break;
        }

//        IrCommand necCommand = IrCommand.NEC.buildNEC(32, 0x723F);
//        manager.transmit(necCommand);

//        irController.sendSamsung(0x12345678);
//        irController.sendDISH(0x1234);
//        irController.sendJVC(0x1234, false);
//        irController.sendJVC(0x1234, true);
//        irController.sendSony(69);
//        irController.sendSonyHex(45);
//        Log.e("Sending HEX", Integer.toHexString(69));
//        irController.sendSonyHex(Integer.parseInt(transmitFreq.getText().toString()));
//        irController.sendLGHex(Integer.parseInt(transmitFreq.getText().toString()));
//        irController.sendLG(0x12345);

        Toast.makeText(getApplicationContext(), "Sending!", Toast.LENGTH_SHORT).show();
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

//        // Check permissions
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.TRANSMIT_IR)
//                != PackageManager.PERMISSION_GRANTED){
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_CONTACTS},
//                    MY_PERMISSIONS_REQUEST_TRANSMIT_IR);
//        }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_TRANSMIT_IR: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                } else {
//                    // permission denied, boo! Disable the
//                    Toast.makeText(this, "You need this permission to ru nthe app!", Toast.LENGTH_SHORT).show();
//                    try {
//                        Thread.sleep(100);
//                        finish();
//                    } catch (InterruptedException e){
//                        Log.e("IR permission", "DENIED");
//                    }
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }