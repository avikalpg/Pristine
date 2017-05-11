# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models

# Create your models here.

class RawInfo(models.Model):
    deviceID = models.CharField(max_length=200)
    TimeStamp = models.DateTimeField()
    cellID = models.CharField(max_length=10)
    LAC = models.CharField(max_length=10)
    dBm = models.FloatField(null=True)


class TrackingInfo(models.Model):
    deviceID = models.CharField(max_length=200)
    TimeStamp = models.DateTimeField()
    Lat = models.FloatField(null=True)
    Long = models.FloatField(null=True)
