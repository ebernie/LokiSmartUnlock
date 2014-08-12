package com.bernieeng.loki.event;

import com.bernieeng.loki.model.UnlockType;

/**
 * Created by ebernie on 8/11/14.
 */
public class LockEvent {

    private final UnlockType type; //lock type
    private final String name;

    public LockEvent(UnlockType type, String name) {
        this.type = type; this.name = name;
    }

    public UnlockType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return
                "type=" + type +
                        ", name='" + name;
    }
}
