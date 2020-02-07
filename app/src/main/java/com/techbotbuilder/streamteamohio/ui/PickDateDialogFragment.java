package com.techbotbuilder.streamteamohio.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.techbotbuilder.streamteamohio.Utils.Notifier;
import com.techbotbuilder.streamteamohio.R;

import org.joda.time.LocalDate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class PickDateDialogFragment extends DialogFragment {
    public static final String DATE_KEY = "DATE_KEY";
    private DateInput dateInput;
    private DatePicker datePicker;

    PickDateDialogFragment(DateInput date){
        this.dateInput = date;
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_pick_date, null);

        builder.setView(view)
                .setTitle(R.string.choose_date)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*InternalDate localDate = new InternalDate(datePicker.getYear(), datePicker.getMonth()+1, datePicker.getDayOfMonth());
                        //note the plus one on month: User uses months 1-12, android DatePicker uses 0-11.
                        String stringDate = localDate.getDate();
                        Notifier.longNotify(getContext(), Integer.toString(datePicker.getYear()));*/

                        dateInput.setValue(InternalDate.getDateStringFromPicker(datePicker));
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PickDateDialogFragment.this.getDialog().cancel();
                    }
                });

        datePicker = view.findViewById(R.id.date_picker);

        if(!dateInput.getValue().equals("")) {
            //load any dateInput information given to us
            /*InternalDate c = new InternalDate(dateInput.getValue());
            datePicker.updateDate(c.getYear(), c.getMonthOfYear() - 1, c.getDayOfMonth());
            //note the minus one; user uses months 1-12, android datePicker uses 0-11.  */

            if (!InternalDate.updatePickerFromDateString(datePicker, dateInput.getValue())){
                Notifier.notify(getContext(), "Invalid date format");
            }
        }

        return builder.create();
    }

    private static class InternalDate {
        private final static DateFormat dateFormat = SimpleDateFormat.getDateInstance();

        static String getDateStringFromPicker(DatePicker datePicker){
            LocalDate localDate = new LocalDate(datePicker.getYear(), monthFromAndroidToJoda(datePicker.getMonth()), datePicker.getDayOfMonth());
            String stringDate = dateFormat.format(localDate.toDate());
            return stringDate;
        }
        static boolean updatePickerFromDateString(DatePicker datePicker, String dateString){
            LocalDate localDate;
            try{
                localDate = LocalDate.fromDateFields(dateFormat.parse(dateString));
            } catch(Exception e){
                return false;
            }
            datePicker.updateDate(localDate.getYear(), monthFromJodaToAndroid(localDate.getMonthOfYear()), localDate.getDayOfMonth());
            return true;
        }

        private static int monthFromAndroidToJoda(int androidMonth){
            return androidMonth + 1;
        }
        private static int monthFromJodaToAndroid(int jodaMonth){
            return jodaMonth - 1;
        }
    }

/*    private static class InternalDate{
        static final String format = "%02d/%02d/%04d"; // mm/dd/yyyy
        int year;
        int month;
        int dayOfMonth;
        InternalDate(int y, int m, int d){
            setDate(y, m, d);
        }
        void setDate(int y, int m, int d){
            year = y;
            month = m;
            dayOfMonth = d;
        }
        int getYear(){
            return year;
        }
        int getMonthOfYear(){
            return month;
        }
        int getDayOfMonth(){
            return dayOfMonth;
        }
        String getDate(){
            return String.format(format, month, dayOfMonth, year);
        }
        InternalDate(String s){
            String[] mdy = s.split("/");
            month = Integer.parseInt(mdy[0]);
            dayOfMonth = Integer.parseInt(mdy[1]);
            year = Integer.parseInt(mdy[2]);
        }

    }*/
}
