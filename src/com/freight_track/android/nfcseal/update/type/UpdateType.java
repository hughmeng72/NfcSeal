package com.freight_track.android.nfcseal.update.type;

//二级（1.手动更新2.自动更新（有网更新，只有WiFi更新，只有WiFi下载））
public enum UpdateType {
    checkupdate,
    autoupdate,
    autowifiupdate,
    autowifidown
}