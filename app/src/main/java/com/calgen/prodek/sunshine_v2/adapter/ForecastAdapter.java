package com.calgen.prodek.sunshine_v2.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.calgen.prodek.sunshine_v2.ItemChoiceManager;
import com.calgen.prodek.sunshine_v2.R;
import com.calgen.prodek.sunshine_v2.Utility;
import com.calgen.prodek.sunshine_v2.data.WeatherContract;
import com.calgen.prodek.sunshine_v2.fragment.ForecastFragment;

/**
 * Created by Gurupad on 05-Aug-16.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private final ItemChoiceManager itemChoiceManager;
    private Cursor cursor;
    private Context mContext;
    private ForecastAdapterOnClickHandler clickHandler;
    private View emptyView;
    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout = true;

    public ForecastAdapter(Context context, ForecastAdapterOnClickHandler clickHandler, View emptyView, int choiceMode) {
        mContext = context;
        this.clickHandler = clickHandler;
        this.emptyView = emptyView;
        itemChoiceManager = new ItemChoiceManager(this);
        itemChoiceManager.setChoiceMode(choiceMode);
    }


    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_TODAY: {
                    layoutId = R.layout.list_item_forecast_today;
                    break;
                }
                case VIEW_TYPE_FUTURE_DAY: {
                    layoutId = R.layout.list_item_forecast;
                    break;
                }
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new ViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int defaultImage;

        boolean useLongToday;

        switch (getItemViewType(position)) {
            case VIEW_TYPE_TODAY:
                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
                useLongToday = true;
                break;
            default:
                defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
                useLongToday = false;
        }

        Glide.with(mContext)
                .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                .error(defaultImage)
                .crossFade()
                .into(holder.mIconView);

        ViewCompat.setTransitionName(holder.mIconView, "iconView" + position);
        // Read date from cursor
        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
        holder.mDateView.setText(Utility.getFriendlyDayString(mContext, dateInMillis, useLongToday));

        // Read weather forecast from cursor
        String description = Utility.getStringForWeatherCondition(mContext, weatherId);
        // Find TextView and set weather forecast on it
        holder.mDescriptionView.setText(description);
        holder.mDescriptionView.setContentDescription(mContext.getString(R.string.a11y_forecast, description));

        // For accessibility, we don't want a content description for the icon field
        // because the information is repeated in the description view and the icon
        // is not individually selectable

        // Read high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        String highString = Utility.formatTemperature(mContext, high);
        holder.mHighTempView.setText(highString);
        holder.mHighTempView.setContentDescription(mContext.getString(R.string.a11y_high_temp, highString));

        // Read low temperature from cursor
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        String lowString = Utility.formatTemperature(mContext, low);
        holder.mLowTempView.setText(lowString);
        holder.mLowTempView.setContentDescription(mContext.getString(R.string.a11y_low_temp, lowString));

        itemChoiceManager.onBindViewHolder(holder, position);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        itemChoiceManager.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        itemChoiceManager.onSaveInstanceState(outState);
    }

    public int getSelectedItemPosition() {
        return itemChoiceManager.getSelectedItemPosition();
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof ViewHolder) {
            ((ViewHolder) viewHolder).onClick(viewHolder.itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        if (cursor == null) return 0;
        return cursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        cursor = newCursor;
        notifyDataSetChanged();
        emptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return cursor;
    }

    public interface ForecastAdapterOnClickHandler {
        void onClick(Long date, ViewHolder viewHolder);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mIconView;
        public final TextView mDateView;
        public final TextView mDescriptionView;
        public final TextView mHighTempView;
        public final TextView mLowTempView;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            mIconView = (ImageView) view.findViewById(R.id.list_item_icon);
            mDateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            mDescriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            mHighTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            mLowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            cursor.moveToPosition(position);
            int dateColumnIndex = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
            clickHandler.onClick(cursor.getLong(dateColumnIndex), this);
            itemChoiceManager.onClick(this);
        }
    }
}
