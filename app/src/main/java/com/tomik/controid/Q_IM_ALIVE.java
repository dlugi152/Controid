package com.tomik.controid;

import android.util.Log;

public class Q_IM_ALIVE extends Q_OBJECT //obiekt oznaczający że komputer nie umarł
{
    @Override
    public void executeQuery(QueuePack queuePack) {
        Log.i("network", "Q_IM_ALIVE " + queuePack.endpoint.getAddress());
        NetworkManager.instance.setComputerTimeZero(queuePack.endpoint);
        NetworkManager.instance.sendToComputer(new Q_IM_ALIVE_RESPONSE(), queuePack.endpoint);
    }
}