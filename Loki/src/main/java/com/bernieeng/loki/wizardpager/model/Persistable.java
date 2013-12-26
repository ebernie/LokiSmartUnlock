package com.bernieeng.loki.wizardpager.model;

import android.content.SharedPreferences;

/**
 * Created by ebernie on 12/26/13.
 */
public interface Persistable {

    void persistInPref(SharedPreferences.Editor editor);
}
