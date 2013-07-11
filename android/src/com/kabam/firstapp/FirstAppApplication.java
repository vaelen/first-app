package com.kabam.firstapp;

import android.app.Application;
import android.content.Context;

public class FirstAppApplication extends Application {

    private static Context context;

    public void onCreate(){
        super.onCreate();
        FirstAppApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return context;
    }
	
}
