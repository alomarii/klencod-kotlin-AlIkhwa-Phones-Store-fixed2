package com.alikhwa.phones;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class StoreApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this, new FirebaseOptions.Builder()
                    .setApiKey(FirebaseConfig.API_KEY)
                    .setApplicationId(FirebaseConfig.APP_ID)
                    .setProjectId(FirebaseConfig.PROJECT_ID)
                    .build());
        }
    }
}
