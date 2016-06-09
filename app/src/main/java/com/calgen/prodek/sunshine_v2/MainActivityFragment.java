package com.calgen.prodek.sunshine_v2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    String[] data = {"Today-Sunny-88/63", "Tommorow - useless","Blum blum shub"};
    ArrayList<String> weekForecast = new ArrayList<>(Arrays.asList(data));

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ArrayAdapter<String> mforecastAdapter =  new ArrayAdapter(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,weekForecast);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mforecastAdapter);
        return rootView;
    }
}
