/*
 *    Copyright 2016 Gurupad Mamadapur
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.protino;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableRecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;


/**
 * Configuration activity to help user choose background color.
 */
public class SunshineWatchFaceWearableConfigActivity extends Activity {

    public static String UPPER_RECT_BG_COLOR_PREF_KEY = "upper_rect_background_color";
    private WearableRecyclerView recyclerView;
    private SharedPreferences sharedPreferences;
    private int currentCheckedPosition;
    private int[] colors;

    //Lifecycle start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sunshine_watchface);

        sharedPreferences = getDefaultSharedPreferences(getApplicationContext());
        colors = getResources().getIntArray(R.array.light_colors);
        currentCheckedPosition = getOriginalCheckedPosition();

        recyclerView = (WearableRecyclerView) findViewById(R.id.color_picker);
        recyclerView.setCenterEdgeItems(true);
        setMargins();
        recyclerView.setAdapter(new ColorListAdapter(colors, this, currentCheckedPosition));
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (recyclerView.getChildCount() > 0) {
                    recyclerView.smoothScrollToPosition(currentCheckedPosition);
                    recyclerView.setTranslationY(Utility.dpToPx(20));
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.onDestroy();
    }
//Lifecycle end

    private void setMargins() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(Utility.pxToDp(metrics.widthPixels) / 2, 0, (int) getResources().getDimension(R.dimen.config_margin), 0);
        recyclerView.setLayoutParams(params);
    }

    private int getOriginalCheckedPosition() {
        int originalColor = sharedPreferences.getInt(UPPER_RECT_BG_COLOR_PREF_KEY, -1);
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == originalColor) {
                return i;
            }
        }
        return 0;
    }

    private class ColorListAdapter extends WearableRecyclerView.Adapter<ColorListAdapter.CustomViewHolder> {
        private final int currentCheckedPosition;
        private final int[] colorList;
        private Context context;

        public ColorListAdapter(int[] colors, Context context, int currentCheckedPosition) {
            colorList = colors;
            this.context = context;
            this.currentCheckedPosition = currentCheckedPosition;
        }

        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CustomViewHolder(new CircledImageView(context));
        }

        @Override
        public void onBindViewHolder(CustomViewHolder holder, int position) {
            int color = colorList[position];
            holder.imageView.setCircleColor(color);
            holder.imageView.setImageResource(position == currentCheckedPosition
                    ? R.drawable.ic_check_white_24dp : 0);
        }

        @Override
        public int getItemCount() {
            return colorList.length;
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder {
            private CircledImageView imageView;

            public CustomViewHolder(View view) {
                super(view);
                imageView = (CircledImageView) view;
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(20, 20, 20, 20);
                imageView.setCircleRadius(30);
                imageView.setLayoutParams(layoutParams);
                imageView.setCircleBorderColor(Color.parseColor("Black"));
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //chosen color
                        int chosenColor = colorList[getAdapterPosition()];
                        imageView.setImageResource(R.drawable.ic_check_white_24dp);
                        //store the color
                        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                        sharedPreferencesEditor.putInt(UPPER_RECT_BG_COLOR_PREF_KEY, chosenColor);
                        sharedPreferencesEditor.commit(); // Deliberate commit
                        finish();
                    }
                });
            }
        }
    }
}
