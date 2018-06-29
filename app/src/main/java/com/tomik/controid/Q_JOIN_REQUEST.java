package com.tomik.controid;

import android.util.Log;

class Q_JOIN_REQUEST extends Q_OBJECT {
    @Override
    public void executeQuery(QueuePack queuePack) {
        Log.i("network", "Q_JOIN_REQUEST execute.");
        if (NetworkManager.instance.getNetworkState() == NetworkState.NET_SERVER) {
            if (NetworkManager.instance.isKnownComputer(queuePack.endpoint)) //może dołączyć nawet w trakcie gry, jeżeli na chwilę go wywali
            {
                NetworkManager.instance.addComputer(queuePack.endpoint);
                NetworkManager.instance.sendToComputer(new Q_JOIN_OK(), queuePack.endpoint);
            }

            NetworkManager.instance.addComputer(queuePack.endpoint);
            NetworkManager.instance.sendToComputer(new Q_JOIN_OK(), queuePack.endpoint);
            //SceneManager.LoadScene("MainMenu");
            Log.i("network", "Q_JOIN_REQUEST done." + queuePack.endpoint.getAddress() + "\t" + queuePack.endpoint.getPort());
        }
    }
}