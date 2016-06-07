# IrSerial

//Initialise the object by passing the context, frequency and baud rate;

IrSerial irSerial = new IrSerial(this, IrSerial.DEFAULT_FREQ, 3600);

//Initialise another object by passing the context and leaving the freq and baud rate to be default values 38400 & 2400 respectively

IrSerial irSerial2 = new IrSerial(this);

//check if your phone has IR blaster

if (!irSerial.isIrSupported()){
    Toast.makeText(this, "IR is NOT supported!", Toast.LENGTH_SHORT).show();
} else {
    Toast.makeText(this, "IR is supported!", Toast.LENGTH_SHORT).show();
}

irSerial.send(123);             //send int in range 0 to 255 (8 bits)
irSerial.send('a');             //Send a char 'a'
irSerial.send("Hello World");   //Send a String

irSerial.setBaud(1200);         //Change the baud rate
irSerial.setFreq(30000);        //change the Infrared blaster frequency
irSerial.begin(1200, 30000);    //changing both at once (baud, freq)

//sends 0x99 hex or 153 dec value through IR blaster;
irSerial.performCorrectionTest();   

//if received 0x99, then there is no need to change anything
//if received not 0x99, then need to add a correction factor by writing
irSerial.setCorrectionEnabled(true);

//Some other values were also noticed to be wrong without adding the correction factor, like 0x55

//The correction factor was needed on Samsung S6 edge & Samsung Note 4, however
//That doesn't mean that all of these models have to have it... Try performing the test

//Correction was not needed for Samsung Tab S and HTC One m8

//As infrared is sent in pulses, you can access these pulses by
int[] myPulses = irSerial.construct(123);   //get the pulses for int; same for char or String;
irSerial.sendRaw(myPulses);                 //send pulses

System.out.println("DEFAULT BAUD=" + IrSerial.DEFAULT_BAUD);//default baud is 2400 bits/s
System.out.println("DEFAULT FREQ=" + IrSerial.DEFAULT_FREQ);//default freq is 38400 Hz
System.out.println("MAX BAUD=" + IrSerial.MAX_BAUD);        //The IR receivers in the lab can receive only up to 4800 bits/s
