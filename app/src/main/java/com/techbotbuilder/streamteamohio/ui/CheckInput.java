package com.techbotbuilder.streamteamohio.ui;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class CheckInput implements UIElement {
    private int weight = 0;
    private boolean checked = false;

    CheckBox checkBox;
    public CheckInput(Context c, String name, String value, String weight){
        checkBox = new CheckBox(c);
        checkBox.setText(name);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            CheckInput.this.checked = isChecked;
        });
        this.weight = Integer.valueOf(weight);
        setValue(value);
    }

    @Override
    public void setValue(String value) {
        this.checked = Boolean.valueOf(value);
    }

    @Override
    public void showOn(LinearLayout layout) {
        layout.addView(checkBox);
    }
}
