package com.calgen.prodek.sunshine_v2.customView;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

import com.calgen.prodek.sunshine_v2.R;

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
                Dialog dialog= getDialog();
                if (dialog instanceof AlertDialog) {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setEnabled(s.length() > minLength);
                }
            }
        });
    }
}
