package com.example.qrscanner;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Settings extends AppCompatActivity {

    private ImageView gear, backBtn, theme_indicator, currentActivity;
    private Switch switchTheme;
    private SharedPreferences sharedPref;
    private static final String MY_PREFS_NAME = "isDarkMode";
    private static final String KEY_PREFS_NAME = "isDark";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gear = findViewById(R.id.settingsIcon);
        gear.setVisibility(View.GONE);
        currentActivity = findViewById(R.id.currentActivity);
        currentActivity.setVisibility(View.GONE);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        switchTheme = findViewById(R.id.switchTheme);
        theme_indicator = findViewById(R.id.theme_indicator);

//        Saves the states
        sharedPref = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        switchTheme.setChecked(sharedPref.getBoolean(KEY_PREFS_NAME, true));
        boolean isSwitchThemeChecked = sharedPref.getBoolean(KEY_PREFS_NAME, true);
        switchTheme.setChecked(isSwitchThemeChecked);
        if (isSwitchThemeChecked) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            theme_indicator.setImageResource(R.drawable.ic_night_mode);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            theme_indicator.setImageResource(R.drawable.ic_light_mode);
        }

        switchTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = switchTheme.isChecked();
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(KEY_PREFS_NAME, isChecked);
                editor.apply();

                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    theme_indicator.setImageResource(R.drawable.ic_night_mode);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    theme_indicator.setImageResource(R.drawable.ic_light_mode);
                }


//                if (switchTheme.isChecked()) {
//                    SharedPreferences.Editor SettingsEdit = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
//                    SettingsEdit.putBoolean(KEY_PREFS_NAME, true);
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//                    SettingsEdit.apply();
//                } else {
//                    SharedPreferences.Editor SettingsEdit = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
//                    SettingsEdit.putBoolean(KEY_PREFS_NAME, false);
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//                    SettingsEdit.apply();
//                }
            }
        });
    }
}