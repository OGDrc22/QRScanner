package com.example.qrscanner.activities;

import static android.app.PendingIntent.getActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.qrscanner.R;

public class Settings extends AppCompatActivity {

    private static final int SETTINGS_REQUEST_CODE = 147;

    private ImageView gear, backBtn, theme_indicator, currentActivity;
    private TextView titleTextView, textView;
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

        titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText("Settings");
        gear = findViewById(R.id.settingsIcon);
        gear.setVisibility(View.GONE);
        currentActivity = findViewById(R.id.currentActivity);
        currentActivity.setImageResource(R.drawable.gear_out_line);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        textView = findViewById(R.id.textIndicator);
        theme_indicator = findViewById(R.id.theme_indicator);
        setTheme_indicator();
        CardView theme = findViewById(R.id.themeCardView);
        theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioBTN();
            }
        });


    }

    private void radioBTN() {
        final Dialog dialog = new Dialog(Settings.this);
        View view = LayoutInflater.from(Settings.this).inflate(R.layout.modes_theme, null);
        dialog.setContentView(view);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        RadioGroup themeGroup = dialog.findViewById(R.id.themeGroup);
        SharedPreferences sharedPref = getSharedPreferences("ThemePref", Context.MODE_PRIVATE);
        String currentTheme = sharedPref.getString("selectedTheme", "System");

        // Set the current selection based on the stored preference
        switch (currentTheme) {
            case "Light":
                themeGroup.check(R.id.radioLight);
                break;
            case "Dark":
                themeGroup.check(R.id.radioDark);
                break;
            case "System":
            default:
                themeGroup.check(R.id.radioSystem);
                break;
        }

        themeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String selectedTheme = "System";  // Default

                if (checkedId == R.id.radioLight) {
                    selectedTheme = "Light";
                } else if (checkedId == R.id.radioDark) {
                    selectedTheme = "Dark";
                } else if (checkedId == R.id.radioSystem) {
                    selectedTheme = "System";
                }

                // Save the selected theme in SharedPreferences
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("selectedTheme", selectedTheme);
                editor.apply();

                // Apply the new theme and restart the activity to reflect changes
                AppCompatDelegate.setDefaultNightMode(
                        selectedTheme.equals("Light") ? AppCompatDelegate.MODE_NIGHT_NO :
                                selectedTheme.equals("Dark") ? AppCompatDelegate.MODE_NIGHT_YES :
                                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

                // Restart the activity to apply the new theme
                recreate();
            }
        });
        dialog.show();
    }

    private void setTheme_indicator() {
        SharedPreferences sharedPref = getSharedPreferences("ThemePref", Context.MODE_PRIVATE);
        String currentTheme = sharedPref.getString("selectedTheme", "System");

        // Set the current selection based on the stored preference
        switch (currentTheme) {
            case "Light":
                textView.setText("Light");
                theme_indicator.setImageResource(R.drawable.ic_light_mode);
                break;
            case "Dark":
                textView.setText("Dark");
                theme_indicator.setImageResource(R.drawable.ic_night_mode);
                break;
            case "System":
            default:
                textView.setText("Follow System");
                theme_indicator.setImageResource(R.drawable.qr_icon_24);
                int cl1 = ContextCompat.getColor(Settings.this, R.color.txtHeaderLight);
                int cl2 = ContextCompat.getColor(Settings.this, R.color.txtHeader);
                if ((Settings.this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                    theme_indicator.setColorFilter(cl1);
                } else {
                    theme_indicator.setColorFilter(cl2);
                }
                break;
        }
    }
}