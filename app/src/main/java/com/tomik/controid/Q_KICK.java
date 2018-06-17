package com.tomik.controid;

import android.util.Log;

public class Q_KICK extends Q_OBJECT {
    @Override
    public void executeQuery(QueuePack queuePack) {
        Log.i("network", "Q_KICK");
        if (NetworkManager.instance.getNetworkState() == NetworkState.NET_CLIENT)
            try {
                //todo kicknięcie
                //MenuManager.instance.setMainMenu();
            } catch (Exception ex) {
            }
    }
}
