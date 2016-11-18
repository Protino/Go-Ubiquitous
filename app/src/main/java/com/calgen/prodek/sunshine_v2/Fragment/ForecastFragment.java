package com.calgen.prodek.sunshine_v2.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.calgen.prodek.sunshine_v2.R;
import com.calgen.prodek.sunshine_v2.Utility;
import com.calgen.prodek.sunshine_v2.adapter.ForecastAdapter;
import com.calgen.prodek.sunshine_v2.data.WeatherContract;
import com.calgen.prodek.sunshine_v2.sync.SunshineSyncAdapter.LocationStatus;

import static com.calgen.prodek.sunshine_v2.sync.SunshineSyncAdapter.LOCATION_STATUS_INVALID;
import static com.calgen.prodek.sunshine_v2.sync.SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN;
import static com.calgen.prodek.sunshine_v2.sync.SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener, ForecastAdapter.ForecastAdapterOnClickHandler {

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private static final int MY_LOADER_ID = 101;
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };
    private static final String SELECTED_KEY = "selected_item_position";
    private static int mPosition = RecyclerView.NO_POSITION;
    private ForecastAdapter mForecastAdapter;
    private RecyclerView recyclerView;
    private TextView mEmptyView;
    private boolean mUseTodayLayout;
    private int mChoiceMode;
    private boolean autoSelectView;
    private LinearLayout parallaxBar;
    private boolean sharedTransition;

    public ForecastFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (sharedTransition) getActivity().supportPostponeEnterTransition();
        getLoaderManager().initLoader(MY_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ForecastFragment, 0, 0);
        mChoiceMode = array.getInt(R.styleable.ForecastFragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
        autoSelectView = array.getBoolean(R.styleable.ForecastFragment_autoSelectView, false);
        sharedTransition = array.getBoolean(R.styleable.ForecastFragment_sharedElementTransitions, false);
        array.recycle();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to RecyclerView.NO_POSITION,
        // so check for that before storing.
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        mForecastAdapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        View emptyView = rootView.findViewById(R.id.recyclerView_forecast_empty);

        mForecastAdapter = new ForecastAdapter(getActivity(), this, emptyView, mChoiceMode);
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView_forecast);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mForecastAdapter);
        recyclerView.setHasFixedSize(true);

        //check if parallax bar is available
        parallaxBar = (LinearLayout) rootView.findViewById(R.id.parallax_bar);

        if (parallaxBar != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        int max = parallaxBar.getHeight();
                        float translateY = (parallaxBar.getTranslationY() - dy / 2);
                        if (dy > 0) {
                            parallaxBar.setTranslationY(Math.max(-max, translateY));
                        } else {
                            parallaxBar.setTranslationY(Math.min(0, translateY));
                        }

                    }
                });
            }
        }


        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);


        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recyclerView.clearOnScrollListeners();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            recyclerView.smoothScrollToPosition(mPosition);
        }
        updateEmptyView();
        if (data.getCount() == 0) {
            getActivity().supportStartPostponedEnterTransition();
        } else {
            recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    // Since we know we're going to get items, we keep the listener around until
                    // we see Children.
                    if (recyclerView.getChildCount() > 0) {
                        recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        int itemPosition = mForecastAdapter.getSelectedItemPosition();
                        if (RecyclerView.NO_POSITION == itemPosition) itemPosition = 0;
                        RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(itemPosition);
                        if (null != vh && autoSelectView) {
                            mForecastAdapter.selectView(vh);
                        }
                        if (sharedTransition) getActivity().supportStartPostponedEnterTransition();
                        return true;
                    }
                    return false;
                }
            });
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }


    // since we read the location when we create the loader, all we need to do is restart things
    public void onLocationChanged() {
        getLoaderManager().restartLoader(MY_LOADER_ID, null, this);
    }

    private void updateEmptyView() {
        if (mForecastAdapter.getItemCount() == 0) {
            mEmptyView = (TextView) getView().findViewById(R.id.recyclerView_forecast_empty);
            if (mEmptyView != null) {
                int message = R.string.empty_forecast_list;
                @LocationStatus int status = Utility.getLocationStatus(getContext());
                switch (status) {
                    case LOCATION_STATUS_SERVER_DOWN:
                        message = R.string.empty_forecast_list_server_down;
                        break;
                    case LOCATION_STATUS_SERVER_INVALID:
                        message = R.string.empty_forecast_list_server_error;
                        break;
                    case LOCATION_STATUS_INVALID:
                        message = R.string.empty_forecast_list_invalid_location;
                        break;
                    default:
                        if (!Utility.isNetworkAvailable(getContext())) {
                            message = R.string.empty_forecast_list_no_network;
                        }
                }
                mEmptyView.setText(message);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_location_status_key))) {
            updateEmptyView();
        }
    }

    @Override
    public void onClick(Long date, ForecastAdapter.ViewHolder viewHolder) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        ((Callback) getActivity()).onItemSelected(
                WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, date)
                , viewHolder
        );
        mPosition = viewHolder.getAdapterPosition();
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri, ForecastAdapter.ViewHolder viewHolder);
    }

}
