# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models

# Create your models here.

class RawInfo(models.Model):
    deviceID = models.CharField(max_length=200)
    TimeStamp = models.DateTimeField()
    cellID = models.CharField(max_length=10)
    LAC = models.CharField(max_length=10)
    MCC = models.CharField(max_length=5, default="")
    MNC = models.CharField(max_length=5, default="")
    dBm = models.FloatField(null=True)

    def __str__(self):
        return "device: %s, CID: %s, LAC: %s, MCC: %s, MNC: %s, Time: " % (self.deviceID, self.cellID, self.LAC, self.MCC, self.MNC) + str(self.TimeStamp)


class TrackingInfo(models.Model):
    deviceID = models.CharField(max_length=200)
    TimeStamp = models.DateTimeField()
    Lat = models.FloatField(null=True)
    Long = models.FloatField(null=True)
