package com.pradeep.videoplayercollection;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Locale;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

public class TextSpeech extends AppCompatActivity implements
        TextToSpeech.OnInitListener, AdapterView.OnItemSelectedListener {
    private TextToSpeech mTextToSpeech;
    private ImageButton buttonSpeak;
    private EditText enterText;
    private String mLanguage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_speech);
        mTextToSpeech = new TextToSpeech(this, this);
        buttonSpeak = (ImageButton) findViewById(R.id.speeckButton);
        enterText = (EditText) findViewById(R.id.enterTextData);
        buttonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                speakOut();
            }

        });

        Spinner languageSelect = (Spinner) findViewById(R.id.select_language);
        languageSelect.setOnItemSelectedListener(this);
        final Locale[] availableLocales=Locale.getAvailableLocales();
        ArrayList <String> temps = new ArrayList<>();
        for(int i =0; i<availableLocales.length;i++) {
            Locale locale = availableLocales[i];
            temps.add(locale.getDisplayName());
            Log.e("Applog", ":" + locale.getDisplayName() + ":" + locale.getLanguage() + ":"
                    + locale.getCountry() + ":values-" + locale.toString().replace("_", "-r"));
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, temps);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSelect.setAdapter(dataAdapter);
    }

    @Override
    public void onDestroy() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTextToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                buttonSpeak.setEnabled(true);
                speakOut();
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    private void speakOut() {
        final Locale[] availableLocales=Locale.getAvailableLocales();
        int index = 0;
        for(int i =0; i<availableLocales.length;i++) {
            Locale locale = availableLocales[i];
            if(locale.getDisplayName().equals(mLanguage)) {
                index = i;
                break;
            }

        }
        mTextToSpeech.setLanguage(availableLocales[index]);
        Log.e("TTS", "Initilization Failed!"+mLanguage);
        String text = enterText.getText().toString();
        mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(TextSpeech.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(i);
        finish();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mLanguage = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), "Selected: " + mLanguage, Toast.LENGTH_LONG).show();
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

}
