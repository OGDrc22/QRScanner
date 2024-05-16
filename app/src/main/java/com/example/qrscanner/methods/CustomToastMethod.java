package com.example.qrscanner.methods;


import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import android.os.Handler;

import androidx.cardview.widget.CardView;

import com.example.qrscanner.R;


public class CustomToastMethod {

    private Activity activity;
    private ViewGroup rootView;
    private View layoutNotification;

    private CardView cardView;
    private TextView textViewMessage, textViewDesc;
    private ImageView imageView;

    public CustomToastMethod(Activity activity) {
        this.activity = activity;
        this.rootView = activity.findViewById(R.id.main);
    }

    public void notify(int notificationLayout, int imgResID, String message,String description, Class<?> toActivity, Integer default_duration_3s_3000) {
        LayoutInflater inflater = activity.getLayoutInflater();
        layoutNotification = inflater.inflate(notificationLayout, rootView, false);

        Animation slideIn = AnimationUtils.loadAnimation(activity, R.anim.slide_in);
        Animation slideOut = AnimationUtils.loadAnimation(activity, R.anim.slide_out);

        cardView = layoutNotification.findViewById(R.id.cardViewNotif);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, toActivity);
                activity.startActivity(intent);
            }
        });

        imageView = layoutNotification.findViewById(R.id.imageViewToast);
        imageView.setImageResource(imgResID);

        textViewMessage = layoutNotification.findViewById(R.id.textViewMessage);
        textViewMessage.setText(message);

        textViewDesc = layoutNotification.findViewById(R.id.textViewDesc);
        textViewDesc.setText(description);
        if (description != null) {
            textViewDesc.setVisibility(View.VISIBLE);
        } else {
            textViewDesc.setVisibility(View.GONE);
        }

        layoutNotification.setAnimation(slideIn);
        rootView.addView(layoutNotification);

        int delay = (default_duration_3s_3000 != null) ? default_duration_3s_3000 : 3000;
        new Handler().postDelayed(() -> {
            layoutNotification.startAnimation(slideOut);
            new Handler().postDelayed(() -> rootView.removeView(layoutNotification), slideOut.getDuration());
        }, delay - slideOut.getDuration());
    }
}
