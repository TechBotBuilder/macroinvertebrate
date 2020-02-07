package com.techbotbuilder.streamteamohio.ui;

import android.content.Context;

import java.util.Arrays;
import java.util.List;


/*
TODO: factor out DOM parsing so is all done in this class? or at least pass nodes here?
 */
public class UIFactory {

    private final static List<String> textBoxVariants =
            Arrays.asList("text", "date", "time", "number", "integer", "watershed");

    public static UIElement create(Context c, String nodeType, String name, String currentValue, String hint){
        UIElement x = new Label(c, name);
        switch(nodeType) {
            case "section": x = new Section(c, name); break;
            case "label": x = new Label(c, name); break;
            case "safety":
                break;
            case "help":
                break;
            case "text":
                TextInput textInput = new TextInput(c, name, currentValue, hint);
                /*int lines = FillReportActivity.ParseNodeAttributes.getInt(e, "lines", 1);
                if(lines > 1) {
                    textInput.editText.setLines(lines);
                    textInput.editText.setSingleLine(false);
                }*/
                x = textInput;
                break;
            case "integer": x = new IntegerInput(c, name, currentValue, hint); break;
            case "number": x = new NumberInput(c, name, currentValue, hint); break;
            case "date": x = new DateInput(c, name, currentValue, hint); break;
            case "time": x = new TimeInput(c, name, currentValue, hint); break;
            case "weather":
                break;
            case "photo":
                x = new PhotoInput(c, name, currentValue, hint);
                break;
            case "table":
                break;
            case "entry":
                break;
            case "checkbox":
                x = new Label(c, name);
//                thisView = checkboxes;
                break;
            case "box":
                x = new CheckInput(c, name, currentValue,"0"); //TODO
//                thisView = box;
                break;
            case "calculated":
                break;
            case "watershed":
                break;
            case "gpscoords":
                /*Button gpsButton = new Button(this);
                gpsButton.setText("Fill GPS coordinates with current location");
                gpsButton.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Notifier.notify(v.getContext(), "BUTTON pressed");
                            }
                        });
*/
                break;
            case "classifier":
                break;
            default:
                break;
        }
        return x;
    }
}
