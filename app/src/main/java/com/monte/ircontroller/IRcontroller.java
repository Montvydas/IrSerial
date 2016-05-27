package com.monte.ircontroller;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.ConsumerIrManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by monte on 26/05/16.
 */
public class IRcontroller {
    private ConsumerIrManager irManager;
    private Context context;
    private int minFreq;
    private int maxFreq;
    private ConsumerIrManager.CarrierFrequencyRange[] irFrequencies;
    private boolean IRsupported;

    public List<Integer> listPulses = new ArrayList<>();
    private int frequency = 38400;

    public IRcontroller(Context context) {
        this.irManager = (ConsumerIrManager) context.getSystemService(AppCompatActivity.CONSUMER_IR_SERVICE);
        this.context = context;

        if (irManager.hasIrEmitter()){
            Toast.makeText(context, "IR supported!", Toast.LENGTH_SHORT).show();
            this.irFrequencies = irManager.getCarrierFrequencies();
            this.minFreq = irFrequencies[0].getMinFrequency();
            this.maxFreq = irFrequencies[0].getMaxFrequency();
            this.IRsupported = true;
        } else {
            Toast.makeText(context, "IR NOT supported!", Toast.LENGTH_SHORT).show();
            this.IRsupported = false;
        }
    }

    public ConsumerIrManager getIrManager() {
        return irManager;
    }
    public int getMinFreq() {
        return minFreq;
    }
    public int getMaxFreq() {
        return maxFreq;
    }
    public ConsumerIrManager.CarrierFrequencyRange[] getIrFrequencies() {
        return irFrequencies;
    }
    public boolean isIRsupported() {
        return IRsupported;
    }

    public void sendLG (long data, int nbits){
        int freq = 38000;
        //freq = 38kHz
        // Header
        listPulses.clear();
        listPulses.add(LG_HDR_MARK);
        listPulses.add(LG_HDR_SPACE);
        listPulses.add(LG_BIT_MARK);

        // Data
        for (long  mask = 1 << (nbits - 1);  mask != 0;  mask >>= 1) {
            if ( (data & mask) != 0 ) {
                listPulses.add(LG_ONE_SPACE);
                listPulses.add(LG_BIT_MARK);
            } else {
                listPulses.add(LG_ZERO_SPACE);
                listPulses.add(LG_BIT_MARK);
            }
        }
        listPulses.add(39416);

        listPulses.add(9000);
        listPulses.add(2210);
        listPulses.add(560);

        Log.e("data=", listPulses.toString());

        int[] newData = new int[listPulses.size()];
        int index = 0;
        for (Integer i: listPulses){
            newData[index] = i.intValue();
            index++;
//                System.out.print(", " + i.intValue());
        }
        irManager.transmit(freq, newData);
    }
    public boolean sendSonyInt2 (int data){
        String binary = Integer.toBinaryString(data);

        //check for required number of bits is no more than 12
        if (binary.length() > 12)
            return false;

        //need to add leading 0 to make sure that big enough binary value is used
        if (binary.length() < 12)
            binary = String.format("%0" + (12-binary.length()) + "d", 0).replace("0", "0") + binary; //add zeroes at the begining

        int[] dataToSend = new int[binary.length()];

        dataToSend[0] = SONY_HDR_MARK;
        dataToSend[1] = SONY_HDR_SPACE;

        int offset = 2;
        for (int i = 0; i < binary.length(); i++) {
            if (binary.charAt(i) == '0') {
                dataToSend[2*i+offset] = SONY_ONE_MARK;
            } else {
                dataToSend[2*i+offset] = SONY_ZERO_MARK;
            }
            dataToSend[2*i+1+offset] = SONY_HDR_SPACE;
        }
        for (int i = 0; i < dataToSend.length; i++)
            System.out.print(dataToSend[i]);
        System.out.println();


        List<Integer> listData = new ArrayList<>();
        listData.add(SONY_HDR_MARK);
        listData.add(SONY_HDR_SPACE);

        for (int i = 0; i < binary.length(); i++) {
            if (binary.charAt(i) == '0') {
                listData.add(SONY_ONE_MARK);
            } else {
                listData.add(SONY_ZERO_MARK);
            }
            listData.add(SONY_HDR_SPACE);
        }
        Log.e("dataToSend=", listData.toString());
        return true;
    }
    public void sendSony (long data,  int nbits)
    {
        int freq = 40000;

        listPulses.clear();
        listPulses.add(SONY_HDR_MARK);
        listPulses.add(SONY_HDR_SPACE);
        // Header

        for (long  mask = 1 << (nbits - 1);  mask != 0;  mask >>= 1) {
            String tmp = String.format("%x", mask);
            Log.e("mask=", tmp);
            if ( (data & mask) != 0) {
                listPulses.add(SONY_ONE_MARK);
            } else {
                listPulses.add (SONY_ZERO_MARK);
            }
            listPulses.add(SONY_HDR_SPACE);
        }
        irManager.transmit(freq, listToArray(listPulses));
    }

    int LG_BITS = 28;
    int LG_HDR_MARK  = 8000;
    int LG_HDR_SPACE = 4000;
    int LG_BIT_MARK  = 600;
    int LG_ONE_SPACE = 1600;
    int LG_ZERO_SPACE = 550;
    int LG_RPT_LENGTH = 60000;

    public boolean sendLGHex (int data){
        return sendLG(hexToDec(data));
    }

    public boolean sendLG (int data){
        String binary = Integer.toBinaryString(data);

        if (binary.length() > LG_BITS || !IRsupported)              //check for required number of bits is no more than allowed
            return false;
        binary = addLeadingZeros(binary, LG_BITS);      //need to add leading 0 to make sure that big enough binary value is used

//        Log.e("binary", binary);
        listPulses.clear();
        listPulses.add(LG_HDR_MARK);
        listPulses.add(LG_HDR_SPACE);
        listPulses.add(LG_BIT_MARK);
        addPulseLengths(binary, LG_ONE_SPACE, LG_ZERO_SPACE, LG_BIT_MARK, false);    //add pulses for about the data
        Log.e("dataToSend", listPulses.toString());

        listPulses.add(4000);
        irManager.transmit(40000, listToArray(listPulses));
        return true;
    }

    int SONY_BITS       = 12;
    int SONY_HDR_MARK   = 2400;
    int SONY_HDR_SPACE  = 600;
    int SONY_ONE_MARK   = 1200;
    int SONY_ZERO_MARK  = 600;
    int SONY_RPT_LENGTH = 45000;
    int SONY_DOUBLE_SPACE_USECS  =  500;  // usually ssee 713 - not using ticks as get number wrapround

    public boolean sendSonyHex (int data){
        return sendSony(hexToDec(data));
    }

    public boolean sendSony (int data){
        String binary = Integer.toBinaryString(data);

        if (binary.length() > SONY_BITS || !IRsupported)        //check for required number of bits is no more than allowed
            return false;
        binary = addLeadingZeros(binary, SONY_BITS);        //need to add leading 0 to make sure that big enough binary value is used
//        Log.e("binary", binary);

        listPulses.clear();
        listPulses.add(SONY_HDR_MARK);    //add stuff at the beginning
        listPulses.add(SONY_HDR_SPACE);
        addPulseLengths(binary, SONY_ONE_MARK, SONY_ZERO_MARK, SONY_HDR_SPACE, false);    //add pulses for about the data

        listPulses.add(SONY_RPT_LENGTH);
        Log.e("dataToSend", listPulses.toString());

        irManager.transmit(40000, listToArray(listPulses));
        return true;
    }

    int NEC_BITS = 32;
    int NEC_HDR_MARK   = 9000;
    int NEC_HDR_SPACE  = 4500;
    int NEC_BIT_MARK    = 560;
    int NEC_ONE_SPACE  = 1690;
    int NEC_ZERO_SPACE  = 560;
    int NEC_RPT_SPACE  = 2250;

    public boolean sendNEC (int data){
        String binary = Integer.toBinaryString(data);

        if (binary.length() > NEC_BITS || !IRsupported)        //check for required number of bits is no more than allowed
            return false;
        binary = addLeadingZeros(binary, NEC_BITS);        //need to add leading 0 to make sure that big enough binary value is used
        Log.e("binary", binary);

        listPulses.clear();
        listPulses.add(NEC_HDR_MARK);    //add stuff at the beginning
        listPulses.add(NEC_HDR_SPACE);
        addPulseLengths(binary, NEC_ONE_SPACE, NEC_ZERO_SPACE, NEC_BIT_MARK, true);    //add pulses for about the data

        listPulses.add(NEC_BIT_MARK);
        listPulses.add(NEC_RPT_SPACE);

        Log.e("dataToSend", listPulses.toString());
        irManager.transmit(40000, listToArray(listPulses));
        return true;
    }

//    void  IRsend::sendNEC (unsigned long data,  int nbits)
//    {
//        // Set IR carrier frequency
//        enableIROut(38);
//
//        // Header
//        mark(NEC_HDR_MARK);
//        space(NEC_HDR_SPACE);
//
//        // Data
//        for (unsigned long  mask = 1UL << (nbits - 1);  mask;  mask >>= 1) {
//        if (data & mask) {
//            mark(NEC_BIT_MARK);
//            space(NEC_ONE_SPACE);
//        } else {
//            mark(NEC_BIT_MARK);
//            space(NEC_ZERO_SPACE);
//        }
//    }
//
//        // Footer
//        mark(NEC_BIT_MARK);
//        space(0);  // Always end with the LED off
//    }

    int SAMSUNG_BITS        =  32;
    int SAMSUNG_HDR_MARK    = 5000;
    int SAMSUNG_HDR_SPACE   = 5000;
    int SAMSUNG_BIT_MARK    = 560;
    int SAMSUNG_ONE_SPACE   = 1600;
    int SAMSUNG_ZERO_SPACE  = 560;
    int SAMSUNG_RPT_SPACE   = 2250;

    public boolean sendSamsung (int data){
        String binary = Integer.toBinaryString(data);

        if (binary.length() > SAMSUNG_BITS || !IRsupported)        //check for required number of bits is no more than allowed
            return false;
        binary = addLeadingZeros(binary, SAMSUNG_BITS);        //need to add leading 0 to make sure that big enough binary value is used
        Log.e("binary", binary);

        listPulses.clear();
        listPulses.add(SAMSUNG_HDR_MARK);    //add stuff at the beginning
        listPulses.add(SAMSUNG_HDR_SPACE);
        addPulseLengths(binary, SAMSUNG_ONE_SPACE, SAMSUNG_ZERO_SPACE, SAMSUNG_BIT_MARK, true);    //add pulses for about the data

        listPulses.add(SAMSUNG_BIT_MARK);
        listPulses.add(SAMSUNG_RPT_SPACE);

        Log.e("dataToSend", listPulses.toString());
        irManager.transmit(40000, listToArray(listPulses));
        return true;
    }

    int DISH_BITS        =  16;
    int DISH_HDR_MARK    = 400;
    int DISH_HDR_SPACE   = 6100;
    int DISH_BIT_MARK    = 400;
    int DISH_ONE_SPACE   = 1700;
    int DISH_ZERO_SPACE  = 2800;
    int DISH_RPT_SPACE   = 6200;

    public boolean sendDISH (int data){
        String binary = Integer.toBinaryString(data);

        if (binary.length() > DISH_BITS || !IRsupported)        //check for required number of bits is no more than allowed
            return false;
        binary = addLeadingZeros(binary, DISH_BITS);        //need to add leading 0 to make sure that big enough binary value is used
//        Log.e("binary", binary);

        listPulses.clear();
        listPulses.add(DISH_HDR_MARK);    //add stuff at the beginning
        listPulses.add(DISH_HDR_SPACE);
        addPulseLengths(binary, DISH_ONE_SPACE, DISH_ZERO_SPACE, DISH_BIT_MARK, true);    //add pulses for about the data

        listPulses.add(DISH_RPT_SPACE);

        Log.e("dataToSend", listPulses.toString());
        irManager.transmit(40000, listToArray(listPulses));
        return true;
    }

    int JVC_BITS        =    16;
    int JVC_HDR_MARK    =  8000;
    int JVC_HDR_SPACE   =  4000;
    int JVC_BIT_MARK    =   600;
    int JVC_ONE_SPACE   =  1600;
    int JVC_ZERO_SPACE  =   550;
    int JVC_RPT_LENGTH  = 60000;


//+=============================================================================
// JVC does NOT repeat by sending a separate code (like NEC does).
// The JVC protocol repeats by skipping the header.
// To send a JVC repeat signal, send the original code value
//   and set 'repeat' to true
//
    public boolean sendJVC (int data, boolean repeat){
        String binary = Integer.toBinaryString(data);

        if (binary.length() > JVC_BITS || !IRsupported)        //check for required number of bits is no more than allowed
            return false;
        binary = addLeadingZeros(binary, JVC_BITS);        //need to add leading 0 to make sure that big enough binary value is used
//        Log.e("binary", binary);

        listPulses.clear();

        if (!repeat){
            listPulses.remove(0);
            listPulses.remove(0);

            irManager.transmit(38000, listToArray(listPulses));
            return true;
        }

        listPulses.add(JVC_HDR_MARK);    //add stuff at the beginning
        listPulses.add(JVC_HDR_SPACE);

        addPulseLengths(binary, JVC_ONE_SPACE, JVC_ZERO_SPACE, JVC_BIT_MARK, true);    //add pulses for about the data

        listPulses.add(JVC_BIT_MARK);
        listPulses.add(JVC_RPT_LENGTH);

        Log.e("dataToSend", listPulses.toString());
        irManager.transmit(38000, listToArray(listPulses));
        return true;
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
