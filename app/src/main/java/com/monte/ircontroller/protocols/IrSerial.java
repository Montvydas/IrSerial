package com.monte.ircontroller.protocols;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.InputMismatchException;
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
    private final int RS232_BITS = 8;
    private int baud;

    //constructor ------------------------------
    public IrSerial (Context context){
        this.freq = DEFAULT_FREQ;
        this.baud = DEFAULT_BAUD;
        initialise(context);
    }

    public IrSerial (Context context, int freq, int baud){
        if (baud > MAX_BAUD)
            Log.e("Baud Rate Warning", "Baud Rate is too high for Infrared");

        this.freq = freq;
        this.baud = baud;
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

    //getters-----------------------------------
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

    //setters ----------------------------------
    public void setFreq(int freq) {
        this.freq = freq;
    }

    //methods ----------------------------------
    public void begin (int baud, int freq){
        if (baud > MAX_BAUD)
            Log.e("Baud Rate Warning", "Baud Rate is too high for Infrared");

        this.baud = baud;
        this.freq = freq;
    }

    public boolean send (int[] data){
        if (!irSupported)
            return false;

        irManager.transmit(freq, data);
        return true;
    }

    public int[] construct (String data){
        listPulses.clear();

        for (int i = 0; i < data.length(); i++){
             constructSequence(Long.toBinaryString(data.charAt(i)));
        }

        return listToArray(listPulses);
    }

    public int[] contruct (char data){
        listPulses.clear();
        constructSequence(Long.toBinaryString((int) data));
        return listToArray(listPulses);
    }

    public int[] construct (int data){
        listPulses.clear();
        long mask= (long) (Math.pow(2, 8) - 1);
        String binaryData = Long.toBinaryString(data ^ mask);
//        String binary = Long.toBinaryString(data ^ 0xFF);

        if (data > (long) Math.pow(2, RS232_BITS)-1){
//            if (binaryData.length() > RS232_BITS)
            Log.e("Construct Warning", "Data Not fitting in 8 bits.");
        }

        constructSequence(binaryData);
        return listToArray(listPulses);
    }

    private void constructSequence (String binaryData){
        int rs232_mark = (int) (Math.pow(10, 6) / baud);

        binaryData = addLeadingZeros(binaryData, RS232_BITS);        //need to add leading 0 to make sure that big enough binary value is used

//        Log.e("binaryData", binaryData);
        StringBuilder tmp = new StringBuilder(binaryData);

        tmp.reverse();
        tmp.insert(0, '1');
        tmp.append('0');

//        Log.e("string build", tmp.toString());

        addDataRS232(tmp.toString(), rs232_mark);

        if (android.os.Build.MODEL == "SM-G925F"){
            listPulses.add(10000);
        }
//        Log.e("dataToSend", listPulses.toString());
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

    private String addLeadingZeros (String binary, int length){
        if (binary.length() < length)
            binary = String.format("%0" + (length-binary.length()) + "d", 0).replace("0", "0") + binary; //add zeroes at the begining
        return binary;
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
}
