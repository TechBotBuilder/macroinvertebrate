package com.techbotbuilder.streamteamohio.ui;

import android.content.Context;
import android.text.InputType;

public class NumberInput extends TextInput {
    public NumberInput(Context c, String name, String currentValue, String hint){
        super(c, name, currentValue, hint);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }
}
