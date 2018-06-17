package com.tomik.controid;

import android.util.Log;

public class Q_PLAYERS_LIST_ELEMENT extends Q_OBJECT {
    //public Color color = new Color(1f, 0f, 0f, 0.5f);

    //public IPEndPoint ip;
    public int id = 0;
    public String ip;
    public boolean isAi;
    public String name;
    public int port;

    @Override
    public void executeQuery(QueuePack queuePack) {
        Log.i("network","Q_PLAYERS_LIST_ELEMENT");
        /*if (NetworkManager.instance.getNetworkState() == NetworkState.NET_CLIENT)
            try {
                var obj = GameObject.Find("ClientPlayersList");
                var sbm = obj.GetComponent < ComputersListManagerClient > ();
                sbm.addElement(new IPEndPoint(IPAddress.Parse(ip), port), name, isAi, color, id);
            } catch {
        }*/
    }
}
