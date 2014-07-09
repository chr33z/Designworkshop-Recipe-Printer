#!/usr/bin/env python2

import time
import math
from YUV import *
from RGB import *

class ColorMatcher:
    MAX_TIME = 1000 # max waiting time for scanner
    
    SCANNING_MODE = False # set true to scan marbles

    timeLastMatch = 0 # last time a match was found

    # when adding colors to queue, interpolate color if distance over this value */
    maxColorDistance = 0.1;
    matchDistance = 0.06;

    # color buffer that hold the last detected colors */
    colors = [];
    
    # fields for scanning the neutral color
    scanStart = 0;
    neutral = None; # neutral YUV color
    averagedColor = None; # neutral YUV color
    scannedColors = [];
    scanDistance = 0.1;
    
    # colormap holding predefined colors to match
    colorMap = dict({
        YUV(180, 58, 50) : "meat",
        YUV(125, 87, 35) : "vegetables",
        YUV(63, 127, 52) : "carbs"
    })

    # previously matched YUV color
    previouslyMatched = None;

    # contains all matched colors including neutral values: YUV
    matched = []
    
    # includes all matched colors without neutral values: YUV
    matchedStripped = [];
    
    def match(self, color):
        if self.SCANNING_MODE:
            scanColor(color)
        else:
            # detect neutral color first
            # if self.neutral == None:
                #findNeutralColor(color);
                #return;

            # if neutral is found then proceed with matching colors
            #addColorToQueue(color);

            bestMatch = None; # bset color match: YUV

            # loop over color map
            for targetColor in self.colorMap:
                if color.distanceTo(targetColor) < self.matchDistance:
                    if bestMatch == None:
                        bestMatch = targetColor
                    else:
                        if color.distanceTo(targetColor) < color.distanceTo(bestMatch):
                            bestMatch = targetColor
    
                # only match color if it was not recognized before and max scanning time was not reached
                if bestMatch != None and bestMatch != self.previouslyMatched:
                    if self.timeLastMatch == 0 or int(time.time()) - self.timeLastMatch < self.MAX_TIME:
                        self.previouslyMatched = bestMatch
                        self.timeLastMatch = int(time.time())
                        self.matched.append(bestMatch)
                        print("matched")

    #scan YUV color
    def scanColor(self, color):
        
        if self.scanStart == 0:
            self.scanStart = int(time.time())
            
            
        if int(time.time.py) - self.scanStart < 3000:
            if self.neutral == None:
                self.neutral = color.copy()
            else:
                self.neutral = color.average(self.neutral)
        else:
            if color.distanceTo(self.neutral) > self.scanDistance:
                self.scannedColors.append(color.copy())
                
                y = 0.0
                u = 0.0
                v = 0.0
                n = 0.0
            
                for c in self.scannedColors:
                    y += c.y
                    u += c.u
                    v += c.v
                    n += 1.0
                    
                self.averagedColor = YUV().setcolorYUVv(y / n, u / n, v / n)  
            
                print("Scanned: " +color.toString())
                print("Averaged: " +averagedColor.toString())

    # find the neutral color: YUV
    def findNeutralColor(color):
        if self.scanStart == 0:
            self.scanStart = int(time.time())
            
            
        if int(time.time.py) - self.scanStart < 3000:
            if self.neutral == None:
                self.neutral = color.copy()
            else:
                self.neutral = color.average(self.neutral)

    # call this function periodically to check id a scanning process is over
    def checkMatchedColors():
        if len(self.matched) > 0 and int(time.time()) - self.timeLastMatch > self.MAX_TIME:
            self.matchedStripped[:] = []
            
            #remove all neutral colors from matched list
            for c in self.matched:
                if c.equals(self.neutral):
                    self.matchedStripped.append(c)
            
            self.matched[:] = []
            self.timeLastMatch = 0
            self.previouslyMatched = None
            return self.matched
        else:
            return None