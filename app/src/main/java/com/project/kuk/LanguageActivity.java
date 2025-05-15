package com.project.kuk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LanguageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
    }

    public void onEnglishClick(View view) {
        setLanguage("en");
        Toast.makeText(this, "Language set to English", Toast.LENGTH_SHORT).show();
    }

    public void onRussianClick(View view) {
        setLanguage("ru");
        Toast.makeText(this, "Язык установлен на русский", Toast.LENGTH_SHORT).show();
    }

    public void onArmenianClick(View view) {
        setLanguage("hy");
        Toast.makeText(this, "Լեզուն սահմանված է հայերեն", Toast.LENGTH_SHORT).show();
    }

    private void setLanguage(String languageCode) {
        LocaleHelper.saveSelectedLanguage(this, languageCode);
        LocaleHelper.setAppLanguage(this);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

