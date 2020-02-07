package com.techbotbuilder.streamteamohio.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.techbotbuilder.streamteamohio.Utils.Notifier;

public class PhotoInput implements UIElement {

    private Button photoButton;
    private Uri photoUri;

    /**
    @param currentValue path to image file or empty or null
     */
    public PhotoInput(Context c, String name, String currentValue, String hint){
        photoButton = new Button(c);
        photoButton.setText(name);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Notifier.notify(v.getContext(), "PHOTOBUTTON pressed");
            }
        });

        setValue(currentValue);
    }

    @Override
    public void setValue(String value) {
        photoUri = Uri.parse(value);

        //todo
    }

    @Override
    public void showOn(LinearLayout layout) {

        layout.addView(photoButton);
    }
}
