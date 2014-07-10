#!/usr/bin/env python2

# YUV color class
# colors are stored as yuv values in range
# y = [0 - 1]
# u = [-0.5 - 0.5]
# v = [-0.5 - 0.5]

import math
from RGB import *

class YUV(object):
    y = 0
    u = 0
    v = 0
    
    convMatrix = [
        0.299, 0.587, 0.114,
        -0.14713, -0.28886, 0.436,
        0.615, -0.51499, -0.10001]
    
    def __init__(self, y=0, u=0, v=0):
        self.y = y
        self.u = u
        self.v = v
        
    @classmethod
    def fromIntRgb(cls, r=0, g=0, b=0):
        red = r / 255.0;
        green = g / 255.0;
        blue = b / 255.0;
        
        y = cls.convMatrix[0] * red + cls.convMatrix[1] * green + cls.convMatrix[2] * blue
        u = cls.convMatrix[3] * red + cls.convMatrix[4] * green + cls.convMatrix[5] * blue
        v = cls.convMatrix[6] * red + cls.convMatrix[7] * green + cls.convMatrix[8] * blue
        return cls(y, u, v)
    
    @classmethod
    def fromFloatRgb(cls, r=0, g=0, b=0):
        y = cls.convMatrix[0] * r + cls.convMatrix[1] * g + cls.convMatrix[2] * b
        u = cls.convMatrix[3] * r + cls.convMatrix[4] * g + cls.convMatrix[5] * b
        v = cls.convMatrix[6] * r + cls.convMatrix[7] * g + cls.convMatrix[8] * b
        return cls(y, u, v)
    
    @classmethod
    def fromYUV(cls, yuv=None):
        if yuv == None:
            return cls()
        else:
            return cls(yuv.y, yuv.u, yuv.v)
        
    @classmethod
    def fromRGB(cls, rgb=None):
        if yuv == None:
            return cls()
        else:
            y = cls.convMatrix[0] * rgb.r + cls.convMatrix[1] * rgb.g + cls.convMatrix[2] * rgb.b
            u = cls.convMatrix[3] * rgb.r + cls.convMatrix[4] * rgb.g + cls.convMatrix[5] * rgb.b
            v = cls.convMatrix[6] * rgb.r + cls.convMatrix[7] * rgb.g + cls.convMatrix[8] * rgb.b
            return cls(y, u, v)
    
    #def setcolorHEX(self, hexString):
    #    if len(hexString) == 6:
    #        r = int(hexString[0:2], 16) / 255.0
    #        g = Integer.parseInt(hexString[2,4], 16) / 255.0
    #        b = Integer.parseInt(hexString[4:], 16) / 255.0
    #
    #        self.y = self.convMatrix[0] * r + self.convMatrix[1] * g + self.convMatrix[2] * b
    #        self.u = self.convMatrix[3] * r + self.convMatrix[4] * g + self.convMatrix[5] * b
    #        self.v = self.convMatrix[6] * r + self.convMatrix[7] * g + self.convMatrix[8] * b
    #    else:
    #        print("RGB: not a valid hex value")
     
    def distanceTo(self, yuv):
        return math.sqrt((self.u-yuv.u)*(self.u-yuv.u) + (self.v-yuv.v)*(self.v-yuv.v) + (self.y-yuv.y)*(self.y-yuv.y))
    
    def average(self, yuv):
        ny = (self.y + yuv.y) / 2.0;
        nu = (self.u + yuv.u) / 2.0;
        nv = (self.v + yuv.v) / 2.0;
        
        return YUV(ny, nu, nv)
    
    def equals(self, yuv):
        if yuv == None:
            return false
        
        if self.y == yuv.y and self.u == yuv.u and self.v == yuv.v:
            return True
        else:
            return False
        
    def toString(self):
        return "["+str(self.y)+","+str(self.u)+","+str(self.v)+"]"
    
    def copy(self):
        return YUV(self.y, self.u, self.v)
    
    def getRGB(self):
        return RGB.fromFloatYUV(self.y, self.u, self.v)
        
