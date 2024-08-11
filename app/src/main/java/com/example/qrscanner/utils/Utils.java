package com.example.qrscanner.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.R;
import com.example.qrscanner.models.Assigned_to_User_Model;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Utils {


    public static byte[] imageViewToByte(Context context, ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable == null) {
            return null;
        }

        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            bitmap = getBitmapFromVectorDrawable(context, R.drawable.device_model);
        }

        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static byte[] getDefaultImageByteArray(Context context, int drawableId) {
        Bitmap bitmap = getBitmapFromDrawable(context, drawableId);
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof VectorDrawable) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }

    public static Bitmap getBitmapFromDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            return getBitmapFromVectorDrawable(context, drawableId);
        }
        return null;
    }

    public static void displayGadgetImageInt(Context context, DBHelper dbHelper, TextInputLayout textInputLayout, int gadgetId, int parentHeight) {
        byte[] imageBytes = dbHelper.getGadgetCategoryImageInt(gadgetId);
        if (imageBytes != null) {
            // ImageView To Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            textInputLayout.setStartIconDrawable(new BitmapDrawable(context.getResources(), resizeBitmap(bitmap, parentHeight)));
        } else {
            // Set a default image if no image is found
            textInputLayout.setStartIconDrawable(R.drawable.device_model);

        }
    }

    public static Bitmap resizeBitmap(Bitmap originalBitmap, int parentHeight) {
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();

        // Calculate the new height (half of the parent view's height)
        double newHeight = (double) parentHeight / 1;
        int intNewHeight = (int) newHeight;

        // Calculate the proportional width based on the original aspect ratio
        float aspectRatio = (float) originalWidth / originalHeight;
        int newWidth = (int) (newHeight * aspectRatio);

        // Resize the Bitmap
        return Bitmap.createScaledBitmap(originalBitmap, newWidth, intNewHeight, true);
    }



    public static void showDeleteAllDialog(Context context, String identifier) {

        DBHelper dbHelper = new DBHelper(context);
        ArrayList<Assigned_to_User_Model> deviceList;

        deviceList = dbHelper.fetchDevice();

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_delete_dialog, null);
        builder.setView(view);

        ((TextView) view.findViewById(R.id.messageText)).setText("Do you want to [ All " + identifier  +" ] ?" + "\n" + "\n" + "This action cannot be undone.");

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // function
        view.findViewById(R.id.actionDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.deleteAll();
                deviceList.clear();
                Toast.makeText(context, "All Data Deleted", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        });


        // For Cancel Button
        view.findViewById(R.id.actionCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
    }

    // Expiration calculator
    private static final String pattern = "MM/dd/yy";

    public static void calculateExpirationAndStatus(Date inputDate, TextView dateExpired, TextView status) {
        // Calculate expiration date
        Calendar expirationDate = Calendar.getInstance();
        expirationDate.setTime(inputDate);
        expirationDate.add(Calendar.YEAR, 5); // Add 5 years

        // Get the expiration date after adding 5 years
        Date updatedExpirationDate = expirationDate.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        String formattedExpirationDate = dateFormat.format(updatedExpirationDate);

        // Set the expiration date to dateExpired TextView
        dateExpired.setText(formattedExpirationDate);

        // Get current date
        Calendar currentDate = Calendar.getInstance();

        if (currentDate.after(expirationDate)) {
            status.setText("For Refresh");
        } else {
            status.setText("Fresh");
        }
    }

    public static boolean calculateExpiration(Date inputDate, String filterKey) {
        // Calculate expiration date
        Calendar expirationDate = Calendar.getInstance();
        expirationDate.setTime(inputDate);
        expirationDate.add(Calendar.YEAR, 5); // Add 5 years


        Calendar currentDate = Calendar.getInstance();

        boolean result = false;
        if (currentDate.after(expirationDate)) {
            result = true;
        } else {
            result = false;
        }
        
        return result;
    }

    public static class ExpirationResult {
        private String formattedExpirationDate;
        private String stringStatus;

        public ExpirationResult(String formattedExpirationDate, String stringStatus) {
            this.formattedExpirationDate = formattedExpirationDate;
            this.stringStatus = stringStatus;
        }

        public String getFormattedExpirationDate() {
            return formattedExpirationDate;
        }

        public String getStringStatus() {
            return stringStatus;
        }
    }

    public static ExpirationResult calculateExpirationString(Date inputDate, String filterKey) {
        // Calculate expiration date
        Calendar expirationDate = Calendar.getInstance();
        expirationDate.setTime(inputDate);
        expirationDate.add(Calendar.YEAR, 5); // Add 5 years

        // Get the expiration date after adding 5 years
        Date updatedExpirationDate = expirationDate.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
        String formattedExpirationDate = dateFormat.format(updatedExpirationDate);

        // Determine the status
        Calendar currentDate = Calendar.getInstance();
        String stringStatus;

            if (currentDate.after(expirationDate)) {
            stringStatus = "For Refresh";
        } else {
            stringStatus = "Fresh";
        }

        // Return both formattedExpirationDate and stringStatus
        return new ExpirationResult(formattedExpirationDate, stringStatus);
    }


    // Check Availability
    public static void updateAvailabilityStatus(EditText wantToCheck, TextView textResultHolder) {
        if (!wantToCheck.getText().toString().isEmpty()) {
            textResultHolder.setText("In Use");
        } else {
            textResultHolder.setText("In Stock");
        }
    }

    public static void rotateUp(ImageView icon) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(icon, "rotation", 0f, 180f);
        objectAnimator.setDuration(500);
        objectAnimator.start();

    }
    public static void rotateDown(ImageView icon) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(icon, "rotation", 180f, 0f);
        objectAnimator.setDuration(500);
        objectAnimator.start();

    }

    public static void smoothTransition(ViewGroup viewGroup, int animationDuration) {
        TransitionManager.beginDelayedTransition(viewGroup, new AutoTransition().setDuration(animationDuration));
    }

    public static void changeBoundsTransition(ConstraintLayout constraintHolder, int duration) {
        Transition transition = new ChangeBounds();
        transition.setInterpolator(new AccelerateDecelerateInterpolator());
        transition.setDuration(500);

        TransitionManager.beginDelayedTransition(constraintHolder, transition);
    }
    public static void scaleUpAnimator(View whatToScale, float from, float to, int duration) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(whatToScale, "scaleX", from, to);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(whatToScale, "scaleY", from, to);
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(duration);
        animatorSet.start();
    }

    public static void scaleDownAnimator(View whatToScale, float from, float to, int duration) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(whatToScale, "scaleX", from, to);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(whatToScale, "scaleY", from, to);
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(duration);
        animatorSet.start();
    }

    public static void expandCardView(final CardView cardView, int duration) {

        int newHeight = getWrapContentHeight(cardView);

        ValueAnimator animator = ValueAnimator.ofInt(0, newHeight);
        animator.addUpdateListener(animation -> {
            cardView.getLayoutParams().height = (int) animation.getAnimatedValue();
            cardView.requestLayout();
        });

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(duration);
        animator.start();
    }

    public static void collapseCardView(final CardView cardView, CardView originalHeight, int duration) {
        final int initialHeight = cardView.getMeasuredHeight();

        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, getWrapContentHeight(originalHeight));
        animator.addUpdateListener(animation -> {
            cardView.getLayoutParams().height = (int) animation.getAnimatedValue();
            cardView.requestLayout();
        });

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(duration);
        animator.start();
    }

    public static void expandCardViewItemAdapter(final CardView cardView, int duration) {

        int newHeight = (getWrapContentHeight(cardView));

        ValueAnimator animator = ValueAnimator.ofInt(cardView.getHeight(), newHeight);
        animator.addUpdateListener(animation -> {
            cardView.getLayoutParams().height = (int) animation.getAnimatedValue();
            cardView.requestLayout();
        });

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(duration);
        animator.start();
    }

    public static void collapseCardViewItemAdapter(final CardView cardView, CardView originalHeight, int duration) {
        final int initialHeight = cardView.getMeasuredHeight();

        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, getWrapContentHeight(originalHeight));
        animator.addUpdateListener(animation -> {
            cardView.getLayoutParams().height = (int) animation.getAnimatedValue();
            cardView.requestLayout();
        });

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(duration);
        animator.start();
    }

    public static int getWrapContentHeight(CardView cardView) {
        // Measure the CardView
//        cardView.measure(View.MeasureSpec.makeMeasureSpec(cardView.getWidth(), View.MeasureSpec.UNSPECIFIED),
//                View.MeasureSpec.makeMeasureSpec(cardView.getWidth(), View.MeasureSpec.UNSPECIFIED));

        int widthSpec = View.MeasureSpec.makeMeasureSpec(cardView.getWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        cardView.measure(widthSpec, heightSpec);

        // Get the measured height
        return cardView.getMeasuredHeight();
    }

    public static int dpToPxOrDirectPx(Context context, float valueInDp) {
        if (valueInDp >= 0) {
            // If the value is positive or zero, treat it as dp
            return (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    valueInDp,
                    context.getResources().getDisplayMetrics()
            );
        } else {
            // If the value is negative, treat it as a direct pixel value
            return (int) valueInDp;
        }
    }


    //    FPS Adjuster    //
    public static class CustomFpsInterpolator implements TimeInterpolator {
        private float framesPerSecond;

        public CustomFpsInterpolator(float fps) {
            this.framesPerSecond = fps;
        }

        @Override
        public float getInterpolation(float input) {
            // Simulate lower FPS by quantizing the input
            float frameInterval = 1.0f / framesPerSecond;
            return (float) (Math.floor(input / frameInterval) * frameInterval);
        }
    }


}