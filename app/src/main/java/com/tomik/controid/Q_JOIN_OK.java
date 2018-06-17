package com.tomik.controid;

import android.util.Log;

import java.net.InetSocketAddress;

public class Q_JOIN_OK extends Q_OBJECT //obiekt oznaczający fakt dołączenia do gry
{
    @Override
    public void executeQuery(QueuePack queuePack) {
        Log.i("network", "Q_JOIN_OK execute.");
        InetSocketAddress tmp = NetworkManager.instance.getJoinIp();
        boolean res = tmp.equals(queuePack.endpoint);
        if (res) {
            InetSocketAddress ip = queuePack.endpoint;
            NetworkManager.instance.acceptJoin(ip);
            //MenuManager.instance.setClientMenu();
            Log.i("network", "Q_JOIN_OK done.");
            //zmiana menu
        }
    }
}
