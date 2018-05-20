package com.tomik.controid;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.*;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
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
        Log.i("TAG", "Test Start, idx = " + mainMenuActivity.IdxAll);
        TimeUnit.SECONDS.sleep(20);
        float[][] allReadingsAccel = mainMenuActivity.AllReadingsAccel;
        float[][] allReadingsRot = mainMenuActivity.AllReadingsRot;
        int idxAll = mainMenuActivity.IdxAll;
        Log.i("TAG", "Test Stop, idx = " + idxAll);
        for (int i = 0; i < idxAll; i++) {
            Log.i("ODCZYTACCEL", allReadingsAccel[i][0] + "," + allReadingsAccel[i][1] + "," + allReadingsAccel[i][2]);
            Log.i("ODCZYTROT", allReadingsRot[i][0] + "," + allReadingsRot[i][1] + "," + allReadingsRot[i][2]);
        }
    }
}