import requests
import logging
import json
from bs4 import BeautifulSoup as bs

try:
    import http.client as http_client
except ImportError:
    import httplib as http_client

http_client.HTTPConnection.debuglevel = 1

logging.basicConfig()
logging.getLogger().setLevel(logging.DEBUG)
requests_log = logging.getLogger("requests.packages.urllib3")
requests_log.setLevel(logging.DEBUG)
requests_log.propagate = True

def temp(tag):
    if tag.has_attr('name'):
        if "csrf" in tag['name']:
            return True
        else:
            return False
    else: 
        return False

# The source field shoyuld contain either "Google" or "Yandex" for desired results
def getTowerLocation( source = "Google" ):

    url = "http://cellidfinder.com/cells"
    r = requests.get( url, headers={'Connection':"keep-alive"} )

    h = r.headers
    cookie = h["Set-Cookie"]

    response = bs( r.text, "html.parser" )
    for x in response.html.head.find_all(temp):
        if x['name'] == "csrf-token":
            auth_token = x['content']

    BlogSessionCookie = r.cookies['_diy_blog_session']

    payload = {
        'authenticity_token':auth_token,
        'cell[mcc]':405, 
        'cell[mnc]':861, 
        'cell[lac]':15001,
        'cell[cid]':22073,
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

def main():
    (Lat, Long) = getTowerLocation()

main()
