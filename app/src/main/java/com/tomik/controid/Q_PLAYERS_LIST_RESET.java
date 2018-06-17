package com.tomik.controid;

public class Q_PLAYERS_LIST_RESET extends Q_OBJECT {
    @Override
    public void executeQuery(QueuePack queuePack) {
        //Debug.Log("Q_PLAYERS_LIST_RESET");
        if (NetworkManager.instance.getNetworkState() == NetworkState.NET_CLIENT)
            try {
                //var obj = GameObject.Find("ClientPlayersList");
                //var sbm = obj.GetComponent < ComputersListManagerClient > ();
                //sbm.killElementsWithLogic();
            } catch (Exception ex) {
            }
    }
}
