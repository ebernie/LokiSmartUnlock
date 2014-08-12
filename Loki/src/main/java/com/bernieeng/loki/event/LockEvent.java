package com.bernieeng.loki.event;

import com.bernieeng.loki.model.UnlockType;

/**
 * Created by ebernie on 8/11/14.
 */
public class LockEvent {

    private final UnlockType type; //lock type

    public LockEvent(UnlockType type) {
        this.type = type;
    }

    public UnlockType getType() {
        return type;
    }
}
