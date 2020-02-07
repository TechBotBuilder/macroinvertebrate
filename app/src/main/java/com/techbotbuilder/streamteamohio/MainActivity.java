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
    }

    @Override
    protected void onResume(){
        super.onResume();
        checkPrivacy();
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

    private void checkPrivacy(){
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        if (prefs.getString(PrivacyActivity.hasAcceptedKey, null)!=PrivacyActivity.PRIVACY_VERSION) {
            Notifier.longNotify(this, getString(R.string.privacy_required));
            Intent intent  = new Intent(this, PrivacyActivity.class);
            startActivity(intent);
        }

    }
}
