package com.techbotbuilder.streamteamohio.ui;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.fragment.app.Fragment;

import com.techbotbuilder.streamteamohio.R;
import com.techbotbuilder.streamteamohio.Utils.Notifier;

import org.joda.time.LocalTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TimeInput implements UIElement {
    private Fragment container;
    private TextView text;
    public TimeInput(final Context c, String name, String currentValue, String hint){
        container = new Fragment(){
            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
                View v =  inflater.inflate(R.layout.time_input_button_fragment, container, false);
                text = v.findViewById(R.id.time_text);
                text.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
                TextView title = v.findViewById(R.id.time_name);
                title.setText(name);
                Button button = v.findViewById(R.id.time_button);
                button.setOnClickListener(v1 -> {

                    TimePickerDialog timePicker = new TimePickerDialog(c, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            text.setText(InternalTime.getDateStringFromPicker(view));
                        }
                    }, 0, 0, true);
                    if (!InternalTime.updatePickerFromDateString(timePicker, text.getText().toString())){
                        Notifier.notify(c, "Incorrect format for time");
                    }
                    timePicker.show();
                });
                return v;
            }
        };
    }

    @Override
    public void setValue(String value) {
        text.setText(value);
    }

    @Override
    public void showOn(LinearLayout layout) {
        layout.addView(container.getView());
    }

    private static class InternalTime {

        private static DateFormat getFormat(){
            return SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        }

        static String getDateStringFromPicker(TimePicker timePicker){
            LocalTime localTime = new LocalTime(timePicker.getHour(), timePicker.getMinute());
            return getFormat().format(localTime);
        }

        static boolean updatePickerFromDateString(TimePickerDialog timePicker, String dateString){
            LocalTime localTime;
            try{
                localTime = LocalTime.fromDateFields(getFormat().parse(dateString));
                timePicker.updateTime(localTime.getHourOfDay(), localTime.getMinuteOfHour());
            } catch(Exception e){
                localTime = LocalTime.now();
                timePicker.updateTime(localTime.getHourOfDay(), localTime.getMinuteOfHour());
                return false;
            }
            return true;
        }
    }
}
