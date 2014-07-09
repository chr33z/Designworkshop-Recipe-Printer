#!/usr/bin/env python2

import smbus
import time
import math
from YUV import *
from RGB import *
from colormatcher import *

#printer
from Adafruit_Thermal import *
 
#init
#printer = Adafruit_Thermal("/dev/ttyAMA0", 19200, timeout=5)

#bus = smbus.SMBus(1)

# I2C address 0x29
# Register 0x12 has device ver. 
# Register addresses must be OR'ed with 0x80
#bus.write_byte(0x29,0x80|0x12)
#ver = bus.read_byte(0x29)
ver = 0x55;
# version # should be 0x44
if ver == 0x44:
    
    colorMatcher = ColorMatcher()
    
    print('Device found\n')
    bus.write_byte(0x29, 0x80|0x00) # 0x00 = ENABLE register
    bus.write_byte(0x29, 0x01|0x02) # 0x01 = Power on, 0x02 RGB sensors enabled
    bus.write_byte(0x29, 0x80|0x14) # Reading results start register 14, LSB then MSB
    
    # main loop for reading colors
    while True:
        data = bus.read_i2c_block_data(0x29, 0)
        clear = clear = data[1] << 8 | data[0]
        red = (data[3] << 8 | data[2]) / clear # divide by clear should give us a range from 0 - 1
        green = (data[5] << 8 | data[4]) / clear
        blue = (data[7] << 8 | data[6]) / clear
        
        # print color result
        crgb = "C: %s, R: %s, G: %s, B: %s\n" % (clear, red, green, blue)
        print(crgb)
        
        # match color
        colorMatcher.match(YUV(red, green, blue))
        
        # get list of matched colors, if null there are none
        matched = colorMatcher.checkMatchedColors()
        
        if matched != null:
            print("hullu!")
            # find recipe here
            # print recipe here
        
        time.sleep(0.05)

elif ver == 0x55:
    #for testing the color things
    
    colorMatcher = ColorMatcher()
    colorMatcher.match(YUV(179, 56, 51))

else: 
    print("Device not found\n")