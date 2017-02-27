package io.github.protino;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Gurupad Mamadapur on 25-Feb-17.
 */

public class WeatherWatchFaceService extends CanvasWatchFaceService {

    private static final Typeface BOLD_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine implements SharedPreferences.OnSharedPreferenceChangeListener {


        private final String LOG_TAG = Engine.class.getSimpleName();
        private final String colon = " : ";
        private final Context context = WeatherWatchFaceService.this.getApplicationContext();
        private String weatherIdPrefKey;
        private String maxTempPrefKey;
        private String minTempPrefKey;
        private Bitmap weatherIconBitmap;
        private Date date;
        private java.text.DateFormat dateFormat;
        private Paint upperRectBackgroundPaint;
        private Paint lowerRectBackgroundPaint;
        private Paint timeTextPaint;
        private Calendar calendar;
        private String twoDigitFormat;
        private String highTempText;
        private String lowTempText;
        private float timeTextSize;
        private float highTempTextSize;
        private float lowTempTextSize;
        private int timeTextColor;
        private int lowTempTextColor;
        private Paint lowTempTextPaint;
        private int highTempTextColor;
        private Paint highTempTextPaint;
        private Paint weatherIconBitmapPaint;
        private SharedPreferences sharedPreferences;
        private int weatherIconId;

        //Lifecycle start
        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);


            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
            weatherIdPrefKey = getString(R.string.WEATHER_ID_PREF_KEY);
            maxTempPrefKey = getString(R.string.MAX_TEMP_PREF_KEY);
            minTempPrefKey = getString(R.string.MIN_TEMP_PREF_KEY);

            setWatchFaceStyle(new WatchFaceStyle.Builder(WeatherWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            Resources resources = context.getResources();

            twoDigitFormat = resources.getString(R.string.two_digit_format);
            timeTextSize = resources.getDimension(R.dimen.time_text_size);
            highTempTextSize = resources.getDimension(R.dimen.high_temp_text_size);
            lowTempTextSize = resources.getDimension(R.dimen.low_temp_text_size);
            timeTextColor = getColor(R.color.white);
            highTempTextColor = getColor(R.color.white);
            lowTempTextColor = getColor(R.color.light_grey);

            weatherIconBitmapPaint = new Paint();
            weatherIconBitmapPaint.setDither(true);
            weatherIconBitmapPaint.setFilterBitmap(true);
            weatherIconBitmapPaint.setAntiAlias(true);

            upperRectBackgroundPaint = new Paint();
            upperRectBackgroundPaint.setColor(Color.parseColor("#009688"));

            lowerRectBackgroundPaint = new Paint();
            lowerRectBackgroundPaint.setColor(Color.parseColor("#795548"));
            lowerRectBackgroundPaint.setStrokeWidth(2);
            lowerRectBackgroundPaint.setShadowLayer(4f, 0, 0, getColor(R.color.black));


            timeTextPaint = createTextPaint(timeTextColor, BOLD_TYPEFACE);
            timeTextPaint.setTextSize(timeTextSize);


            highTempTextPaint = createTextPaint(highTempTextColor, BOLD_TYPEFACE);
            highTempTextPaint.setTextSize(highTempTextSize);

            lowTempTextPaint = createTextPaint(lowTempTextColor, NORMAL_TYPEFACE);
            lowTempTextPaint.setTextSize(lowTempTextSize);

            weatherIconId = sharedPreferences.getInt(weatherIdPrefKey, -1);
            weatherIconBitmap = getWeatherIconBitmapFromId(resources, weatherIconId);
            highTempText = sharedPreferences.getString(maxTempPrefKey, "");
            lowTempText = sharedPreferences.getString(minTempPrefKey, "");

            date = new Date();
            calendar = Calendar.getInstance();

            initFormats();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }

        private Bitmap getWeatherIconBitmapFromId(Resources resources, int weatherIconId) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            return BitmapFactory.decodeResource(
                    resources,
                    Utility.getArtResourceForWeatherCondition(weatherIconId),
                    options);
        }

        private Paint createTextPaint(int timeColor, Typeface typeface) {
            Paint paint = new Paint();
            paint.setColor(timeColor);
            paint.setTypeface(typeface);
            paint.setAntiAlias(true);
            return paint;
        }

        private void initFormats() {
            dateFormat = DateFormat.getDateFormat(context);
            dateFormat.setCalendar(calendar);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            /* get device features (burn-in, low-bit ambient) */
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            /* the time changed */
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            /* the wearable switched between modes */
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            long now = System.currentTimeMillis();
            date.setTime(now);
            calendar.setTimeInMillis(now);

            /* Data */
            boolean is24Hour = DateFormat.is24HourFormat(context);
            String hourString;
            if (is24Hour) {
                hourString = formatTwoDigitNumber(calendar.get(Calendar.HOUR_OF_DAY));
            } else {
                int hour = calendar.get(Calendar.HOUR);
                if (hour == 0) {
                    hour = 12;
                }
                hourString = String.valueOf(hour);
            }
            String minuteString = formatTwoDigitNumber(calendar.get(Calendar.MINUTE));
            String timeText = hourString + colon + minuteString;


            /* Measurements */
            int boundsHeight = bounds.height();
            int boundsWidth = bounds.width();
            int upperRectYOffset = (int) (boundsHeight - boundsHeight * 0.4);

            Rect upperRect = new Rect();
            Rect lowerRect = new Rect();

            upperRect.set(0, 0, boundsWidth, upperRectYOffset);
            lowerRect.set(0, upperRectYOffset, boundsWidth, boundsHeight);

            // center in the time - both vertically and horizontally

            Rect rect = new Rect();
            timeTextPaint.getTextBounds(timeText, 0, timeText.length(), rect);

            int timeTextHeight = rect.height();
            int timeTextWidth = rect.width();

            float timeTextYOffset = upperRect.centerY() + timeTextHeight / 2;
            float timeTextXOffset = upperRect.centerX() - timeTextWidth / 2;

            //center highTempText and lowTempText in right and left halves of lower rect respectively.
            rect = new Rect();
            highTempTextPaint.getTextBounds(highTempText, 0, highTempText.length(), rect);
            int highTempTextHeight = rect.height();
            int highTempTextWidth = rect.width();

            rect = new Rect();
            lowTempTextPaint.getTextBounds(lowTempText, 0, lowTempText.length(), rect);
            int lowTempTextHeight = rect.height();

            int colonTextWidth = (int) highTempTextPaint.measureText(colon);

            int highTempTextYOffset = lowerRect.centerY() + highTempTextHeight / 2;
            int highTempTextXOffset = lowerRect.centerX() - (highTempTextWidth);

            int lowTempTextYOffset = lowerRect.centerY() + lowTempTextHeight / 2;
            int lowTempTextXOffset = lowerRect.centerX() + colonTextWidth / 2;


            /** place weatherIconBitmap overlapping both upperRect and lowerRect
             centered along x-axis.
             */
            rect = new Rect();
            int halfLength = (weatherIconId == -1) ? 12 : 10;
            int weatherIconBitmapOffset = boundsWidth / halfLength;
            int weatherIconBitmapLeft = lowerRect.centerX() - weatherIconBitmapOffset;
            int weatherIconBitmapTop = upperRectYOffset - weatherIconBitmapOffset;
            int weatherIconBitmapBottom = upperRectYOffset + weatherIconBitmapOffset;
            int weatherIconBitmapRight = lowerRect.centerX() + weatherIconBitmapOffset;
            rect.set(weatherIconBitmapLeft, weatherIconBitmapTop, weatherIconBitmapRight, weatherIconBitmapBottom);

            /* Draw items */

            //Draw upper rectangle
            canvas.drawRect(upperRect, upperRectBackgroundPaint);

            //draw lower rectangle
            canvas.drawRect(0, upperRectYOffset, boundsWidth, boundsHeight, lowerRectBackgroundPaint);

            //draw time
            canvas.drawText(timeText, timeTextXOffset, timeTextYOffset, timeTextPaint);

            //draw temperatures
            canvas.drawText(highTempText, highTempTextXOffset, highTempTextYOffset, highTempTextPaint);
            canvas.drawText(lowTempText, lowTempTextXOffset, lowTempTextYOffset, lowTempTextPaint);

            //draw weatherIcon
            canvas.drawBitmap(weatherIconBitmap, null, rect, weatherIconBitmapPaint);
            Log.d(LOG_TAG, "onDraw: completion time" + (now - System.currentTimeMillis()));
        }

        private String formatTwoDigitNumber(int time) {
            return String.format(twoDigitFormat, time);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            /* the watch face became visible or invisible */
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            Log.d(LOG_TAG, "onSurfaceChanged: " + width + " " + height);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(weatherIdPrefKey)) {
                weatherIconId = sharedPreferences.getInt(weatherIdPrefKey, -1);
                weatherIconBitmap = getWeatherIconBitmapFromId(getResources(), weatherIconId);
                invalidate();
            } else if (key.equals(maxTempPrefKey)) {
                highTempText = sharedPreferences.getString(maxTempPrefKey, "");
                invalidate();
            } else if (key.equals(minTempPrefKey)) {
                lowTempText = sharedPreferences.getString(minTempPrefKey, "");
                invalidate();
            }
        }
    }
}
