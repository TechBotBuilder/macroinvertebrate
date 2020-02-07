package com.techbotbuilder.streamteamohio.ui;

import android.content.Context;
import android.text.InputType;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class DateInput extends TextInput {
    public DateInput(final Context c, String name, String currentValue, String hint) {
        super(c, name, currentValue, hint);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create new dialog
                FragmentManager fm = ((AppCompatActivity)c).getSupportFragmentManager();
                PickDateDialogFragment datePicker = new PickDateDialogFragment(DateInput.this);
                datePicker.show(fm, "PickDateDialogFragment");
            }
        });
        //editText.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
    }

    public String getValue(){
        return editText.getText().toString();
    }
}
