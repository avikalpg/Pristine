# -*- coding: utf-8 -*-
from __future__ import unicode_literals
from django.shortcuts import render

# Create your views here.

from django.http import HttpResponse, Http404
from django.shortcuts import render, get_object_or_404
#from django.template import loader

from .models import RawInfo, TrackingInfo

import sys
sys.path.insert(0, '../../library/')
from  towertLocation import getTowerLocation
import json

def index(request):
    tracking_points_list = TrackingInfo.objects.order_by('-TimeStamp')
    #output = ', '.join([(t.Lat, t.Long) for t in tracking_points_list])
    #eturn HttpResponse("The visualisation of all the tracked points will appear here!<br>" + output)

    #template = loader.get_template('receiveCellInfo/index.html')
    context = {
        'tracking_points_list' : tracking_points_list,
    }
    #return HttpResponse(template.render(context, request))
    return render(request, 'receiveCellInfo/index.html', context)

def getLocation( cellInfo ):
    # The purpose of this function is to provide you with a GPS location for the device with some error
    test_string = ""
    for cell in cellInfo:
        if "lac" in cell.keys():
            test_string += "\nGSM :"
            (Lat, Long) = getTowerLocation()
            test_string += "Lat: " + str(Lat) + "; Long: " + str(Long)
        elif "tac" in cell.keys():
            test_string += "\nLTE :"
        else:
            test_string += "\nERROR: neither GSM not LTE cell tower information"
    return test_string

def listener(request):
    CID = "5011"
    LAC = "15001"
    MCC = "405"
    MNC = "861"
    Lat = 10.00
    Long = 1.00

    try:
#        device = request.POST['device']
#        time_stamp = request.POST['time']
#        CellInfo = request.POST['cellinfo']
#        MCC = request.POST['mcc']
#        MNC = request.POST['mnc']
        device = request.GET['device']
        time_stamp = request.GET['time']
        cellInfo = request.GET['cellinfo']
        MCC = request.GET['mcc']
        MNC = request.GET['mnc']
    except (KeyError):
        return render(request, 'receiveCellInfo/listener.html', {
            'message': "Invalid Raw Information entry requested!"
        })
    else:
        cellInfo = json.loads(cellInfo)
        testString = getLocation(cellInfo)
        return HttpResponse( testString + "\n" + str(len(cellInfo)) + " sets of cell tower information has been received for device ID: " + device + "<br>time-stamp: " + time_stamp)

def track(request, device_id):
    #trackedInfo = "<br>coming up"
    #return HttpResponse("Here are the LatLong positions where device " + device_id + ":" + trackedInfo )

    track_points = get_object_or_404(TrackingInfo, deviceID= device_id)
    context = {'track_points': track_points}
    return render( request, 'receiveCellInfo/track.html', context)


