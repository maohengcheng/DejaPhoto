package cse110.group6.dejaphoto;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends AppCompatActivity implements OnItemSelectedListener {

    Spinner spinnerDialog;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        adapter = ArrayAdapter.createFromResource(this, R.array.time_entries, android.R.layout.simple_spinner_item);

        spinnerDialog = (Spinner) findViewById(R.id.spinner_dialog);
        spinnerDialog.setAdapter(adapter);
        spinnerDialog.setOnItemSelectedListener(Settings.this);

        String size = spinnerDialog.getSelectedItem().toString();
        int spinner_pos = spinnerDialog.getSelectedItemPosition();
        String[] size_values = getResources().getStringArray(R.array.time_values);
        long actual_size = Long.valueOf(size_values[spinner_pos]);

        Intent timeChange = new Intent();
        timeChange.putExtra("newtime", actual_size);
        setResult(RESULT_OK, timeChange);
        //finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        TextView spinnerDialogText = (TextView) view;
        Toast.makeText(this, "Background changes every " + spinnerDialogText.getText(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
