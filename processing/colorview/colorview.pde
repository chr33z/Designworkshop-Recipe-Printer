/* For use with the colorview Arduino example sketch 
   Update the Serial() new call to match your serial port
   e.g. COM4, /dev/usbserial, etc!
*/


import processing.serial.*;
import java.awt.datatransfer.*;
import java.awt.Toolkit;

Serial port;
 
void setup(){
  //println(Serial.list());
  
 size(1024,1024);
 port = new Serial(this, "/dev/ttyACM0", 9600); //remember to replace COM20 with the appropriate serial port on your computer
}
 
 
String buff = "";

int wRed, wGreen, wBlue, wClear;
String hexColor = "ffffff";
 

void draw(){
 background(wRed, wGreen, wBlue);
 // check for serial, and process
 while (port.available() > 0) {
   serialEvent(port.read());
 }
}
 
void serialEvent(int serial) {
  if(serial != '\n'){
    buff += char(serial);
  } else {
   println(buff);
   if(buff.length() == 6){
     wRed = Integer.valueOf(buff.substring(0,2), 16);
     wGreen = Integer.valueOf(buff.substring(2,4), 16);
     wBlue = Integer.valueOf(buff.substring(4,6), 16);
   }
   buff = "";
  } 
  
 /*
 if(serial != '\n') {
   buff += char(serial);
 } else {
   //println(buff);
   
   int cRed = buff.indexOf("R");
   int cGreen = buff.indexOf("G");
   int cBlue = buff.indexOf("B");
   int clear = buff.indexOf("C");
   
   if(clear >=0){
     String val = buff.substring(clear+2);
     val = val.split("\t")[0];
     try{
       wClear = Integer.parseInt(val.trim());
     }catch(NumberFormatException e){
       return;
     }
   } else { return; }
   
   if(cRed >=0){
     String val = buff.substring(cRed+2);
     val = val.split("\t")[0];
     try{
       wRed = Integer.parseInt(val.trim());
     }catch(NumberFormatException e){
       return;
     }
   } else { return; }
   
   if(cGreen >=0) {
     String val = buff.substring(cGreen+2);
     val = val.split("\t")[0];
     try{
       wGreen = Integer.parseInt(val.trim());
     }catch(NumberFormatException e){
       return;
     }
   } else { return; }
   
   if(cBlue >=0) {
     String val = buff.substring(cBlue+2);
     val = val.split("\t")[0];
     try{
       wBlue = Integer.parseInt(val.trim());
     }catch(NumberFormatException e){
       return;
     }
   } else { return; }
   
   print("Red: "); print(wRed);
   print("\tGrn: "); print(wGreen);
   print("\tBlue: "); print(wBlue);
   print("\tClr: "); println(wClear);
   
   wRed *= 255; wRed /= wClear;
   wGreen *= 255; wGreen /= wClear; 
   wBlue *= 255; wBlue /= wClear; 

   hexColor = hex(color(wRed, wGreen, wBlue), 6);
   println(hexColor);
   buff = "";
 }
 */
}
