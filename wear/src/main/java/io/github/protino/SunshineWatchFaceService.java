package io.github.protino;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.SurfaceHolder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Gurupad Mamadapur on 25-Feb-17.
 */

public class SunshineWatchFaceService extends CanvasWatchFaceService {

    private static final Typeface BOLD_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine implements SharedPreferences.OnSharedPreferenceChangeListener {


        private static final int MSG_UPDATE_TIME = 0;
        private static final String colon = " : ";
        /**
         * Handler to update the time periodically in interactive mode.
         */
        private final Handler updateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        long timeMs = System.currentTimeMillis();
                        long delayMs = 500 - (timeMs % 500);
                        updateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        break;
                }
            }
        };
        private final String LOG_TAG = Engine.class.getSimpleName();
        private final Context context = SunshineWatchFaceService.this.getApplicationContext();
        private final int ambientModeBackgroundColor = Color.parseColor("Black");
        private final int white = Color.parseColor("White");
        private String weatherIdPrefKey;
        private String maxTempPrefKey;
        private String minTempPrefKey;
        private Bitmap weatherIconBitmap;
        private Date date;
        private DateFormat dateFormat;
        private Paint upperRectBackgroundPaint;
        private Paint lowerRectBackgroundPaint;
        private Paint timeTextPaint;
        private Calendar calendar;
        private final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                calendar.setTimeZone(TimeZone.getDefault());
                initFormats();
                invalidate();
            }
        };
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
        private Rect lowerRect;
        private Rect upperRect;
        private Rect bitMapRect;
        private int highTempTextYOffset;
        private int highTempTextXOffset;
        private int lowTempTextYOffset;
        private int lowTempTextXOffset;
        private int upperRectYOffset;
        private int timeDateSpace;
        private Paint dateTextPaint;
        private int dateTextColor;
        private float dateTextSize;
        private int upperRectBackgroundColor;
        private int lowerRectBackgroundColor;
        private int width;
        private int lowHighTempSpace;
        private boolean inAmbientMode;
        private boolean lowBitAmbient;
        private boolean registeredReceiver;

        //Lifecycle start
        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);


            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
            weatherIdPrefKey = getString(R.string.WEATHER_ID_PREF_KEY);
            maxTempPrefKey = getString(R.string.MAX_TEMP_PREF_KEY);
            minTempPrefKey = getString(R.string.MIN_TEMP_PREF_KEY);

            setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineWatchFaceService.this).build());

            Resources resources = context.getResources();

            twoDigitFormat = resources.getString(R.string.two_digit_format);
            timeTextSize = resources.getDimension(R.dimen.time_text_size);
            highTempTextSize = resources.getDimension(R.dimen.high_temp_text_size);
            lowTempTextSize = resources.getDimension(R.dimen.low_temp_text_size);
            dateTextSize = resources.getDimension(R.dimen.date_text_size);
            timeTextColor = white;
            dateTextColor = white;
            highTempTextColor = getColor(R.color.black);
            lowTempTextColor = getColor(R.color.dark_grey);

            upperRectBackgroundColor = sharedPreferences.getInt(
                    SunshineWatchFaceWearableConfigActivity.UPPER_RECT_BG_COLOR_PREF_KEY,
                    getColor(R.color.default_upper_rect_background_color));
            lowerRectBackgroundColor = getColor(R.color.default_lower_rect_background_color);

            weatherIconBitmapPaint = new Paint();
            weatherIconBitmapPaint.setDither(true);
            weatherIconBitmapPaint.setFilterBitmap(true);

            upperRectBackgroundPaint = new Paint();
            upperRectBackgroundPaint.setColor(upperRectBackgroundColor);

            lowerRectBackgroundPaint = new Paint();
            lowerRectBackgroundPaint.setColor(lowerRectBackgroundColor);
            //lowerRectBackgroundPaint.setStrokeWidth(2);
            lowerRectBackgroundPaint.setShadowLayer(4f, 0, 0, getColor(R.color.black));


            timeTextPaint = createTextPaint(timeTextColor, BOLD_TYPEFACE);
            timeTextPaint.setTextSize(timeTextSize);

            dateTextPaint = createTextPaint(dateTextColor, NORMAL_TYPEFACE);
            dateTextPaint.setTextSize(dateTextSize);

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
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
            updateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
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
            dateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
            dateFormat.setCalendar(calendar);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            /* get device features (burn-in, low-bit ambient) */
            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            timeTextPaint.setTypeface(burnInProtection ? NORMAL_TYPEFACE : BOLD_TYPEFACE);
            lowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
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
            this.inAmbientMode = inAmbientMode;
            switchPaintColor(upperRectBackgroundPaint, ambientModeBackgroundColor, upperRectBackgroundColor);
            switchPaintColor(lowerRectBackgroundPaint, ambientModeBackgroundColor, lowerRectBackgroundColor);
            if (inAmbientMode) {
                lowerRectBackgroundPaint.clearShadowLayer();
            } else {
                //lowerRectBackgroundPaint.setStrokeWidth(2);
                lowerRectBackgroundPaint.setShadowLayer(4f, 0, 0, getColor(R.color.black));
            }
            if (lowBitAmbient) {
                boolean antiAlias = !inAmbientMode;
                timeTextPaint.setAntiAlias(antiAlias);
                highTempTextPaint.setAntiAlias(antiAlias);
                lowTempTextPaint.setAntiAlias(antiAlias);
                upperRectBackgroundPaint.setAntiAlias(antiAlias);
                lowerRectBackgroundPaint.setAntiAlias(antiAlias);

                weatherIconBitmapPaint.setDither(false);
                weatherIconBitmapPaint.setFilterBitmap(false);
            }
            invalidate();
            updateTimer();
        }

        private void updateTimer() {
            updateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !inAmbientMode;
        }

        private void switchPaintColor(Paint paint, int ambientColor, int interactiveColor) {
            paint.setColor(inAmbientMode ? ambientColor : interactiveColor);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            long now = System.currentTimeMillis();
            date.setTime(now);
            calendar.setTimeInMillis(now);

            /* Fetch time data */
            boolean is24Hour = android.text.format.DateFormat.is24HourFormat(context);
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

            /* Measure placement of time text */
            int boundsWidth = bounds.width();
            int boundsHeight = bounds.height();
            String timeText = hourString + colon + minuteString;
            String dateText = dateFormat.format(date);
            Rect rect = new Rect();
            timeTextPaint.getTextBounds(timeText, 0, timeText.length(), rect);
            int timeTextHeight = rect.height();
            int timeTextWidth = rect.width();
            dateTextPaint.getTextBounds(dateText, 0, dateText.length(), rect);
            int dateTextWidth = rect.width();

            // x offsets
            int hourTextXOffset = upperRect.centerX() - timeTextWidth / 2;
            int colonTextXOffset = hourTextXOffset + (int) (timeTextPaint.measureText(hourString));
            int minuteTextXOffset = colonTextXOffset + (int) (timeTextPaint.measureText(colon));
            int dateTextXOffset = upperRect.centerX() - dateTextWidth / 2;

            // y offsets
            int timeTextYOffset = timeTextHeight + upperRect.centerY() - timeDateSpace;
            int dateTextYOffset = timeTextYOffset + timeDateSpace;


            /* Draw items */

            //Draw upper rectangle
            canvas.drawRect(upperRect, upperRectBackgroundPaint);

            //draw lower rectangle
            canvas.drawRect(0, upperRectYOffset, boundsWidth, boundsHeight, lowerRectBackgroundPaint);

            //draw time
            canvas.drawText(hourString, hourTextXOffset, timeTextYOffset, timeTextPaint);
            canvas.drawText(minuteString, minuteTextXOffset, timeTextYOffset, timeTextPaint);

            if (inAmbientMode || now % 1000 < 500) {
                canvas.drawText(colon, colonTextXOffset, timeTextYOffset, timeTextPaint);
            }
            //draw date
            canvas.drawText(dateText, dateTextXOffset, dateTextYOffset, dateTextPaint);

            if (!inAmbientMode) {
                //draw temperatures
                canvas.drawText(highTempText, highTempTextXOffset, highTempTextYOffset, highTempTextPaint);
                canvas.drawText(lowTempText, lowTempTextXOffset, lowTempTextYOffset, lowTempTextPaint);

                //draw weatherIcon
                canvas.drawBitmap(weatherIconBitmap, null, bitMapRect, weatherIconBitmapPaint);
            }
            //  Log.d(LOG_TAG, "onDraw: took " + (now - System.currentTimeMillis()) + " ms");
        }

        private String formatTwoDigitNumber(int time) {
            return String.format(twoDigitFormat, time);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            /* the watch face became visible or invisible */
            if (visible) {
                registerReceiver();
            } else {
                unregisterReceiver();
            }

            updateTimer();
        }

        private void unregisterReceiver() {
            if (!registeredReceiver) {
                return;
            }
            registeredReceiver = false;
            SunshineWatchFaceService.this.unregisterReceiver(receiver);
        }

        private void registerReceiver() {
            if (registeredReceiver) {
                return;
            }
            registeredReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            filter.addAction(Intent.ACTION_LOCALE_CHANGED);
            SunshineWatchFaceService.this.registerReceiver(receiver, filter);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            this.width = width;

            /* Let upperRect take 60% space */
            upperRectYOffset = (int) (height - height * 0.4);
            upperRect = new Rect();
            lowerRect = new Rect();

            upperRect.set(0, 0, width, upperRectYOffset);
            lowerRect.set(0, upperRectYOffset, width, height);

            /* Space between time and date be 20% of upperRect height
               and space between temperatures bt 10% of total width */
            timeDateSpace = (int) (upperRectYOffset * 0.2);
            lowHighTempSpace = (int) (width * 0.1);

            calculateDataSpecificMeasurements();
        }

        private void calculateDataSpecificMeasurements() {
            /* Center highTempText and lowTempText in right and left halves of lower rect respectively.*/
            Rect rect = new Rect();
            highTempTextPaint.getTextBounds(highTempText, 0, highTempText.length(), rect);
            int highTempTextHeight = rect.height();
            int highTempTextWidth = rect.width();

            lowTempTextPaint.getTextBounds(lowTempText, 0, lowTempText.length(), rect);
            int lowTempTextHeight = rect.height();

            highTempTextYOffset = lowerRect.centerY() + highTempTextHeight / 2;
            highTempTextXOffset = lowerRect.centerX() - (highTempTextWidth) - lowHighTempSpace / 2;

            lowTempTextYOffset = lowerRect.centerY() + lowTempTextHeight / 2;
            lowTempTextXOffset = lowerRect.centerX() + lowHighTempSpace / 2;

            /* Place weatherIconBitmap overlapping both upperRect and lowerRect centered along x-axis.*/
            bitMapRect = new Rect();
            int halfLength = (weatherIconId == -1) ? 12 : 9;
            int weatherIconBitmapOffset = width / halfLength;
            int weatherIconBitmapLeft = lowerRect.centerX() - weatherIconBitmapOffset;
            int weatherIconBitmapTop = upperRectYOffset - weatherIconBitmapOffset;
            int weatherIconBitmapBottom = upperRectYOffset + weatherIconBitmapOffset;
            int weatherIconBitmapRight = lowerRect.centerX() + weatherIconBitmapOffset;
            bitMapRect.set(weatherIconBitmapLeft, weatherIconBitmapTop, weatherIconBitmapRight, weatherIconBitmapBottom);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            String longKey = SunshineWatchFaceWearableConfigActivity.UPPER_RECT_BG_COLOR_PREF_KEY;
            if (key.equals(weatherIdPrefKey)) {
                weatherIconId = sharedPreferences.getInt(weatherIdPrefKey, -1);
                weatherIconBitmap = getWeatherIconBitmapFromId(getResources(), weatherIconId);
            } else if (key.equals(maxTempPrefKey)) {
                highTempText = sharedPreferences.getString(maxTempPrefKey, "");
            } else if (key.equals(minTempPrefKey)) {
                lowTempText = sharedPreferences.getString(minTempPrefKey, "");
            } else if (key.equals(longKey)) {
                upperRectBackgroundColor = sharedPreferences.getInt(longKey, upperRectBackgroundColor);
                upperRectBackgroundPaint.setColor(upperRectBackgroundColor);
            }
            //recalculateMeasurements
            calculateDataSpecificMeasurements();
            // invalidate only if not in ambient mode because only time is shown
            if (!inAmbientMode) {
                invalidate();
            }
        }
    }
}
