package com.techbotbuilder.streamteamohio.ui;

import android.content.Context;
import android.text.InputType;

public class IntegerInput extends NumberInput {
    public IntegerInput(Context c, String name, String currentValue, String hint) {
        super(c, name, currentValue, hint);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
    }
}
