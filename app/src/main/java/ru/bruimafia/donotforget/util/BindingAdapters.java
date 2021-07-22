package ru.bruimafia.donotforget.util;

import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.bruimafia.donotforget.App;
import ru.bruimafia.donotforget.BuildConfig;
import ru.bruimafia.donotforget.R;

public class BindingAdapters {

    @BindingAdapter({"app:setDate"})
    public static void setDate(TextView view, long milliseconds) {
        if (milliseconds != 0)
            view.setText(getDateOf(milliseconds));
        else
            view.setText(R.string.date);
    }

    public static String getDateOf(long milliseconds) {
        SimpleDateFormat sf = new SimpleDateFormat("E, dd MMMM", Locale.getDefault());
        return sf.format(new Date(milliseconds));
    }

    @BindingAdapter({"app:setExpandedDate"})
    public static void setExpandedDate(TextView view, long milliseconds) {
        if (milliseconds != 0)
            view.setText(getExpandedDateOf(milliseconds));
        else
            view.setText(R.string.date);
    }

    public static String getExpandedDateOf(long milliseconds) {
        SimpleDateFormat sf = new SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault());
        return sf.format(new Date(milliseconds));
    }

    @BindingAdapter({"app:setTime"})
    public static void setTime(TextView view, long milliseconds) {
        if (milliseconds != 0)
            view.setText(getTimeOf(milliseconds));
        else
            view.setText(R.string.time);
    }

    public static String getTimeOf(long milliseconds) {
        SimpleDateFormat sf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sf.format(new Date(milliseconds));
    }

    @BindingAdapter({"app:backgroundColor"})
    public static void backgroundColor(MaterialCardView view, int color) {
        if (color != 0)
            view.setCardBackgroundColor(color);
        else
            view.setCardBackgroundColor(view.getContext().getResources().getColor(R.color.white));
    }

    @BindingAdapter({"app:textColor"})
    public static void textColor(TextView view, int color) {
        if (isDarkBackground(color) && color != 0)
            view.setTextColor(view.getContext().getResources().getColor(R.color.colorLightText));
        else
            view.setTextColor(view.getContext().getResources().getColor(R.color.colorOnPrimary));
    }

    @BindingAdapter({"app:imageCalendarColor"})
    public static void imageCalendarColor(ImageView view, int color) {
        if (isDarkBackground(color) && color != 0)
            view.setImageResource(R.drawable.ic_calendar_light);
        else
            view.setImageResource(R.drawable.ic_calendar);
    }

    @BindingAdapter({"app:imageClockColor"})
    public static void imageClockColor(ImageView view, int color) {
        if (isDarkBackground(color) && color != 0)
            view.setImageResource(R.drawable.ic_clock_light);
        else
            view.setImageResource(R.drawable.ic_clock);
    }

    // определение оттенка цвета фона
    public static boolean isDarkBackground(int color) {
        // if (darkness > 0.5) -> темный цвет фона
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness > 0.5;
    }

    @BindingAdapter({"app:setVersion"})
    public static void setVersion(TextView view, boolean isFullVersion) {
        String version = isFullVersion ? "pro" : "";
        view.setText(String.format(App.getInstance().getResources().getString(R.string.app_version), BuildConfig.VERSION_NAME, version, BuildConfig.VERSION_CODE));
    }

    @BindingAdapter({"app:setLastSync"})
    public static void setLastSync(TextView view, long time) {
        view.setText(transformationTimeLastSync(time));
    }

    private static String transformationTimeLastSync(long time) {
        return time == 0 ? App.getInstance().getResources().getString(R.string.tv_no_sync) : new SimpleDateFormat("EEEE, dd MMMM y HH:mm", Locale.getDefault()).format(new Date(time));
    }

}
