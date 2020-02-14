package com.techbotbuilder.streamteamohio;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.techbotbuilder.streamteamohio.Utils.Notifier;

public class PrivacyActivity extends AppCompatActivity {

    public static final String PRIVACY_VERSION = "1";
    public static final String hasAcceptedKey = "HAS_ACCEPTED_PRIVACY";
    private CheckBox findBox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        findBox = findViewById(R.id.confirm_privacy);
    }

    public void openPrivacy(View v){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_url)));
        startActivity(browserIntent);
    }

    public void onAccept(View v){
        if (findBox.isChecked()) {
            getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)
                    .edit()
                    .putString(PrivacyActivity.hasAcceptedKey, PRIVACY_VERSION)
                    .apply();
            finish();
        }else{
            Notifier.notify(this, R.string.privacy_please_check);
        }
    }
}
