package com.calgen.prodek.sunshine_v2.customView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.calgen.prodek.sunshine_v2.R;
import com.calgen.prodek.sunshine_v2.activity.SettingsActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;

/**
 * Created by Gurupad Mamadapur on 11/8/2016.
 */

public class LocationEditText extends EditTextPreference {

    private final int DEFAULT_MINIMUM_LOCATION_LENGTH = 3;
    private final String TAG = EditTextPreference.class.getSimpleName();
    public int minLength;

    public LocationEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LocationEditText,
                0, 0);

        try {
            minLength = array.getInteger(R.styleable.LocationEditText_minLength, DEFAULT_MINIMUM_LOCATION_LENGTH);
        } finally {
            array.recycle();
        }

        // Check to see if Google Play services is available. The Place Picker API is available
        // through Google Play services, so if this is false, we'll just carry on as though this
        // feature does not exist. If it is true, however, we can add a widget to our preference.
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getContext());
        if (resultCode == ConnectionResult.SUCCESS) {
            // Add the get current location widget to our location preference
            setWidgetLayoutResource(R.layout.pref_current_location);
        }
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        View currentLocation = view.findViewById(R.id.current_location);
        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();

                PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();

                Activity settingsActivity = (SettingsActivity) context;
                try {
                    settingsActivity.startActivityForResult(intentBuilder.build(settingsActivity), SettingsActivity.PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesNotAvailableException
                        | GooglePlayServicesRepairableException ignore) {
                }
            }
        });

        return view;
    }


    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        EditText editText = getEditText();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Dialog dialog = getDialog();
                if (dialog instanceof AlertDialog) {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setEnabled(s.length() > minLength);
                }
            }
        });
    }
}
