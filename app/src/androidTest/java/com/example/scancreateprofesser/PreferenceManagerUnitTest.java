package com.example.scancreateprofesser;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;

import model.preference.PreferenceManager;

public class PreferenceManagerUnitTest {

    private PreferenceManager preference;

    @Before
    public void setUp(){
        Context context = ApplicationProvider.getApplicationContext();
        preference = PreferenceManager.getInstance(context);
    }

    @Test
    public void testSetAndReadPreference(){
        preference.setConfig_UnitTest("testKey", "testValue");
        assertEquals("testValue", preference.getConfig_UnitTest("testKey"));
    }
}