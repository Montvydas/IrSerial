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
    private int DEBUG = 1;

    private ConsumerIrManager irManager;
    private Context context;
    private int minFreq;
    private int maxFreq;
    private ConsumerIrManager.CarrierFrequencyRange[] irFrequencies;
    private boolean irSupported;
    private List<Integer> listPulses = new ArrayList<>();
    private int freq;
    public final static int DEFAULT_FREQ = 38400;
    public final static int DEFAULT_BAUD = 2400;
    public final static int MAX_BAUD = 4800;
    public final static int RS232_BITS = 8;
    private int baud;

    /**
     * Initialise the IR transmission with default values of frequency of 38400 Hz and
     * baud rate of 2400 bits/s.
     *
     * @param context
     */

    //constructor ------------------------------
    public IrSerial (Context context){
        this.freq = DEFAULT_FREQ;
        this.baud = DEFAULT_BAUD;
        initialise(context);
    }

    /**
     * This time you can also initialise the transmission and specify your desired frequency and baud rate.
     *
     * @param context
     * @param freq
     * @param baud
     */
    public IrSerial (Context context, int freq, int baud){
        if (baud > MAX_BAUD)
            Log.e("Baud Rate Warning", "Baud Rate is too high for Infrared. Was set Anyway.");
        if (freq > maxFreq || freq < minFreq){
            Log.e("Frequency Warning", "Specified frequency is not within the boundary. Was set DEFAULT.");
            this.freq = DEFAULT_FREQ;
        }

        this.freq = freq;
        this.baud = baud;
        initialise(context);
    }

    /**
     * Part of the constructor. This will have to be called for any type of construction as it
     * also initialises the IrManager.
     *
     * @param context
     */
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
    public int getBaud() {
        return baud;
    }

    //setters ----------------------------------
    public void setFreq(int freq) {
        if (freq > maxFreq || freq < minFreq){
            Log.e("Frequency Warning", "Specified frequency is not within the boundary. Was discarded.");
            return;
        }
        this.freq = freq;
    }
    public void setBaud(int baud) {
        if (baud > MAX_BAUD)
            Log.e("Baud Rate Warning", "Baud Rate is too high for Infrared. Was set anyway.");

        this.baud = baud;
    }

    //methods----------------------------------
    public void begin (int baud, int freq){
        if (baud > MAX_BAUD) {
            Log.e("Baud Rate Warning", "Baud Rate is too high for Infrared. Was set anyway.");
        }

        if (freq > maxFreq || freq < minFreq){
            Log.e("Frequency Warning", "Specified frequency is not within the boundary. Was discarded.");
            return;
        }
        this.baud = baud;
        this.freq = freq;
    }

    //Sends raw data coming from an integer array
    public boolean sendRaw (int[] data){
        if (!irSupported)
            return false;

        irManager.transmit(freq, data);
        return true;
    }

    //sending int
    public boolean send (int data){
        if (!irSupported)
            return false;
        sendRaw(construct(data));
        return true;
    }

    public boolean send (char data){
        if (!irSupported)
            return false;
        sendRaw(construct(data));
        return true;
    }

    public boolean send (String data){
        if (!irSupported)
            return false;
        sendRaw(construct(data));

//        for (int i = 0; i < data.length(); i++)
//            sendRaw(construct(data.charAt(i)));
        return true;
    }


    public int[] construct (String data){
        listPulses.clear();

        for (int i = 0; i < data.length(); i++){
             constructSequence(Long.toBinaryString(data.charAt(i) ^ 0xFF));
        }

        return listToArray(listPulses);
    }

    public int[] construct (char data){
        listPulses.clear();
        constructSequence(Long.toBinaryString(data ^ 0xFF));
        return listToArray(listPulses);
    }

    public int[] construct (int data){
        listPulses.clear();
        long mask= (long) (Math.pow(2, 8) - 1);
        String binaryData = Long.toBinaryString(data ^ mask);
//        String binary = Long.toBinaryString(data ^ 0xFF);

        if (data > (long) Math.pow(2, RS232_BITS)-1){
//            if (binaryData.length() > RS232_BITS)
            Log.e("Construct Warning", "Data Not fitting in 8 bits. Sent the lower 8 bits only.");
        }

        constructSequence(binaryData);
        return listToArray(listPulses);
    }

    private void constructSequence (String binaryData){
        int rs232_mark = (int) (Math.pow(10, 6) / baud);

        binaryData = addLeadingZeros(binaryData, RS232_BITS);        //need to add leading 0 to make sure that big enough binary value is used

        if (DEBUG == 1)
            Log.e("binaryData", binaryData);

        StringBuilder tmp = new StringBuilder(binaryData);

        tmp.reverse();
        tmp.insert(0, '1');
        tmp.append('0');

        if (DEBUG == 1)
            Log.e("string build", tmp.toString());

        addDataRS232(tmp.toString(), rs232_mark);

        //tested with Samsung S6 edge, which was not working very well otherwise... !!! Need more testing
        if (android.os.Build.MODEL == "SM-G925F"){
            listPulses.add(10000);
        }

        if (listPulses.size() % 2 != 0)     //mostly used for strings as when one is built, we need to make sure the
            listPulses.add(rs232_mark);     //final pulse goes to LOW

        if (DEBUG == 1)
            Log.e("dataToSend", listPulses.toString());
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
