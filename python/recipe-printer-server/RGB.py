#!/usr/bin/env python2

import math

class RGB:
    
    r = 0
    g = 0
    b = 0
    
    convMatrix = [
        1, 0, 1.13983,
        1, -0.39465, -0.58060,
        1, 2.03211, -0
    ]
    
    def __init__(self, r, g, b):
        self.r = r / 255.0
        self.g = g / 255.0
        self.b = b / 255.0
    
    def setcolorRGBvint(self, r, g, b):
        self.r = r / 255.0
        self.g = g / 255.0
        self.b = b / 255.0

    def setcolorHEX(self, hexString):
        if len(hexString) == 6:
            self.r = int(hexString[0:2], 16) / 255.0
            self.g = int(hexString[2,4], 16) / 255.0
            self.b = int(hexString[4:], 16) / 255.0
        else:
            print("RGB: not a valid hex value")
    
    def setcolorYUVo(self, yuv):
        self.r = math.min((yuv.y + 1.140 * yuv.v), 1);
        self.g = math.min((yuv.y - 0.395 * yuv.u - 0.581 * yuv.v), 1);
        self.b = math.min((yuv.y + 2.032 * yuv.u), 1);
        
    def setHEX(self, hexString):
        if len(hexString) == 6:
            self.r = int(hexString[0:2], 16) / 255.0
            self.g = int(hexString[2,4], 16) / 255.0
            self.b = int(hexString[4:], 16) / 255.0
        else:
            print("RGB: not a valid hex value")
        
    def toString(self):
        return "["+str(self.r * 255)+","+str(self.g * 255)+","+str(self.b * 255)+"]";

