from django.conf.urls import url

from . import views

urlpatterns = [
    url(r'^$', views.index, name='index'),
    url(r'^location/$', views.listener, name='listener'),
    url(r'^rawinfo/$', views.raw_info, name='raw_info'),
    url(r'^eval/$', views.evaluateLocations, name='evaluateLocations'),
    url(r'^(?P<device_id>[A-Z][0-9]+)/$', views.track, name='track'),
]
