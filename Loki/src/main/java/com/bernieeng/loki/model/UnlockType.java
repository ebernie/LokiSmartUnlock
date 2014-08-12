package com.bernieeng.loki.model;

/**
 * Created by ebernie on 12/7/13.
 */
public enum UnlockType {

    BLUETOOTH("BLUETOOTH"), WIFI("WIFI"), ACTIVITY("ACTIVITY"), PREF_CHANGE("PREF_CHANGE");

    private final String value;

    UnlockType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
