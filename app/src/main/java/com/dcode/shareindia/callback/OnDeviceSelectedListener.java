package com.dcode.shareindia.callback;

import com.dcode.shareindia.object.NetworkDevice;

import java.util.List;

public interface OnDeviceSelectedListener
{
    void onDeviceSelected(NetworkDevice.Connection connection, List<NetworkDevice.Connection> availableInterfaces);
}
