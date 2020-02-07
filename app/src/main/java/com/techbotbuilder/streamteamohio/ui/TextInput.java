package com.techbotbuilder.streamteamohio.ui;

import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


public class TextInput implements UIElement {
    protected TextView label;
    protected EditText editText;


    public TextInput(Context c, String name, String currentValue, String hint){
        label = new TextView(c);
        editText = new EditText(c);
        label.setLabelFor(editText.getId());

        editText.setInputType(InputType.TYPE_CLASS_TEXT);

        label.setText(name);
        editText.setHint(hint);

        setValue(currentValue);
        label.setPadding(10,10,8,0);
        editText.setPadding(10,0,8,10);
    }

    @Override
    public void setValue(String value) {
        editText.setText(value);
    }

    @Override
    public void showOn(LinearLayout layout) {
        layout.addView(this.label);
        layout.addView(this.editText);
    }
}
