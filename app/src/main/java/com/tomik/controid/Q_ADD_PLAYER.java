package com.tomik.controid;

import android.graphics.Color;
import android.util.Log;

public class Q_ADD_PLAYER extends Q_OBJECT {
    public Color color ;//= Color. (0.5f, 1f, 1f);
    public boolean isAi;
    public String name;

    @Override
    public

    void executeQuery(QueuePack queuePack) {
        Log.i("network", "Q_ADD_PLAYER lockmode:" + NetworkManager.instance.lockMode);
        if (NetworkManager.instance.getNetworkState() == NetworkState.NET_SERVER &&
                NetworkManager.instance.lockMode == false) {
            NetworkManager.instance.addPlayer(name, queuePack.endpoint, isAi, color);
            Log.i("network", "Q_ADD_PLAYER done");
        }
    }
}
