#!/usr/bin/env python2
from __future__ import division
import smbus
import time
import math
import random
import glob
import re
from YUV import *
from RGB import *
from colormatcher import *
from xml.dom.minidom import parse

#printer
from Adafruit_Thermal import *
 
def findRecipe(tags):
    print("called findRecipe with tags: " + str(tags))
    completeMatches = []
    minusOneMatches = []
    
    for filePath in glob.glob("recipes/*.xml"):
        recipeDOM = parse(filePath)
        templist = recipeDOM.getElementsByTagName("tag")
	taglist = []
	for domElement in templist:
	    taglist.append(domElement.firstChild.nodeValue)
        print("taglist: " + str(taglist))
        #compare, get number of matches
        commonTags = set(taglist) & set(tags)
        print("commonTags: " + str(commonTags))
        #distribute to lists according to matches
        difference = len(tags) - len(commonTags)
        print("difference: " + str(difference))
        if difference == 0:
            completeMatches.append(filePath)
        elif difference == 1:
            minusOneMatches.append(filePath)

    if completeMatches:
        print("returning from findRecipe with " + str(completeMatches))
        return completeMatches
    elif minusOneMatches:
        print("returning from findRecipe with " + str(minusOneMatches))
        return minusOneMatches
    else:
        print("returning from findRecipe with None")
        return None

def printRecipe(recipePath, peopleCount):
    recipeDOM = parse(recipePath)

    #print title
    title = recipeDOM.getElementsByTagName("title")[0].firstChild.nodeValue

    printer.doubleHeightOn()
    print(title)
    printer.doubleHeightOff()
    printer.feed(1)

    #print blurb + author
    #blurb = recipeDOM.getElementsByTagName("blurb")[0].nodeValue
    author = recipeDOM.getElementsByTagName("author")[0].firstChild.nodeValue

    print("By " + author)
    printer.feed(1)

    printer.boldOn()
    print("Ingredients for "+str(peopleCount)+ " servings")
    printer.boldOff()

    # read ingredients, adjust quantity
    for ingredient in recipeDOM.getElementsByTagName("ingredient"):
        ingredientStr = "";
        
        # does only work if ingredients tag does only contain a <quantity> node and text
        for child in ingredient.childNodes:
            if child != None:
                if child.nodeName == 'quantity':
                    ingredientStr += (str(int(child.firstChild.nodeValue) * peopleCount if peopleCount != 0 else 1)) + " "
                else:
                    ingredientStr += child.nodeValue + " "
            
        ingredientStr = ' '.join(ingredientStr.split()) # replace all whitespace (space, tabs, ..) with single space
        print(ingredientStr)

    printer.feed(1)
    printer.boldOn()
    print("Preparation")
    printer.boldOff()

    # print preparation
    preparation = recipeDOM.getElementsByTagName('preparation')[0].firstChild.nodeValue
    print(' '.join(preparation.split()))
    
#init
printer = Adafruit_Thermal("/dev/ttyAMA0", 19200, timeout=5)
bus = smbus.SMBus(1)

# I2C address 0x29
# Register 0x12 has device ver. 
# Register addresses must be OR'ed with 0x80


# UNCOMMENT NEXT TWO LINES FOR SERIAL ACCESS
bus.write_byte(0x29,0x80|0x12)
ver = bus.read_byte(0x29)
#ver = 0x55
#version # should be 0x44

if ver == 0x44:
    
    # init color matcher
    colorMatcher = ColorMatcher()
    
    print('Device found\n')
    bus.write_byte(0x29, 0x80|0x00) # 0x00 = ENABLE register
    bus.write_byte(0x29, 0x01|0x02) # 0x01 = Power on, 0x02 RGB sensors enabled
    bus.write_byte(0x29, 0x80|0x14) # Reading results start register 14, LSB then MSB
    
    # main loop for reading colors
    while True:
        data = bus.read_i2c_block_data(0x29, 0)
        clear = data[1] << 8 | data[0]
        red = (data[3] << 8 | data[2]) / clear  * 255 # division by clear should give us a range from 0 - 1
        green = (data[5] << 8 | data[4]) / clear * 255
        blue = (data[7] << 8 | data[6]) / clear * 255
        
        # print color result
        crgb = "C: %s, R: %s, G: %s, B: %s\n" % (clear, red, green, blue)
        #print(crgb)
        
        # match color
        # all matching is done in YUV color space because of distance function
        
        colorMatcher.match(YUV.fromIntRgb(red, green, blue))
        
        # get list of matched colors, if null there are none
        matched = colorMatcher.checkMatchedColors()
        
        if matched:
            tags = []
            peopleCount = 0
            #iterate over matched colors
            for color in matched:
                print("found " + colorMatcher.colorMap.get(color))
                #ensures that persons aren't added to the tags
                if colorMatcher.colorMap.get(color) == "person":
                    peopleCount += 1
                else:
                    #don't add duplicate category-tags
                    if colorMatcher.colorMap.get(color) in tags:
                        break
                    else:
                        tags.append(colorMatcher.colorMap.get(color))        
            
            # find recipe here
            matchingRecipes = findRecipe(tags)

            if matchingRecipes is None:
                printer.println("Sorry, I couldn't find any recipes matching your requirements :(")
                printer.feed(2)
            else:
                # select random recipe from list
                recipeToPrint = random.choice(matchingRecipes)
                # print recipe here
                printRecipe(recipeToPrint, peopleCount)

        time.sleep(0.05)

elif ver == 0x55:
    printRecipe('recipes/recipe-asian-fried-noodles-veg-meat.xml', 2)
    
#     #for testing the color things
#     rgb1 = RGB.fromFloat(0.1, 1.0, 0.5)
#     print(rgb1.toString())
    
#     rgb1 = RGB(255, 0, 0)
#     print(rgb1.toString())
    
#     rgb1 = RGB.fromYUV(YUV(1,0.5,0.5))
#     print(rgb1.toString())
    
#     yuv = YUV(1.0,0.5,0.5)
#     print(yuv.toString())
    
#     yuv = YUV.fromFloatRgb(0,1,0)
#     print(yuv.getRGB().toString())
    
#     yuv = YUV.fromIntRgb(0,255,0)
#     print(yuv.getRGB().toString())
    
#     # init color matcher
#     colorMatcher = ColorMatcher()
#     colorMatcher.match(YUV.fromIntRgb(180, 58, 50))

else:
    print("Device not found\n")
