#!/usr/bin/env python3
## Author: Jack Pittenger
## Date: March 2021

# This works together with TorqueBallSeer.java to detect the
# yellow balls. It uses OpenCV to first mask the color, find
# countours on the mask, and then find the most prominent one.
# Information on the center of the largest is sent via network
# tables. A reset boolean network table entry can be used to 
# reset the current findings. The stream is viewable as
# "TargetStream" inside SmartDashboard.

import json
import time
import sys
import logging
import threading
import cv2 as cv
import numpy as np

from cscore import CameraServer, VideoSource, VideoMode
from networktables import NetworkTables, NetworkTablesInstance
from datetime import datetime

## CONFIG
configFile = "/boot/frc.json"

class CameraConfig:
    pass

team = 1477
ntServerIpAddress = "10.14.77.2"
cameraConfigs = []
width = 320
height = 240
fps = 20
frameAngleX = 60
processingScale = 1.25
frameWidth = int(width / processingScale)
frameHeight = int(height / processingScale)
frameCenter = (int(frameWidth/2), int(frameHeight/2))
frameArea = frameWidth * frameHeight

config = {"properties":[
    {"name":"connect_verbose","value":1},
    {"name":"raw_brightness","value":100},
    {"name":"brightness","value":10}, 
    {"name":"raw_contrast","value":0},
    {"name":"contrast","value":50},
    {"name":"raw_saturation","value":0},
    {"name":"saturation","value":50},
    {"name":"white_balance_temperature_auto","value":False},
    {"name":"power_line_frequency","value":2},
    {"name":"white_balance_temperature","value":4500},
    {"name":"raw_sharpness","value":0},
    {"name":"sharpness","value":0},
    {"name":"backlight_compensation","value":0},
    {"name":"exposure_auto","value":1},
    {"name":"raw_exposure_absolute","value":0},
    {"name":"exposure_absolute","value":0},
    {"name":"pan_absolute","value":0},
    {"name":"tilt_absolute","value":0},
    {"name":"zoom_absolute","value":0}]}

# Report parse error
def parseError(str):
    print("Config error in '" + configFile + "': " + str)


# Read configuration file
def readConfig():
    global team

    # Parse file
    try:
        with open(configFile, "rt") as f:
            parsed = json.load(f)
    except OSError as err:
        print("could not open '{}': {}".format(configFile, err))
        # print("could not open '{}': {}".format(configFile, err), file=sys.stderr)
        return False

    # Top level must be an object
    if not isinstance(parsed, dict):
        parseError("Must be JSON object")
        return False

    # Team number
    try:
        team = parsed["team"]
    except KeyError:
        parseError("Could not read team number")
        return False
    
    # Cameras
    try:
        cameras = parsed["cameras"]
    except KeyError:
        parseError("Could not read cameras")
        return False

    for camera in cameras:
        if not readCameraConfig(camera):
            return False

    return True

def readCameraConfig(config):
    cam = CameraConfig()

    # Name
    try:
        cam.name = config["name"]
    except KeyError:
        parseError("Could not read camera name")
        return False

    # Path
    try:
        cam.path = config["path"]
    except KeyError:
        parseError("Camera '{}': could not read path".format(cam.name))
        return False

    cam.config = config

    cameraConfigs.append(cam)
    return True

# Start running the camera
def startCamera(config):
    print("Starting camera '{}' on {}".format(config.name, config.path))
    camera = CameraServer.getInstance().startAutomaticCapture(name=config.name, path=config.path)
    camera.setConfigJson(json.dumps(config.config))

    return camera

## Vision logic

def findTarget(hsv, lowerBound, upperBound):
    mask = cv.inRange(hsv, lowerBound, upperBound)

    kernelOpen = np.ones((5,5))
    kernelClose = np.ones((20,20))

    maskOpen = cv.morphologyEx(mask, cv.MORPH_OPEN, kernelOpen)
    maskClose = cv.morphologyEx(maskOpen, cv.MORPH_CLOSE, kernelClose)
    
    maskFinal = maskClose

    _, conts, hierarchy = cv.findContours(maskFinal.copy(), cv.RETR_EXTERNAL, cv.CHAIN_APPROX_NONE)
    #cv.drawContours(maskFinal, conts, -1, (255,0,0),3)

    if len(conts) > 0:
        contour_sizes = [(cv.contourArea(contour), contour) for contour in conts]
        biggest_contour = max(contour_sizes, key=lambda x: x[0])[1]  
        x,y,w,h=cv.boundingRect(biggest_contour)
        cv.rectangle(maskFinal, (x,y), (x+w,y+h), (255,0,0), 2)
#        print(x,y,w,h)
        if w*h < 10: # may need to change
            return (False, [], maskFinal) 
        else:
            rect = cv.minAreaRect(biggest_contour)
            center, _, _ = rect
            center_x, center_y = center
            return (True, [center_x, center_y], maskFinal)
    else:
        return (False, [], maskFinal) # Check if frame doesn't work


## MAIN
def main(): 
    logging.basicConfig(level=logging.DEBUG)

    if len(sys.argv) >= 2:
        configFile = sys.argv[1]

    # read configuration
    if not readConfig():
        sys.exit(1)

    # start NetworkTables
    condition = threading.Condition()
    notified = [False]
    
    def connectionListener(connected, info):
        print(info, '; Connected=%s' % connected)
        with condition:
            notified[0] = True
            condition.notify()

    NetworkTables.initialize(server=ntServerIpAddress)
    NetworkTables.addConnectionListener(connectionListener, immediateNotify=True)

    with condition:
        print("NetworkTables initialized at {}, waiting...".format(ntServerIpAddress))
        if not notified[0]:
            condition.wait()

    print("NetworkTables connected to {}".format(ntServerIpAddress))
    ntinst = NetworkTablesInstance.getDefault()
    targetTable = ntinst.getTable("BallSeer")


    # setup a cvSource
    cs = CameraServer.getInstance()

    # Returns a cscore.VideoSource, used to automatically start a mjpegstream on port 1181
    camera = cs.startAutomaticCapture(name=cameraConfigs[0].name, path=cameraConfigs[0].path)

    # VideoMode.PixelFormat.kMJPEG, kBGR, kGray, kRGB565, kUnknown, kYUYV
    #camera.setVideoMode(VideoMode.PixelFormat.kYUYV, width, height, fps)

    # Load camera properties config
    camera.setConfigJson(json.dumps(config))

    # Get a CvSink. This will capture images from the camera
    cvSink = cs.getVideo()

    # Let the camera initialize/warm up
    time.sleep(2.0)
    
    # Preallocate a numpy empty array
    img = np.zeros(shape=(frameHeight, frameWidth, 3), dtype=np.uint8)

    # (Optional) setup a CvSource. This will send images back to the Dashboard, useful to see output from any image processing 
    targetOutputStream = cs.putVideo("TargetStream", frameWidth, frameHeight)

    targetTable.getEntry("frame_width").setDouble(frameWidth)
    targetTable.getEntry("frame_height").setDouble(frameHeight)

    resetEntry = targetTable.getEntry("reset")
    resetEntry.setBoolean(False)
    locationEntry = targetTable.getEntry("target_location")

    lowerBound = np.array([30,25,0])
    upperBound = np.array([100,75,5])
    
    while True:
        _, frame = cvSink.grabFrame(img)
        frame = cv.resize(frame, (frameWidth, frameHeight))
        hsv = cv.cvtColor(frame, cv.COLOR_BGR2RGB)
        
        targetExists, targetLocation, targetFrame = findTarget(hsv, lowerBound, upperBound)
        targetOutputStream.putFrame(targetFrame)
        if resetEntry.getBoolean(False):
            locationEntry.setDoubleArray([])
            resetEntry.setBoolean(False)
        elif targetExists:
            locationEntry.setDoubleArray(targetLocation)
            
        ntinst.flush()

if __name__ == "__main__":
    main()
