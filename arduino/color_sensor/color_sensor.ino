#include <Adafruit_Thermal.h>

#include <Wire.h>
#include "Adafruit_TCS34725.h"

#include "SoftwareSerial.h"
// If you're using Arduino 23 or earlier, uncomment the next line:
//#include "NewSoftSerial.h"

// printer libraries
#include "Adafruit_Thermal.h"
#include "adalogo.h"
#include "adaqrcode.h"
#include <avr/pgmspace.h>

int printer_RX_Pin = 5;  // This is the green wire
int printer_TX_Pin = 6;  // This is the yellow wire

Adafruit_Thermal printer(printer_RX_Pin, printer_TX_Pin);

// our RGB -> eye-recognized gamma color
byte gammatable[256];

boolean modePrint = true;

Adafruit_TCS34725 tcs = Adafruit_TCS34725(TCS34725_INTEGRATIONTIME_50MS, TCS34725_GAIN_4X);

void setup() {
  pinMode(13, OUTPUT);
  
  Serial.begin(9600);
  
  while(!tcsFound()){
    Serial.println("No TCS34725 found ... check your connections");
    delay(1000);
  }
  
  Serial.println("TCS34725 found");
  
  //establishConnection();

  pinMode(7, OUTPUT); digitalWrite(7, LOW); // To also work w/IoTP printer
  printer.begin();
}


void loop() {
  if(Serial.available()){
   processInput(Serial.readStringUntil('#'));
  }
  
  if(modePrint){
    uint16_t clear, red, green, blue;
  
    tcs.setInterrupt(false);      // turn on LED
  
    delay(25);  // takes 50ms to read 
    
    tcs.getRawData(&red, &green, &blue, &clear);
  
    tcs.setInterrupt(true);  // turn off LED
  
    // Figure out some basic hex code for visualization
    uint32_t sum = clear;
    float r, g, b;
    r = red; r /= sum;
    g = green; g /= sum;
    b = blue; b /= sum;
    r *= 256; g *= 256; b *= 256;
    
    Serial.print((int)r, HEX);
    Serial.print((int)g, HEX);
    Serial.print((int)b, HEX);
    Serial.print('\n');
  }
}

void processInput(String input){
  Serial.print(input);
  
  if(input == "PRINT_LINE"){
    printLine(Serial.readStringUntil('#'));
  }
  
  if(input == "PRINT_FEED"){
    printLineFeed(Serial.readStringUntil('#'));
  }
  
  if(input == "MODE_PRINT"){
    modePrint = true; 
  }
  
  if(input == "MODE_SENSOR"){
    modePrint = false; 
  }
}

/** Check whether farb sensor is found */
boolean tcsFound(){
    if(tcs.begin()){
      return true;
    } else {
      return false;
    }
}

void establishConnection(){
  while(Serial.available() == 0);
  
  if(Serial.available() > 0 && Serial.read() == 0){
    Serial.print("HELLO!");
    Serial.print('\n');
  } 
  
  //while (Serial.available() == 0) { // wait until bytes arrive
    Serial.print("HELLO!");
    Serial.print('\n');
    delay(300);
  //}
}

void printLine(String string){  
  printer.println(string);

  //printer.sleep();      // Tell printer to sleep
  //printer.wake();       // MUST call wake() before printing again, even if reset
  //printer.setDefault(); // Restore printer to defaults
}

void printLineFeed(String n){
  const char * c = n.c_str();
  
  printer.feed(atoi(c));
}

void printTestProgram(){
  // Test inverse on & off
  printer.inverseOn();
  printer.println("Inverse ON");
  printer.inverseOff();

  // Test character double-height on & off
  printer.doubleHeightOn();
  printer.println("Double Height ON");
  printer.doubleHeightOff();

  // Set text justification (right, center, left) -- accepts 'L', 'C', 'R'
  printer.justify('R');
  printer.println("Right justified");
  printer.justify('C');
  printer.println("Center justified");
  printer.justify('L');
  printer.println("Left justified");

  // Test more styles
  printer.boldOn();
  printer.println("Bold text");
  printer.boldOff();

  printer.underlineOn(); 
  printer.println("Underlined text ");
  printer.underlineOff();

  printer.sleep();      // Tell printer to sleep
  printer.wake();       // MUST call wake() before printing again, even if reset
  printer.setDefault(); // Restore printer to defaults
}

