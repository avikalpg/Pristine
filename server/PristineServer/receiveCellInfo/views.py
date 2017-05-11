# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.shortcuts import render

# Create your views here.

from django.http import HttpResponse


def index(request):
    return HttpResponse("The visualisation of all the tracked points will appear here!")

def listener(request):
    CID = "5011"
    LAC = "15001"
    MCC = "405"
    MNC = "861"
    Lat = 10.00
    Long = 1.00
    return HttpResponse("The location of this cell tower (CID: %s, LAC: %s) is Lat: %f, Long: %f " % (CID, LAC, Lat, Long) )

def track(request, device_id):
    trackedInfo = "<br>coming up"
    return HttpResponse("Here are the LatLong positions where device " + device_id + ":" + trackedInfo )
