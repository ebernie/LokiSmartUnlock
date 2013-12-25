package com.bernieeng.loki.wizardpager.model;

import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.bernieeng.loki.wizardpager.ui.PinSetupPageFragment;

import java.util.ArrayList;

/**
 * Created by ebernie on 12/23/13.
 */
public class PinSetupPage extends Page {

    public static final String PIN_DATA_KEY = "pin";

    public PinSetupPage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return PinSetupPageFragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem("Your PIN", mData.getString(PIN_DATA_KEY), getKey(), -1));
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(PIN_DATA_KEY));
    }
}
