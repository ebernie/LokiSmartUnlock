package com.bernieeng.loki.event;

import com.bernieeng.loki.model.UnlockType;

/**
 * Created by ebernie on 8/11/14.
 */
public class UnlockEvent {

    private final UnlockType type;

    public UnlockEvent(UnlockType type) {
        this.type = type;
    }

    public UnlockType getType() {
        return type;
    }
}
