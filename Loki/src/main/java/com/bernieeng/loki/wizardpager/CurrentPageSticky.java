package com.bernieeng.loki.wizardpager;

/**
 * Created by ebernie on 12/25/13.
 */
public class CurrentPageSticky {

    private final String pageKey;


    public CurrentPageSticky(String pageKey) {
        this.pageKey = pageKey;
    }

    public String getPageKey() {
        return pageKey;
    }
}
