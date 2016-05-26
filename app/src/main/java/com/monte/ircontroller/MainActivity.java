package com.monte.ircontroller;

/**
 * Created by monte on 26/05/16.
 */

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView minFreqText;
    private TextView maxFreqText;
    private EditText transmitFreq;
    private IRcontroller irController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        minFreqText = (TextView) findViewById(R.id.minFreqText);
        maxFreqText = (TextView) findViewById(R.id.maxFreqText);
        transmitFreq = (EditText) findViewById(R.id.transmitFrequencyText);

        irController = new IRcontroller(this);

//        minFreqText.setText("Min Freq= " + irController.getMinFreq() + " Hz");
//        maxFreqText.setText("Max Freq= " + irController.getMaxFreq() + " Hz");
    }

    public void sendIR (View view){
        irController.sendSonyInt(69);
        Log.e("Sending HEX", Integer.toHexString(69));
        irController.sendLGInt(133);

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
}
//        int freq = 38400;
//        try {
//            freq = Integer.parseInt(transmitFreq.getText().toString());
//        } catch (Exception e){
//            Toast.makeText(getApplicationContext(), "Enter frequency in Hertz!", Toast.LENGTH_SHORT).show();
//            return;
//        }

//int[] irSignal = {9000, 4500, 560, 560, 560, 560, 560, 1690, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 560, 560, 560, 560, 560, 560, 1690, 560, 560, 560, 560, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 39416}; //AnalysIR Batch Export (IRremote) - RAW

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