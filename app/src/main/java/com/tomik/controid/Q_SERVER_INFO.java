package com.tomik.controid;

import android.graphics.Color;

public class Q_SERVER_INFO extends Q_OBJECT //obiekt zawierający dane o serwerze
{
    public Color color;
    public String serverName;

    @Override
    public void executeQuery(QueuePack queuePack) {
        /*Debug.Log("Q_SERVER_INFO: "+ serverName+"\t"+ numberOfPlayers);
        GameObject gameObject = GameObject.Find("GameObject");
        Test test = gameObject.GetComponent<Test>();
        if (test != null)
            test.ip = queuePack.endpoint;*/

        //cos co doda kafelke w menu
        try {
            //todo wyświetl ten serwer
            //var obj = GameObject.Find("ServersContainer");
            //var sbm = obj.GetComponent < ServerButtonManager > ();
            //sbm.addData(serverName, queuePack.endpoint.Address.ToString(), color, queuePack.endpoint.Port);
        } catch (Exception ex) {
        }
    }
}
