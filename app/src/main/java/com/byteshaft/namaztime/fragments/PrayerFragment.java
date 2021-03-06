package com.byteshaft.namaztime.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byteshaft.namaztime.R;

public class PrayerFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public PrayerFragment() {
    }

    public static PrayerFragment newInstance(int sectionNumber) {
        PrayerFragment fragment = new PrayerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.prayer_fragment, container, false);
        return rootView;
    }
}