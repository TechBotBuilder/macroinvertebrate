package com.techbotbuilder.streamteamohio.ui;

import android.content.Context;
import android.text.InputType;

public class PhoneInput extends TextInput {
    public PhoneInput(Context c, String name, String currentValue, String hint){
        super(c, name, currentValue, hint);
        editText.setInputType(InputType.TYPE_CLASS_PHONE);
    }
}
