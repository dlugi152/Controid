package com.tomik.controid;

import com.google.gson.Gson;

import java.io.Serializable;

import static com.tomik.controid.SendMode.SM_ALL_IN_NETWORK;

public class QueryPack implements Serializable
{
    public String json = "";
    public int port = 11001;
    public SendMode sendMode = SM_ALL_IN_NETWORK;
    public int targetPlayerId;
    public String type = "";

    public static String getJson(QueryPack q) {
        return new Gson().toJson(q);
    }

    public static QueryPack getObject(String json) {
        return new Gson().fromJson(json, QueryPack.class);
    }
}
