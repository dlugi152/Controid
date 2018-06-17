package com.tomik.controid;

import android.util.Log;

public class Q_REMOVE_PLAYER extends Q_OBJECT {
    public String name;

    @Override
    public

    void executeQuery(QueuePack queuePack) {
        Log.i("network", "Q_REMOVE_PLAYER lockmode:" + NetworkManager.instance.lockMode);
        if (NetworkManager.instance.getNetworkState() == NetworkState.NET_SERVER &&
                NetworkManager.instance.lockMode == false) {
            NetworkManager.instance.removePlayer(name, queuePack.endpoint);
            Log.i("network", "Q_REMOVE_PLAYER done");
        }
    }
}
