package com.techbotbuilder.streamteamohio;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.techbotbuilder.streamteamohio.Utils.Notifier;
import com.techbotbuilder.streamteamohio.forms.FormManager;

public class FillReportActivity extends AppCompatActivity {

    private FormManager formManager;

    private LinearLayout contentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_report);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        contentLayout = findViewById(R.id.contentLayout);
        try {
            formManager = new FormManager(this, contentLayout, extras);
        }catch(Exception e){
            LinearLayout x = findViewById(R.id.contentLayout);
            TextView em = new TextView(this);
            em.setText(R.string.file_not_found);
            x.addView(em);
        }

    }

    @Override
    public void onBackPressed(){
        //TODO: prompt user to save over old data, save as new, or discard.
        AlertDialog.Builder x = new AlertDialog.Builder(this);
        x.setMessage(R.string.report_save_before_exit);
        x.setPositiveButton(R.string.report_confirm_save_new,  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: save
                Notifier.notify(FillReportActivity.this, "Saving (TODO)");
                //formManager.saveNew();
                finish();
            }
        });
        x.setNegativeButton(R.string.report_deny_quit_nosave, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Notifier.notify(FillReportActivity.this, "Closing without saving.");
                dialog.dismiss();
                finish();
            }
        });
        x.create();
        x.show();

    }

    /*@Override
    protected void onDestroy(){
        super.onDestroy();

    }*/


}
