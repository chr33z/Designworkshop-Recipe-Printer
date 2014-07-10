#!/usr/bin/env python2

import time
import math
from YUV import *
from RGB import *

class ColorMatcher:
    MAX_TIME = 1 # max waiting time for scanner
    
    SCANNING_MODE = False # set true to scan marbles

    timeLastMatch = 0 # last time a match was found

    # maxColorDistance = 0.1; # not needed; 
    matchDistance = 0.04; # color distance to color in the colormap

    # color buffer that hold the last detected colors */
    colors = [];
    
    # fields for scanning
    scanStart = 0;
    neutral = None; # neutral YUV color
    averagedColor = None; # neutral YUV color
    scannedColors = [];
    scanDistance = 0.1;
    
    # colormap holding predefined colors to match
    colorMap = dict({
        YUV.fromIntRgb(138, 64, 52) : "meat",
        YUV.fromIntRgb(89, 94, 61) : "vegetables",
        YUV.fromIntRgb(115, 85, 39) : "carbs",
        YUV.fromIntRgb(69, 85, 88) : "person"
    })

    # previously matched YUV color
    previouslyMatched = None;

    # contains all matched colors including neutral values: YUV
    matched = []
    
    # includes all matched colors without neutral values: YUV
    matchedStripped = [];
    
    def match(self, color):
        if self.SCANNING_MODE:
            self.scanColor(color)
        else:
            # detect neutral color first
            if not "neutral" in self.colorMap.values():
                # find neutral color; this takes 3000 ms
                self.findNeutralColor(color);
                return;

            # if neutral is found then proceed with matching colors
            #addColorToQueue(color);

            bestMatch = None; # best color match: YUV

            # loop over color map to find matches
            for targetColor in self.colorMap:
                # see if the distance from the scanned color to the colorMap is small enough
		if color.distanceTo(targetColor) < self.matchDistance:
                    if bestMatch == None:
                        bestMatch = targetColor
                    else: # if we have multiple matches, only take the best one (smalles distance)
                        if color.distanceTo(targetColor) < color.distanceTo(bestMatch):
                            bestMatch = targetColor
    
                # only match color if it was not recognized before and max scanning time was not reached
                # since we also detect neutral color a list could look like this:
                # red - neutral - red - neutral - green
                if bestMatch != None and bestMatch != self.previouslyMatched:
                    # only add color to the matched colors list if the last matching time was not
                    # more than MAX_TIME ago
                    if self.timeLastMatch == 0 or int(time.time()) - self.timeLastMatch < self.MAX_TIME:
                        self.previouslyMatched = bestMatch
                        self.timeLastMatch = int(time.time()) # save time of this match
                        self.matched.append(bestMatch)
                        print("matched")
			for color in self.matched:
			    print("matched color:" + str(self.colorMap.get(bestMatch)))

    # scan a single YUV color
    # the first 1000 ms of the scanning process the neutral color is found
    # Insert the marble multiple times to form an average value
    def scanColor(self, color):
	print("in scanColor, y:" + str(color.y) + " u:"+ str(color.u) + " v:"+ str(color.v))
	print("scanstart:" + str(self.scanStart) + " timediff:" + str(time.time() - self.scanStart))
        if self.scanStart == 0:
            self.scanStart = int(time.time())
            
        elif int(time.time()) - self.scanStart < 1:
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
                
                if self.averagedColor == None:
                    self.averagedColor = YUV(0,0,0)
                
                self.averagedColor = YUV(y / n, u / n, v / n)  
            
                print("Scanned: " +color.getRGB().toString())
                print("Averaged: " + self.averagedColor.getRGB().toString())

    # find the neutral color: YUV
    # takes 3000 ms
    def findNeutralColor(self, color):
	if self.scanStart == 0:
            self.scanStart = int(time.time())
            
        if int(time.time()) - self.scanStart < 1: # average neutral color
            if self.neutral == None:
                self.neutral = color.copy()
            else:
                self.neutral = color.average(self.neutral)
        else:
            self.colorMap[self.neutral] = "neutral" # add neutral color to colormap

    # call this function periodically to check if a scanning process is over
    # returns null if no scanning is done and no colors are detected
    # returns all matched colors with the neutral color filtered out
    def checkMatchedColors(self):
        if len(self.matched) > 0 and int(time.time()) - self.timeLastMatch > self.MAX_TIME:
            self.matchedStripped[:] = []
            
            #remove all neutral colors from matched list
            for c in self.matched:
                if c.equals(self.neutral):
                    self.matchedStripped.append(c)
            
            result = self.matched
            
            # reset values
            self.matched[:] = []
            self.timeLastMatch = 0
            self.previouslyMatched = None
            return result
        else:
            return None
