package com.techbotbuilder.streamteamohio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.techbotbuilder.streamteamohio.Utils.Notifier;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkFirstOpen();
    }

    /** Called when user taps Classify button */
    public void classifier(View view){ goToClassifier(); }
    private void goToClassifier(){
        Intent intent  = new Intent(this, ClassifierActivity.class);
        startActivity(intent);
    }
    public void about(View view){
        Intent intent  = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void checkFirstOpen(){
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String firstRunKey = "FIRST_RUN";
        if (prefs.getBoolean(firstRunKey, true)) {
            Notifier.longNotify(this, "Please provide your feedback!");
            prefs.edit().putBoolean(firstRunKey, false).apply();
        }

    }
}
