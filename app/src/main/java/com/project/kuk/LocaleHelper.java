package com.project.kuk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Locale;

public class LocaleHelper {

    private static final String LANGUAGE_KEY = "language";

    public static void setAppLanguage(Context context) {
        String languageCode = getSelectedLanguageCode(context);
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }

        Resources resources = context.getResources();
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration);
        }
    }

    public static String getSelectedLanguageCode(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return preferences.getString(LANGUAGE_KEY, "en");
    }

    public static void saveSelectedLanguage(Context context, String languageCode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LANGUAGE_KEY, languageCode);
        editor.apply();
    }
}

