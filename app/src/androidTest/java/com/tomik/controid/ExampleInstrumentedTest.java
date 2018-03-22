package com.tomik.controid;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.tomik.controid", appContext.getPackageName());
    }

    @Rule
    public ActivityTestRule<MainMenuActivity> activityRule
            = new ActivityTestRule<>(
            MainMenuActivity.class,
            true,     // initialTouchMode
            true);   // launchActivity. False to customize the intent
    @Test
    public void exportToFile10s() throws Exception {
        // Context of the app under test.
        MainMenuActivity mainMenuActivity = activityRule.getActivity();
        //long startTime = System.currentTimeMillis();
        Log.i("TAG", "Test Start, idx = " + mainMenuActivity.IdxAll);
        //while (startTime + 10000000 < System.currentTimeMillis()) ;
        TimeUnit.SECONDS.sleep(20);
        float[][] allReadingsAccel = mainMenuActivity.AllReadingsAccel;
        float[][] allReadingsVel = mainMenuActivity.AllReadingsVel;
        float[][] allReadingsPos = mainMenuActivity.AllReadingsPos;
        int idxAll = mainMenuActivity.IdxAll;
        Log.i("TAG", "Test Stop, idx = " + idxAll);
        /*File fileAccel = new File("C:/controid/przysp.txt");
        File fileVel = new File("C:/controid/ped.txt");
        File filePos = new File("C:/controid/pozycja.txt");
        fileAccel.createNewFile();
        fileVel.createNewFile();
        filePos.createNewFile();*/
        /*PrintWriter writerAccel = new PrintWriter("przysp.txt", "UTF-8");
        PrintWriter writerVel = new PrintWriter("C:/controid/ped.txt", "UTF-8");
        PrintWriter writerPos = new PrintWriter("C:/controid/pozycja.txt", "UTF-8");*/
        for (int i = 0; i < idxAll; i++) {
            Log.i("ODCZYTACCEL",allReadingsAccel[i][0] + "," + allReadingsAccel[i][1] + "," + allReadingsAccel[i][2]);
            Log.i("ODCZYTCEL",allReadingsVel[i][0] + "," + allReadingsVel[i][1] + "," + allReadingsVel[i][2]);
            Log.i("ODCZYTPOS",allReadingsPos[i][0] + "," + allReadingsPos[i][1] + "," + allReadingsPos[i][2]);
        }
        /*writerAccel.close();
        writerVel.close();
        writerPos.close();*/
    }
}
