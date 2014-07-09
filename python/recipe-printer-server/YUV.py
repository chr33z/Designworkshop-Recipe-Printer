#!/usr/bin/env python2

# YUV color class
# colors are stored as yuv values in range
# y = [0 - 1]
# u = [-0.5 - 0.5]
# v = [-0.5 - 0.5]

import math

class YUV(object):
    y = 0
    u = 0
    v = 0
    
    convMatrix = [
        0.299, 0.587, 0.114,
        -0.14713, -0.28886, 0.436,
        0.615, -0.51499, -0.10001]
    
    def __init__(self, r, g, b):
        self.setcolorRGBvint(r,g,b)
    
    def setcolorYUVv(self, y, u, v):
        self.y = y
        self.u = u
        self.v = v
        
    def setcolorYUVo(self, yuv):
        self.y = yuv.y
        self.u = yuv.u
        self.v = yuv.v
        
    def setcolorRGBvint(self, r, g, b):
        red = r / 255.0;
        green = g / 255.0;
        blue = b / 255.0;
        
        self.y = self.convMatrix[0] * red + self.convMatrix[1] * green + self.convMatrix[2] * blue
        self.u = self.convMatrix[3] * red + self.convMatrix[4] * green + self.convMatrix[5] * blue
        self.v = self.convMatrix[6] * red + self.convMatrix[7] * green + self.convMatrix[8] * blue
        
    def setcolorRGBv(self, r, g, b):
        r = r / 255.0;
        g = g / 255.0;
        b = b / 255.0;
        
        self.y = self.convMatrix[0] * r + self.convMatrix[1] * g + self.convMatrix[2] * b
        self.u = self.convMatrix[3] * r + self.convMatrix[4] * g + self.convMatrix[5] * b
        self.v = self.convMatrix[6] * r + self.convMatrix[7] * g + self.convMatrix[8] * b
        
    def setcolorRGBo(self, rgb):
        self.y = self.convMatrix[0] * rgb.r + self.convMatrix[1] * rgb.g + self.convMatrix[2] * rgb.b
        self.u = self.convMatrix[3] * rgb.r + self.convMatrix[4] * rgb.g + self.convMatrix[5] * rgb.b
        self.v = self.convMatrix[6] * rgb.r + self.convMatrix[7] * rgb.g + self.convMatrix[8] * rgb.b
    
    def setcolorHEX(self, hexString):
        if len(hexString) == 6:
            r = int(hexString[0:2], 16) / 255.0
            g = Integer.parseInt(hexString[2,4], 16) / 255.0
            b = Integer.parseInt(hexString[4:], 16) / 255.0

            self.y = self.convMatrix[0] * r + self.convMatrix[1] * g + self.convMatrix[2] * b
            self.u = self.convMatrix[3] * r + self.convMatrix[4] * g + self.convMatrix[5] * b
            self.v = self.convMatrix[6] * r + self.convMatrix[7] * g + self.convMatrix[8] * b
        else:
            print("RGB: not a valid hex value")
     
    def distanceTo(self, yuv):
        return math.sqrt((self.u-yuv.u)*(self.u-yuv.u) + (self.v-yuv.v)*(self.v-yuv.v) + (self.y-yuv.y)*(self.y-yuv.y))
    
    def average(self, yuv):
        ny = (self.y + yuv.y) / 2.0;
        nu = (self.u + yuv.u) / 2.0;
        nv = (self.v + yuv.v) / 2.0;
        
        result = YUV(0,0,0)
        result.setcolorYUVv(ny, nu, nv)
        return result
    
    def equals(self, yuv):
        if yuv == None:
            return false
        
        if self.y == yuv.y and self.u == yuv.u and self.v == yuv.v:
            return true
        else:
            return false
        
    def toString(self):
        return "["+str(self.y)+","+str(self.u)+","+str(self.v)+"]"
    
    def copy(self):
        return YUV().setcolorYUVv(self.y, self.u, self.v)