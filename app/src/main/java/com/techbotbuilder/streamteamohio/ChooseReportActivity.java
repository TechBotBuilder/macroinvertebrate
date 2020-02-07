package com.techbotbuilder.streamteamohio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.techbotbuilder.streamteamohio.forms.FormManager;

import java.util.List;
import java.util.Locale;

public class ChooseReportActivity extends AppCompatActivity {

    RadioGroup chooseNew, chooseOld;
    Button goNew, goOld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_report);

        chooseNew = (RadioGroup) findViewById(R.id.newFormChoice);
        chooseOld = (RadioGroup) findViewById(R.id.oldFormChoice);

        goNew = (Button) findViewById(R.id.goNewButton);
        goOld = (Button) findViewById(R.id.goOldButton);

        //populate the radio-groups
        List<String> formTypes = FormManager.getTemplates(this);
        if (formTypes==null || formTypes.isEmpty()){
            RadioButton error = new RadioButton(this);
            error.setText(R.string.no_template_forms_error);
            error.setClickable(false);
            chooseNew.addView(error);
            goNew.setClickable(false);
        } else {
            for (String formType : formTypes) {
                RadioButton choice = new RadioButton(this);
                choice.setText(formType);
                choice.setTag(formType);
                chooseNew.addView(choice);
            }
        }
        List<String> oldForms = FormManager.getOldForms(this);
        if (oldForms == null || oldForms.isEmpty()) {
            RadioButton error = new RadioButton(this);
            error.setText(R.string.no_prior_forms_error);
            error.setClickable(false);
            chooseOld.addView(error);
            goOld.setClickable(false);
        } else {
            for (String oldFormName : oldForms) {
                RadioButton choice = new RadioButton(this);
                FormManager.OldFormMetaData metaData = FormManager.getMeta(oldFormName);
                String finalText = String.format(Locale.US,
                        "%-15.15s  |  C:%s  |  M:%s  |  %10.10s",
                        metaData.name,
                        metaData.getDateCreated(),
                        metaData.getDateModified(),
                        metaData.formType);
                choice.setText(finalText);
                choice.setTag(oldFormName);
                chooseOld.addView(choice);
            }
        }
    }


    /**
     * Start new report, based on selection of report type.
     */
    public void newReport(View view){
        Bundle bundle = new Bundle();
        populateBundleFromRadioGroup(chooseNew, FormManager.TEMPLATE_ID_FLAG, bundle);
        _startReport(bundle);
    }

    public void oldReport(View view){
        Bundle bundle = new Bundle();
        populateBundleFromRadioGroup(chooseOld, FormManager.OLD_DATA_ID_FLAG, bundle);
        _startReport(bundle);
    }

    private void populateBundleFromRadioGroup(RadioGroup radioGroup, String id, Bundle bundle){
        View selectedRadio = findViewById(radioGroup.getCheckedRadioButtonId());
        String whichForm = null;
        if (selectedRadio != null) whichForm = selectedRadio.getTag().toString();
        bundle.putString(id, whichForm);
    }

    /**
     * Head over to the report-filling activity.
     */
    private void _startReport(Bundle extras){
        Intent intent = new Intent(this, FillReportActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }
}
