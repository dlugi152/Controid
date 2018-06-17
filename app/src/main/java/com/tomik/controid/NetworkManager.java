package com.tomik.controid;

import android.graphics.Color;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Queue;

import static com.tomik.controid.NetworkState.NET_CLIENT;
import static com.tomik.controid.NetworkState.NET_SERVER;
import static com.tomik.controid.SendMode.SM_ALL_IN_NETWORK;
import static com.tomik.controid.SendMode.SM_BROADCAST;
import static com.tomik.controid.SendMode.SM_TO_SERVER;
import static com.tomik.controid.SendMode.SM_TO_SERVER_TO_ALL;

class PlayerInfo {
    public Color color = new Color();
    //public Color color = new Color(0f, 1f, 0f, 0.5f);
    public int id;
    public InetSocketAddress ip;
    public boolean isAi;
    public String name = "";
    public PlayerInfo() {
        //Color opaqueRed = Color.argb() .valueOf(0xffff0000); // from a color int
        //color = Color.argb(0.5f,1.0f, 0.0f, 0.0f);
    }
}

class Computer {
    public InetSocketAddress ip;
    public float offlineTime;
    public int state = 0;
}

class QueuePack {
    public InetSocketAddress endpoint; //ip nadawcy z portem na który można wysyłać dane
    public QueryPack qp;
}

public class NetworkManager {
    private final int joinTimeout = 4000;
    private final int receiverTimeout = 100;
    private final int aliveTimeout = 1000;
    private final int kickTimeout = 10;
    public static NetworkManager instance = new NetworkManager(); //instancja

    private static int idCounter;

    public int broadcastPort = 11000; //port na którym musi chodzić serwer i na który będą wysyłane wiadomości broadcast.

    public List<Computer> computers;

    public int connectionPort = 11001; //port na którym chodzi klient, serwer pamięta port przez który może się komunikować z klientem.

    private Thread connector;

    private boolean disableTrigger;

    //public MainGame GameInstance;
    private Thread joiner;

    private InetSocketAddress joinSemaphore;
    private int listenerCounter;
    private boolean listenerErrorTrigger;
    public boolean lockMode; //tryb blokowania wiadomości z poza podłączonyh komputerów (tylko dla serwera)
    private InetSocketAddress myIp;

    private NetworkState networkState;

    public List<PlayerInfo> players;
    public int port = 11000;
    private Queue<QueuePack> receiveQueue;
    private Thread receiver;
    private Socket s;

    private Queue<QueuePack> sendQueue;

    private InetSocketAddress serverIp;
    private float serverOfflineTime;

    // Use this for initialization
    private NetworkManager() {
        networkState = NetworkState.NET_DISABLED;
        myIp = new InetSocketAddress(getMyIp(), port);
    }

    public void kickComputer(InetSocketAddress ip) {
        Log.i("network", "kickComputer");
        if (networkState == NET_SERVER) {
            if (myIp.equals(ip)) return;
            for (int i = 0; i < computers.size(); ++i) {
                boolean access = computers.get(i).ip.equals(ip);
                if (access) {
                    computers.remove(i);
                    for (int j = 0; j < players.size(); ++j) {
                        boolean access2 = players.get(j).ip.equals(ip);
                        if (access2) {
                            PlayerInfo deadPlayerInfo = players.get(j);
                            players.remove(j);
                            try {
                                //GameInstance.GetPlayerById(deadPlayerInfo.id);
                                //if (deadPlayer != null) deadPlayer.KillMe();
                            } catch (Exception ex) {
                            }

                            --j;
                        }
                    }

                    --i;
                    break;
                }
            }

            Q_KICK kick = new Q_KICK();
            instance.sendToComputer(kick, ip);
        }
    }

    public void addPlayer(String name, InetSocketAddress ip, boolean isAi, Color color) {
        PlayerInfo pi = new PlayerInfo();
        pi.color = color;
        pi.name = name;
        pi.id = idCounter++;
        pi.ip = ip;
        pi.isAi = isAi;
        players.add(pi);
        Log.i("network", "addPlayer: " + pi.name + "\t" + pi.id + "\t" + pi.ip + "\t" + pi.isAi);
        //foreach (var pl in players) Debug.Log("pl: " + pl.name + "\t" + pl.ip.Address + "\t" + pl.ip.Port);
        //foreach (var com in computers) Debug.Log("com: " + com.ip.Address + "\t" + com.ip.Port);
    }

    public void removePlayer(String name, InetSocketAddress ip) {
        Log.i("network", "removePlayer: " + name);
        boolean highPriority = ip.equals(myIp);
        for (int i = 0; i < players.size(); ++i) {
            boolean access = players.get(i).ip.equals(ip);
            if (access || highPriority)
                if (players.get(i).name.equals(name)) {
                    Log.i("network", "removed: " + name);
                    players.remove(i);
                    break;
                }
        }
    }

    //Wysyła obiekt do wszystkich urządzeń w domenie rozgłoszeniowej, nawet do samego siebie
    public void sendBroadcast(Object o) {
        String json = new Gson().toJson(o);
        QueryPack qp = new QueryPack();
        qp.json = json;
        qp.type = o.getClass().getName().replace("com.tomik.controid.","");//todo może się różnić
        qp.port = port;
        qp.sendMode = SM_BROADCAST;
        QueuePack queue = new QueuePack();
        queue.qp = qp;
        //queue.endpoint = new InetSocketAddress(InetAddress. .Broadcast, broadcastPort);
        sendQueue.add(queue);
        //Debug.Log("Broadcast");
    }

    //Pilnuje aby obiekt dotarł do każdego komputera w grze, poza komputerem z którego wysłano obiekt.
    public void sendToAllComputers(Object o) {
        if (networkState == NetworkState.NET_DISABLED || networkState == NetworkState.NET_ENABLED)
            return;
        String json = new Gson().toJson(o);
        QueryPack qp = new QueryPack();
        qp.json = json;
        qp.type = o.getClass().getName().replace("com.tomik.controid.","");//todo może się różnić
        qp.port = port;
        qp.sendMode = SM_ALL_IN_NETWORK;
        QueuePack queue = new QueuePack();
        queue.qp = qp;
        //throw new NotImplementedException();
        switch (networkState) {
            case NET_SERVER: {
                for (Computer comp : computers) {
                    InetSocketAddress ip = comp.ip;
                    if (ip.equals(myIp)) continue;
                    QueuePack tmp = new QueuePack();
                    tmp.endpoint = ip;
                    tmp.qp = queue.qp;
                    sendQueue.add(tmp);
                }

                break;
            }
            case NET_CLIENT: {
                queue.endpoint = serverIp;
                sendQueue.add(queue);
                break;
            }
            default:
                sendQueue.add(queue);
                break;
        }
    }

    //Wysyła obiekt do komputera o podanym ip
    public void sendToComputer(Object o, InetSocketAddress ip) {
        String json = new Gson().toJson(o);
        QueryPack qp = new QueryPack();
        qp.json = json;
        qp.type = o.getClass().getName().replace("com.tomik.controid.","");//todo może się różnić
        qp.port = port;
        qp.sendMode = SendMode.SM_COMPUTER;
        QueuePack queue = new QueuePack();
        queue.qp = qp;
        queue.endpoint = ip;
        sendQueue.add(queue);
    }

    //Wysyła obiekt do komputera na którym gra gracz o danym id, nawet jeżeli to komputer z którego wysłano obiekt.
    public void sendToPlayer(Object o, int playerId) {
        return;
        //throw new NotImplementedException();
        /*if (networkState == NetworkState.NET_DISABLED || networkState == NetworkState.NET_ENABLED)
            return;
        String json = new Gson().toJson(o);
        QueryPack qp = new QueryPack();
        qp.json = json;
        qp.type = o.getClass().getName().replace("com.tomik.controid.","");//todo może się różnić
        qp.targetPlayerId = playerId;
        qp.port = port;
        qp.sendMode = SM_PLAYER;
        QueuePack queue = new QueuePack();
        queue.qp = qp;
        switch (networkState) {
            case NET_CLIENT:
                queue.endpoint = serverIp;
                sendQueue.add(queue);
                break;
            case NET_SERVER:
                for (PlayerInfo player : players)
                    if (player.id == playerId) {
                        queue.endpoint = player.ip;
                        sendQueue.add(queue);
                        break;
                    }

                break;
        }*/
    }

    //Wysyła obiekt do serwera i serwer wysyła go do wszystkich komputerów łącznie z serwerem. Służy to głównie do traktowania gry jakby była na jakiejś chmurze (czyli model w którym użytkownik nie jest przypisany do stanowiska).
    public void sendToServerToAll(Object o) {
        if (networkState == NetworkState.NET_DISABLED || networkState == NetworkState.NET_ENABLED)
            return;
        String json = new Gson().toJson(o);
        QueryPack qp = new QueryPack();
        qp.json = json;
        qp.type = o.getClass().getName().replace("com.tomik.controid.","");//todo może się różnić
        qp.port = port;
        qp.sendMode = SM_TO_SERVER_TO_ALL;
        QueuePack queue = new QueuePack();
        queue.qp = qp;
        queue.endpoint = serverIp;
        sendQueue.add(queue);
    }

    //Wysyła obiekt do serwera, jeżeli serwer to wysyła to wyśle sam do siebie.
    public void sendToServer(Object o) {
        if (networkState == NetworkState.NET_DISABLED || networkState == NetworkState.NET_ENABLED)
            return;
        String json = new Gson().toJson(o);
        QueryPack qp = new QueryPack();
        qp.json = json;
        qp.type = o.getClass().getName().replace("com.tomik.controid.","");//todo może się różnić
        qp.port = port;
        qp.sendMode = SM_TO_SERVER;
        QueuePack queue = new QueuePack();
        queue.qp = qp;
        queue.endpoint = serverIp;
        sendQueue.add(queue);
    }

    public void runSerwer() {
        setStateServer();
    }

    public void connectToSerwer(InetSocketAddress ip) {
        //setStateEnabled();
        if (getNetworkState() != NetworkState.NET_DISABLED) {
            joinSemaphore = ip;
            joiner = new Thread(new Runnable() {
                @Override
                public void run() {
                    JoinThread();
                }
            });
            joiner.start();
            instance.sendToComputer(new Q_JOIN_REQUEST(), ip);
        }
    }

    private void JoinThread() {
        //Debug.Log("Odliczanie start !!!");
        try {
            Thread.sleep(joinTimeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        joinSemaphore = null;
        joiner = null;
        //Debug.Log("Odliczanie null !!!");
        //////////////////////////////
    }

    public InetSocketAddress getJoinIp() {
        return joinSemaphore;
    }

    //zmienia stan sieci na kliencki
    public void acceptJoin(InetSocketAddress ip) {
        setStateClient(ip);
        if (joiner != null) joiner.interrupt();//todo może nie działać
        joinSemaphore = null;
        joiner = null;

        connector = new Thread(new Runnable() {
            @Override
            public void run() {
                AliveThread();
            }
        });
        connector.start();
    }

    private void AliveThread() {
        while (getNetworkState() == NET_CLIENT) {
            /////////////////////////////////////
            try {
                Thread.sleep(aliveTimeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendToServer(new Q_IM_ALIVE());
        }
    }

    public void enableNetwork() {
        setStateEnabled();
    }

    public void disableNetwork() {
        setStateDisabled();
    }

    public boolean isKnownComputer(InetSocketAddress ip) {
        if (networkState == NET_SERVER) {
            boolean fail = false;
            for (Computer c : computers)
                if (c.ip.equals(ip))
                    fail = true;
            if (!fail) return true;
        }

        return false;
    }

    public boolean addComputer(InetSocketAddress ip) {
        if (networkState == NET_SERVER) {
            boolean fail = false;
            for (Computer c : computers)
                if (c.ip.equals(ip))
                    fail = true;
            if (!fail) {
                Computer computer = new Computer();
                computer.ip = ip;
                computers.add(computer);
                return true;
            }
        }

        return false;
    }

    //server nie odbiera wiadomości od obcych komputerów (start gry)
    public void setLockMode() {
        lockMode = true;
    }

    public void kill() {
        stopReceiver();
    }

    public NetworkState getNetworkState() {
        return networkState;
    }

    public void setComputerTimeZero(InetSocketAddress ip) {
        if (networkState != NET_SERVER) return;
        for (Computer comp : computers)
            if (comp.ip.equals(ip))
                comp.offlineTime = 0;
    }

    public void setServerTimeZero() {
        if (networkState != NET_CLIENT) return;
        serverOfflineTime = 0;
    }

    private long prevTime = 0;

    public void update() {
        if (networkState == NET_SERVER) {
            for (Computer comp : computers) {
                if (myIp.equals(comp.ip)) continue;
                long dTime = System.currentTimeMillis();
                if (dTime - prevTime > 1) comp.offlineTime += 1;
                prevTime = dTime;
            }

            List<Computer> fakeList = computers;
            for (int i = 0; i < fakeList.size(); ++i) {
                Computer comp = fakeList.get(i);
                if (comp.offlineTime > kickTimeout) kickComputer(comp.ip);
            }
        }

        if (networkState == NET_CLIENT) {
            long dTime = System.currentTimeMillis();
            if (dTime - prevTime > 1) serverOfflineTime += 1;
            prevTime = dTime;
        }

        if (disableTrigger) {
            Log.i("network", "Nieoczekiwany błąd. Sieć wyłączona.");
            disableTrigger = false;
            setStateDisabled();
        }

        if (listenerErrorTrigger) {
            listenerErrorTrigger = false;
            Log.i("network", "Błąd podczas tworzenia nasłuchiwania na porcie " + port +
                    ". Możliwe, że jest z jakiegoś powodu zajęty. Próbuje naprawić problem.");
            if (networkState == NET_SERVER || networkState == NET_CLIENT) {
                //setStateDisabled();
                setStateEnabled();
                Log.i("network", "Nie można naprawić problemu. Port jest blokowany przez inną aplikację.");
            }

            if (networkState == NetworkState.NET_ENABLED) {
                connectionPort++;
                setStateEnabled();
                Log.i("network", "Port został zmieniony na " + port);
            }
        }

        sendAllQueriesInQueue();
        executeAllQueriesInQueue();
        if (serverOfflineTime > kickTimeout) {
            Log.i("network", "Rozłączono z serwerem - TimeoutKick");
            //GameInstance.pauseMenu.GoToMainMenu();
            setStateDisabled();
        }
    }

    private void sendAllQueriesInQueue() {
        if (networkState != NetworkState.NET_DISABLED && sendQueue != null)
            for (; sendQueue.size() > 0; ) {
                QueuePack queue = sendQueue.remove();
                String json = QueryPack.getJson(queue.qp);
                sendObject(json, queue.endpoint);
            }
    }

    private void executeAllQueriesInQueue() {
        if (networkState != NetworkState.NET_DISABLED && receiveQueue != null)
            for (; receiveQueue.size() > 0; ) {
                QueuePack queue = receiveQueue.remove();
                Q_OBJECT query = null;
                try {
                    query = Q_OBJECT.Deserialize(queue.qp.json, queue.qp.type);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                query.executeQuery(queue);
                if (networkState == NetworkState.NET_DISABLED) return;
            }
    }

    public InetAddress getMyIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (!intf.getDisplayName().contains("wlan"))
                    continue;
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) return inetAddress;
                }
            }
        } catch (SocketException ex) {
            Log.e("network", ex.toString());
        }
        return null;
    }

    private void sendObject(String json, InetSocketAddress ip) {
        DatagramChannel channel = null;
        try {
            channel = DatagramChannel.open();
            byte[] sendbuf = json.getBytes();
            channel.send(ByteBuffer.wrap(sendbuf), ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Debug.Log("Message sent: " + json);
    }

    private void setStateServer() {
        setStateDisabled();
        port = broadcastPort;
        myIp = new InetSocketAddress(getMyIp(), port);
        runReceiver();
        if (networkState == NET_SERVER) return;
        networkState = NET_SERVER;
        players = new ArrayList<>();
        computers = new ArrayList<>();
        addComputer(myIp);
        serverIp = new InetSocketAddress(getMyIp(), broadcastPort);
    }

    private void setStateClient(InetSocketAddress serverIp) {
        setStateDisabled();
        runReceiver();
        if (networkState == NET_CLIENT) return;
        networkState = NET_CLIENT;
        this.serverIp = serverIp;
    }

    private void setStateEnabled() {
        port = connectionPort;
        setStateDisabled();
        runReceiver();
        if (networkState == NetworkState.NET_ENABLED) return;
        networkState = NetworkState.NET_ENABLED;
    }

    private void setStateDisabled() {
        serverOfflineTime = 0;
        lockMode = false;
        //port = connectionPort;
        myIp = new InetSocketAddress(getMyIp(), port);
        if (networkState == NetworkState.NET_DISABLED) return;
        sendQueue = null;
        receiveQueue = null;
        players = null;
        computers = null;
        serverIp = null;
        stopReceiver();
        networkState = NetworkState.NET_DISABLED;
    }

    private void runReceiver() {
        if (receiver == null) {
            sendQueue = new ArrayDeque<>();
            receiveQueue = new ArrayDeque<>();
            receiver = new Thread(new Runnable() {
                @Override
                public void run() {
                    ReceiverThread(Thread.currentThread(), listenerCounter++);
                }
            });
            receiver.start();
        }
    }

    private void stopReceiver() {
        if (receiver != null) {
            receiver.interrupt();
            receiver = null;
        }
    }


    private void ReceiverThread(Thread main, int id) {
        //Debug.Log("id:"+id+" NetworkManager - ReceiverThread Start");
        try {
            boolean done = false;

            while (!done) {
                //Thread.Sleep(1000);
                if (!main.isAlive()) throw new Exception("NetworkManager - Aplikacja zamknieta");
                try {
                    DatagramSocket socket = new DatagramSocket();
                    //Debug.Log("Waiting for broadcast");
                    byte[] buf = new byte[512];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);


                    socket.receive(packet);
                    String json = new String(packet.getData(), 0, packet.getLength());
                    QueryPack queryPack = new Gson().fromJson(json, QueryPack.class);
                    QueuePack queuePack = new QueuePack();
                    queuePack.endpoint = new InetSocketAddress(serverIp.getAddress(), queryPack.port);
                    queuePack.qp = queryPack;
                    processQueueMessage(queuePack);
                    //Console.WriteLine("Received broadcast from {0} :\n {1}\n",groupEP.ToString(),Encoding.ASCII.GetString(bytes, 0, bytes.Length));
                } catch (Exception e) {
                    //Debug.Log("Blad in");
                }
            }
        } catch (SocketException e) {
            Log.i("message", "id:" + id + " Port error");
            listenerErrorTrigger = true;
        } catch (Exception e) {
            Log.i("message", "id:" + id + " Blad");
            disableTrigger = true;
        } finally {
            //Debug.Log("id:" + id + " NetworkManager - ReceiverThread Stop");
        }
    }

    private void processQueueMessage(QueuePack queuePack) {
        boolean wtf = !queuePack.endpoint.equals(serverIp);
        if (networkState == NetworkState.NET_CLIENT && wtf) return;
        if (networkState == NetworkState.NET_SERVER && lockMode && !isKnownComputer(queuePack.endpoint))
            return;
        switch (queuePack.qp.sendMode) {
            case SM_BROADCAST:
                if (!queuePack.endpoint.equals(myIp))
                    receiveQueue.add(queuePack);
                break;
            case SM_ALL_IN_NETWORK:
                receiveQueue.add(queuePack);
                if (this.networkState == NetworkState.NET_SERVER) {
                    InetSocketAddress source = queuePack.endpoint;
                    for (Computer computer : computers) {
                        if (source.equals(computer.ip) || myIp.equals(computer.ip)) continue;
                        QueuePack tmp2 = new QueuePack();
                        tmp2.endpoint = computer.ip;
                        tmp2.qp = queuePack.qp;
                        tmp2.qp.port = serverIp.getPort();
                        sendQueue.add(tmp2);
                    }
                }
                break;
            case SM_PLAYER:
                if (this.networkState == NetworkState.NET_SERVER) {
                    for (PlayerInfo player : players) {
                        if (player.id == queuePack.qp.targetPlayerId) {
                            queuePack.endpoint = player.ip;
                            sendQueue.add(queuePack);
                            break;
                        }
                    }
                } else {
                    receiveQueue.add(queuePack);
                }
                break;
            case SM_TO_SERVER_TO_ALL:
                for (Computer comp : computers) {
                    InetSocketAddress ip = comp.ip;
                    QueryPack tmp = queuePack.qp;
                    tmp.sendMode = SendMode.SM_COMPUTER;
                    QueuePack queue = new QueuePack();
                    queue.qp = tmp;
                    queue.endpoint = ip;
                    sendQueue.add(queue);
                }
                break;
            case SM_TO_SERVER:
                receiveQueue.add(queuePack);
                break;
            default:
                receiveQueue.add(queuePack);
                break;
        }
    }
    /*public void SetGameInstance(MainGame mainGame) {
        if (GameInstance == null)
            GameInstance = mainGame;
    }*/
}