package com.techbotbuilder.macroinvertebrateclassifier;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Network network;
    private ImageView imagePreview;
    private TextView resultView;
    private Button goButton;
    private Bitmap displayImage;
    static final int TAKE_IMAGE_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imagePreview = (ImageView) findViewById(R.id.image_preview);
        resultView = (TextView) findViewById(R.id.classification_result);
        goButton  = (Button) findViewById(R.id.take_image_button);
        if (displayImage != null) imagePreview.setImageBitmap(displayImage);
        if (network == null) loadNetwork();
    }

    private void loadNetwork(){
        String message;
        try {
            network = new Network();
            message = "Files loaded normally.";
        } catch (IOException exception){
            message = "Files failed to load! AAaaaahhhhh!??!!";
            goButton.setEnabled(false);
        }
        displayMessage(message);
    }

    public void takePhoto(View view){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePictureIntent, TAKE_IMAGE_CODE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_IMAGE_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            displayImage = ImageParser.resizeImage((Bitmap) extras.get("data"), 32, 32);
            imagePreview.setImageBitmap(displayImage);
            String results;
            try {
                results = network.identify(displayImage);
            } catch (IOException exception) {
                results = "Failed to load files needed to classify image";
            }
            displayMessage(results);
        }
    }

    protected void displayMessage(String message){
        resultView.setText(message);
    }
}
