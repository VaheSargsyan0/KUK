package com.project.kuk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LanguageActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        prefs = getSharedPreferences("Settings", MODE_PRIVATE);
    }

    public void onEnglishClick(View view) {
        changeLanguage("en", R.string.language_set_english);
    }

    public void onRussianClick(View view) {
        changeLanguage("ru", R.string.language_set_russian);
    }

    public void onArmenianClick(View view) {
        changeLanguage("hy", R.string.language_set_armenian);
    }

    private void changeLanguage(String languageCode, int messageId) {
        String currentLang = prefs.getString("App_Lang", "en");

        if (!currentLang.equals(languageCode)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("App_Lang", languageCode);
            editor.apply();

            LocaleHelper.saveSelectedLanguage(this, languageCode);
            LocaleHelper.setAppLanguage(this);

            restartMainActivity();
        }

        showToast(messageId);
    }

    private void restartMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showToast(int messageId) {
        Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
    }
}


