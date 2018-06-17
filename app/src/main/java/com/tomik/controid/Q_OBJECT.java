package com.tomik.controid;

import android.util.Log;

import com.google.gson.Gson;

import java.io.Serializable;

public abstract class Q_OBJECT implements Serializable {
    public abstract void executeQuery(QueuePack queuePack);

    public static Q_OBJECT Deserialize(String json, String type) throws Exception {
        Gson gson = new Gson();
        switch (type) {
            case "Q_SERVER_INFO":
                return gson.fromJson(json, Q_SERVER_INFO.class);
            case "Q_HELLO":
                return gson.fromJson(json, Q_HELLO.class);
            case "Q_JOIN_OK":
                return gson.fromJson(json, Q_JOIN_OK.class);
            case "Q_IM_ALIVE":
                return gson.fromJson(json, Q_IM_ALIVE.class);
            case "Q_IM_ALIVE_RESPONSE":
                return gson.fromJson(json, Q_IM_ALIVE_RESPONSE.class);
            case "Q_ADD_PLAYER":
                return gson.fromJson(json, Q_ADD_PLAYER.class);
            case "Q_REMOVE_PLAYER":
                return gson.fromJson(json, Q_REMOVE_PLAYER.class);
            case "Q_PLAYERS_LIST_ELEMENT":
                return gson.fromJson(json, Q_PLAYERS_LIST_ELEMENT.class);
            case "Q_PLAYERS_LIST_RESET":
                return gson.fromJson(json, Q_PLAYERS_LIST_RESET.class);
            case "Q_START_GAME":
                return gson.fromJson(json, Q_START_GAME.class);
            case "Q_KICK":
                return gson.fromJson(json, Q_KICK.class);
            case "Q_JUMP":
                return gson.fromJson(json, Q_JUMP.class);
            case "Q_CROUCH":
                return gson.fromJson(json, Q_CROUCH.class);
            case "Q_LEFT":
                return gson.fromJson(json, Q_LEFT.class);
            case "Q_RIGHT":
                return gson.fromJson(json, Q_RIGHT.class);
            default:
                //na wypadek błędu
                Log.i("network", "Q_OBJECT ERROR, Nieznany typ " + type);
                Log.i("network", "Zapomniałeś dopisać tą linię kodu w Q_OBJECT");
                throw new Exception("Q_OBJECT ERROR, Nieznany typ " + type);
        }
    }
}
