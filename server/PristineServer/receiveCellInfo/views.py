# -*- coding: utf-8 -*-
from __future__ import unicode_literals
from django.shortcuts import render

# Create your views here.

from django.http import HttpResponse, Http404
from django.shortcuts import render, get_object_or_404
#from django.template import loader

from .models import RawInfo, TrackingInfo
import json
import requests
from bs4 import BeautifulSoup as bs
from datetime import datetime

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
    test_string = ""
    #CID = "5011"
    #LAC = "15001"
    #MCC = "405"
    #MNC = "861"
    #Lat = 10.00
    #Long = 1.00

    try:
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
        for cell in cellInfo:
            if "lac" in cell.keys():
                test_string += "\nGSM :"
                newRawInfo = RawInfo(
                    deviceID = device,
                    TimeStamp = datetime.fromtimestamp(float(time_stamp)),
                    cellID = cell["cellId"],
                    LAC = cell["lac"],
                    MCC = MCC,
                    MNC = MNC,
                    dBm = cell["dbm"]
				)
                newRawInfo.save()
            elif "tac" in cell.keys():
                test_string += "\nLTE :"
            else:
                test_string += "\nERROR: neither GSM not LTE cell tower information"
        #testString = getLocation(cellInfo, MNC, MCC)
        return HttpResponse( str(len(cellInfo)) + " sets of cell tower information has been received for device ID: " + device + "<br>time-stamp: " + time_stamp + test_string)

def track(request, device_id):
    #trackedInfo = "<br>coming up"
    #return HttpResponse("Here are the LatLong positions where device " + device_id + ":" + trackedInfo )

    track_points = get_object_or_404(TrackingInfo, deviceID= device_id)
    context = {'track_points': track_points}
    return render( request, 'receiveCellInfo/track.html', context)

def raw_info(request):
    raw_info = RawInfo.objects.order_by('-TimeStamp')
    context = {
        'raw_info' : raw_info,
    }
    return render(request, 'receiveCellInfo/index.html', context)
    

#######################################################################################
#######################################################################################
############################ HELPER FUNCTIONS #########################################
#######################################################################################
#######################################################################################

def isCSRFtag(tag):
    if tag.has_attr('name'):
        if "csrf" in tag['name']:
            return True
        else:
            return False
    else: 
        return False

# The source field shoyuld contain either "Google" or "Yandex" for desired results
def getTowerLocation( mcc, mnc, lac, cid, source = "Google" ):

    url = "http://cellidfinder.com/cells"
    r = requests.get( url, headers={'Connection':"keep-alive"} )

    h = r.headers
    cookie = h["Set-Cookie"]

    response = bs( r.text, "html.parser" )
    for x in response.html.head.find_all(isCSRFtag):
        if x['name'] == "csrf-token":
            auth_token = x['content']

    BlogSessionCookie = r.cookies['_diy_blog_session']

    payload = {
        'authenticity_token':auth_token,
        'cell[mcc]':mcc, 
        'cell[mnc]':mnc, 
        'cell[lac]':lac,
        'cell[cid]':cid,
        'google_data':1, 
        'yandex_data':1, 
        'cell[average]':0, 
        'commit':'Search+CellID'
    }

    cookies = {
        '_diy_blog_session':BlogSessionCookie, 
        '_ym_isad':"2", 
        '_ym_uid':"1494487308881011121", 
        'current_locale':"en"
    }

    headers = {
        'Referer':'http://cellidfinder.com/', 
        'Connection':"keep-alive", 
        'Host':"cellidfinder.com", 
        'Cookie':cookie,
        'User-Agent': "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0",
        'Accept': "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        'Accept-Language': "en-US,en;q=0.5",
        'Accept-Encoding': "gzip, deflate",
        'DNT':"1",
        'Upgrade-Insecure-Requests': "1"
    }

    r = requests.post( url , data=payload, headers = headers, cookies = cookies )

    coordinates = "(0.0, 0.0)"

    response = bs( r.text, "html.parser" )
    for x in response.html.body.find_all("div", {"class": "alert alert-success"}):
        element = x

    for x in element.find_all("p"):
        con = x.contents
        if source in con[0]:
            coordinates = x.next_sibling.next_sibling.contents
            coordinates = coordinates[0]
    coordinates = coordinates[2:-2]
    (Lat, Long) = coordinates.split(", ")
    (Lat, Long) = (float(Lat), float(Long))
    print "Lat: ", type(Lat), Lat, "\nLong: ", type(Long), Long
    return (Lat, Long)

def getLocation( cellInfo, mnc, mcc ):
    # The purpose of this function is to provide you with a GPS location for the device with some error
    test_string = ""
    for cell in cellInfo:
        if "lac" in cell.keys():
            test_string += "\nGSM :"
            (Lat, Long) = getTowerLocation(mcc, mnc, cell["lac"], cell["cellId"], source = "Google")
            test_string += "Lat: " + str(Lat) + "; Long: " + str(Long) + " -- strength: " + str(cell["dbm"])
        elif "tac" in cell.keys():
            test_string += "\nLTE :"
        else:
            test_string += "\nERROR: neither GSM not LTE cell tower information"
    return test_string

