package com.techbotbuilder.streamteamohio.ui;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Label implements UIElement {

    TextView text;

    Label(Context c, String name){
        text = new TextView(c);
        text.setText(name);
    }

    @Override
    public void setValue(String value) {
        text.setText(value);
    }

    @Override
    public void showOn(LinearLayout layout) {
        layout.addView(text);
    }
}
