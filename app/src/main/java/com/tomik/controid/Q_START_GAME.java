package com.tomik.controid;

import android.util.Log;

public class Q_START_GAME extends Q_OBJECT {
    @Override

    public void executeQuery(QueuePack queuePack) {
        Log.i("network", "Q_START_GAME");
        if (NetworkManager.instance.getNetworkState() == NetworkState.NET_CLIENT)
            try {
                //todo serwer rozpoczął grę
                //  MenuManager.instance.startGameClient();
            } catch (Exception ex) {
            }
    }
}
