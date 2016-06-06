package com.monte.ircontroller.protocols;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by monte on 06/06/16.
 */
public class IrSerial {

    //vars
    private ConsumerIrManager irManager;
    private Context context;
    private int minFreq;
    private int maxFreq;
    private ConsumerIrManager.CarrierFrequencyRange[] irFrequencies;
    private boolean irSupported;
    public List<Integer> listPulses = new ArrayList<>();
    private int freq;
    private final int DEFAULT_FREQ = 38400;
    private final int DEFAULT_BAUD = 2400;
    private final int MAX_BAUD = 4800;
    private int baud;

    //constructor -----------------------
    public IrSerial (Context context){
        this.freq = DEFAULT_FREQ;
        initialise(context);
    }

    public IrSerial (Context context, int freq){
        this.freq = freq;
        initialise(context);
    }

    private void initialise (Context context){
        this.irManager = (ConsumerIrManager) context.getSystemService(AppCompatActivity.CONSUMER_IR_SERVICE);
        this.context = context;


        if (irManager.hasIrEmitter()){
            this.irFrequencies = irManager.getCarrierFrequencies();
            this.minFreq = irFrequencies[0].getMinFrequency();
            this.maxFreq = irFrequencies[0].getMaxFrequency();
            this.irSupported = true;
        } else {
            this.irSupported = false;
        }
    }
    //-----------------------------------

    //getters
    public int getMinFreq() {
        return minFreq;
    }
    public int getMaxFreq() {
        return maxFreq;
    }
    public int getFreq() {
        return freq;
    }
    public boolean isIrSupported() {
        return irSupported;
    }

    //setters
    public void setFreq(int freq) {
        this.freq = freq;
    }

    //methods
    public boolean send (int[] data){
        if (!IrSupported)
            return false;

        irManager.transmit(freq, data);
        return true;
    }

    public int[] construct (String data){
        return new int[12];
    }

    public int[] contruct (char data){
        return new int[12];
    }

    public int[] construct (int data){
        long mask= (long) (Math.pow(2, 8) - 1);
        String binary = Long.toBinaryString(data ^ mask);
//        String binary = Long.toBinaryString(data ^ 0xFF);

        if (data > (long) Math.pow(2, RS232_BITS)-1){
            Log.e("Construct Error", "Data Not fitting in 8 bits.");
            
        }

        return new int[12];
    }


    int BAUD_MAX = 4800;
    int RS232_BITS = 8;

    private boolean constructSequence (long data, int frequency, int baud, int bits){
        int rs232_mark = (int) (Math.pow(10, 6) / baud);

        if (baud > BAUD_MAX) {       //make sure the transmission is GOOD and for that need
            Log.e("Baud rate", "Too high for typical Infrared Sensor");
//            return false;
        }

        long mask= (long) (Math.pow(2, 8) - 1);
        String binary = Long.toBinaryString(data ^ mask);

        if (data > (long) Math.pow(2, RS232_BITS)-1 || !irSupported){
            Log.e("Data", "Not fitting in the specified number of bits");
            return false;
        }

//        String binary = Long.toBinaryString(data ^ 0xFF);

//        if (binary.length() > RS232_BITS || !IRsupported) {        //check for required number of bits is no more than allowed
//            Log.e("Data", "Not fitting in the specified number of bits");
//            return false;
//        }
        binary = addLeadingZeros(binary, RS232_BITS);        //need to add leading 0 to make sure that big enough binary value is used

        Log.e("binaryData", binary);
        StringBuilder tmp = new StringBuilder(binary);

        tmp.reverse();
        tmp.insert(0, '1');
        tmp.append('0');

        Log.e("string build", tmp.toString());

        listPulses.clear();
        addDataRS232(tmp.toString(), rs232_mark);

        if (android.os.Build.MODEL == "SM-G925F"){
            listPulses.add(10000);
        }
        Log.e("dataToSend", listPulses.toString());

        irManager.transmit(frequency, listToArray(listPulses));
        return true;
    }

    private void addDataRS232 (String binary, int mark){
        int pulse = 0;
        char prevChar = '1';
        for (int i = 0; i < binary.length(); i++){
            if (prevChar != binary.charAt(i)){
                listPulses.add(pulse*mark);
                prevChar = binary.charAt(i);
                pulse = 0;
            }
            pulse++;
        }
        if (pulse > 0)
            listPulses.add(pulse*mark);
    }


    //converts from hex to dec
    private int hexToDec(int n) {
        return Integer.valueOf(String.valueOf(n), 16);
    }

    private void addPulseLengths (String binary, int one_mark, int zero_mark, int space, boolean reverse){
        for (int i = 0; i < binary.length(); i++) {
            if (reverse)
                listPulses.add(space);

            if (binary.charAt(i) == '1') {
                listPulses.add(one_mark);
            } else {
                listPulses.add(zero_mark);
            }

            if (!reverse)
                listPulses.add(space);
        }
    }

    private int[] listToArray (List<Integer> myData){
        int[] newData = new int[myData.size()];
        int index = 0;
        for (Integer entry: myData){
            newData[index] = entry.intValue();
            index++;
        }
        return newData;
    }

    private String addLeadingZeros (String binary, int length){
        if (binary.length() < length)
            binary = String.format("%0" + (length-binary.length()) + "d", 0).replace("0", "0") + binary; //add zeroes at the begining
        return binary;
    }


}
