# -*- coding: utf-8 -*-
# Generated by Django 1.11.1 on 2017-05-16 16:33
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('receiveCellInfo', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='rawinfo',
            name='MCC',
            field=models.CharField(default='', max_length=5),
        ),
        migrations.AddField(
            model_name='rawinfo',
            name='MNC',
            field=models.CharField(default='', max_length=5),
        ),
    ]
