package com.monte.ircontroller;

/* Created by Coco */

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.ConsumerIrManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
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

    private ConsumerIrManager irManager;
    private ConsumerIrManager.CarrierFrequencyRange[] irFrequencies;
    private final int MY_PERMISSIONS_REQUEST_TRANSMIT_IR = 69;
    private TextView minFreqText;
    private TextView maxFreqText;
    private EditText transmitFreq;
    private int minFreq;
    private int maxFreq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        minFreqText = (TextView) findViewById(R.id.minFreqText);
        maxFreqText = (TextView) findViewById(R.id.maxFreqText);
        transmitFreq = (EditText) findViewById(R.id.transmitFrequencyText);

        irManager = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);

        if (irManager.hasIrEmitter()){
            Toast.makeText(this, "IR supported!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "IR NOT supported!", Toast.LENGTH_SHORT).show();
            try{
                Thread.sleep(100);
                finish();
            } catch (InterruptedException e){

            }
        }

        // Check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.TRANSMIT_IR)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_TRANSMIT_IR);
        }

        irFrequencies = irManager.getCarrierFrequencies();

        minFreq = irFrequencies[0].getMinFrequency();
        maxFreq = irFrequencies[0].getMaxFrequency();

        minFreqText.setText("Min Freq= " + minFreq + " Hz");
        maxFreqText.setText("Max Freq= " + maxFreq + " Hz");

        Log.e("min Freq ", ""+irFrequencies[0].getMinFrequency());
        Log.e("max Freq ", "" + irFrequencies[0].getMaxFrequency());
    }

    public void sendIR (View view){
//        int num = (Integer.parseInt("A2B", 12));
//        System.out.print(Integer.toBinaryString(num));

//        Log.e("A2B=", hexToBin("a2b"));

        int freq = 38400;
        try {
            freq = Integer.parseInt(transmitFreq.getText().toString());
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), "Enter frequency in Hertz!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (freq >= minFreq && freq <= maxFreq) {
//            sendLG(0x20DF10EF, 32);
            sendLG(0x54321, 20);
//            sendSony(0x1234, 16);
            int[] newData = new int[myData.size()];
            int index = 0;
            for (Integer i: myData){
                newData[index] = i.intValue();
                index++;
//                System.out.print(", " + i.intValue());
            }
//            System.out.println();

//            for (int i = 0; i < newData.length/2; i++) {
//                int tmp = newData[i];
//                newData[i] = newData[newData.length-1-i];
//                newData[newData.length-1-i] = tmp;
//            }
            int[] irSignal = {9000, 4500, 560, 560, 560, 560, 560, 1690, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 560, 560, 560, 560, 560, 560, 1690, 560, 560, 560, 560, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 39416}; //AnalysIR Batch Export (IRremote) - RAW

            irManager.transmit(freq, newData);
            Toast.makeText(getApplicationContext(), "Sending!", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Enter a Valid Frequency", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_TRANSMIT_IR: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // permission denied, boo! Disable the
                    Toast.makeText(this, "You need this permission to ru nthe app!", Toast.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(100);
                        finish();
                    } catch (InterruptedException e){
                        Log.e("IR permission", "DENIED");
                    }
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private int SONY_BITS       = 12;
    private int SONY_HDR_MARK   = 2400;
    private int SONY_HDR_SPACE  = 600;
    private int SONY_ONE_MARK   = 1200;
    private int SONY_ZERO_MARK  = 600;
    private int SONY_RPT_LENGTH = 45000;
    private int SONY_DOUBLE_SPACE_USECS  =  500;  // usually ssee 713 - not using ticks as get number wrapround

    private List<Integer> myData = new ArrayList();

    private void sendSony (long data,  int nbits)
    {
        // Set IR carrier frequency
//        enableIROut(40);

        myData.clear();
        myData.add(SONY_HDR_MARK);
        myData.add(SONY_HDR_SPACE);
        // Header

        for (long  mask = 1 << (nbits - 1);  mask != 0;  mask >>= 1) {
            String tmp = String.format("%x", mask);
            Log.e("mask=", tmp);
            if ( (data & mask) != 0) {
//                System.out.print("1");
                myData.add(SONY_ONE_MARK);
            } else {
//                System.out.print("0");
                myData.add (SONY_ZERO_MARK);
            }
            myData.add(SONY_HDR_SPACE);
        }
//        System.out.println ("");
        Log.e("data=", myData.toString());
    }


    private int LG_HDR_MARK  = 8000;
    private int LG_HDR_SPACE = 4000;
    private int LG_BIT_MARK  = 600;
    private int LG_ONE_SPACE = 1600;
    private int LG_ZERO_SPACE = 550;
    private int LG_RPT_LENGTH = 60000;

    private void sendLG (long data, int nbits){
    //freq = 38kHz
            // Header
        myData.clear();
        myData.add(LG_HDR_MARK);
        myData.add(LG_HDR_SPACE);
        myData.add(LG_BIT_MARK);

            // Data
        for (long  mask = 1 << (nbits - 1);  mask != 0;  mask >>= 1) {
            if ( (data & mask) != 0 ) {
                myData.add(LG_ONE_SPACE);
                myData.add(LG_BIT_MARK);
            } else {
                myData.add(LG_ZERO_SPACE);
                myData.add(LG_BIT_MARK);
            }
        }
        myData.add(39416);

        myData.add(9000);
        myData.add(2210);
        myData.add(560);

        Log.e("data=", myData.toString());
    }


    //hext to binary function for strings
    public static String hexToBin (String hex){
        int i = Integer.parseInt(hex, 12);
        String bin = Integer.toBinaryString(i);

        if (bin.length() < 8)
            bin = String.format("%0" + (8-bin.length()) + "d", 0).replace("0", "0") + bin; //add zeroes at the begining

        // System.out.print ("In hexToBi: hex=" + hex + " bin=" + bin + "\n");

        checkLengthOfHexAndBin(bin.length(), hex.length());
        return bin;
    }
    //checks length of hex and binary just in case
    public static void checkLengthOfHexAndBin (int binLength, int hexLength){
        if (binLength != 8 || hexLength != 2){
            System.err.print ("hint: entered value is a not valid hex value.\n");
        }
    }
}
