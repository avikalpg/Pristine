# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.shortcuts import render

# Create your views here.

from django.http import HttpResponse, Http404
#from django.template import loader
from django.shortcuts import render, get_object_or_404

from .models import RawInfo, TrackingInfo

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

def listener(request):
    CID = "5011"
    LAC = "15001"
    MCC = "405"
    MNC = "861"
    Lat = 10.00
    Long = 1.00

    try:
        device = request.POST['device']
        time_stamp = request.POST['time']
        CID = request.POST['cid']
        LAC = request.POST['lac']
        MCC = request.POST['mcc']
        MNC = request.POST['mnc']
        strength = request.POST['dBm']
    except (KeyError):
        return render(request, 'receiveCellInfo/listener.html', {
            'message': "Invalid Raw Information entry requested!"
        })
    else:
        return HttpResponse("The location of this cell tower (CID: %s, LAC: %s) is Lat: %f, Long: %f " % (CID, LAC, Lat, Long) + "<br>Device ID: " + device + "<br>time-stamp: " + time_stamp)

def track(request, device_id):
    #trackedInfo = "<br>coming up"
    #return HttpResponse("Here are the LatLong positions where device " + device_id + ":" + trackedInfo )

    track_points = get_object_or_404(TrackingInfo, deviceID= device_id)
    context = {'track_points': track_points}
    return render( request, 'receiveCellInfo/track.html', context)


