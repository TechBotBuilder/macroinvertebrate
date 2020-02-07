package com.techbotbuilder.streamteamohio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.techbotbuilder.streamteamohio.Utils.Notifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkFirstOpen();
    }

    /** Called when user taps Classify button */
    public void classifier(View view){
        Intent intent  = new Intent(this, ClassifierActivity.class);
        startActivity(intent);
    }
    public void reporter(View view){
        Notifier.notify(this, "Warning: work in progress");
        Intent intent  = new Intent(this, ChooseReportActivity.class);
        startActivity(intent);
    }
    public void options(View view){
        Notifier.notify(this, "Work in progress");
        /*
        Intent intent  = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    */}
    public void about(View view){
        Intent intent  = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    //TODO
    private static final String initFileName = "init.txt";
    private void checkFirstOpen(){
        boolean firstOpen = false;
        InputStream is = null;
        try{
            is = openFileInput(initFileName);
        }catch(FileNotFoundException e){
            firstOpen = true;
        } finally {
            if (is != null)
                try {is.close();} catch(IOException e){e.printStackTrace();}
        }

        if (!firstOpen) return;

        Notifier.longNotify(this, "FIRST LAUNCH");
        //if it is the first open, copy asset files into 'template' directory
        OutputStream os = null;
        try {
            String[] templates = getAssets().list("templates");
            //for (String templateName : templates){
            //    InputStream a = FormManager.getTemplateStream(this, templateName);
            //    InputStreamReader x = new InputStreamReader(a);
            //}
            os = openFileOutput(initFileName, Context.MODE_PRIVATE);
            os.write(new byte[]{});
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            if(os!=null)
                try{ os.close(); } catch(IOException e){e.printStackTrace();}
        }
    }
}
