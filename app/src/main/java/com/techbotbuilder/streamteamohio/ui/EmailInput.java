package com.techbotbuilder.streamteamohio.ui;

import android.content.Context;
import android.text.InputType;

public class EmailInput extends TextInput {
    public EmailInput(Context c, String name, String currentValue, String hint) {
        super(c, name, currentValue, hint);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }
}
