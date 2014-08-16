package com.bernieeng.loki.event;

import com.bernieeng.loki.model.UnlockType;

/**
 * Created by ebernie on 8/11/14.
 */
public class LockEvent {

    private final UnlockType type; //lock type
    private final String name;
    private final boolean isForceLock;

    public LockEvent(UnlockType type, String name, boolean isForceLock) {
        this.type = type;
        this.name = name;
        this.isForceLock = isForceLock;
    }

    public boolean isForceLock() {
        return isForceLock;
    }

    public UnlockType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        //MUST MATCH toString() in UnlockEvent!!!
        return
                "type=" + type +
                        ", name='" + name;
    }
}
