package com.tomik.controid;

import android.util.Log;

public class Q_HELLO extends Q_OBJECT //obiekt do testowania
{
    public String text;

    @Override
    public void executeQuery(QueuePack queuePack) {
        Log.i("network", "HELLO " + text);
    }
}