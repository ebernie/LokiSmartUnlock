package com.bernieeng.loki.event;

import com.bernieeng.loki.model.UnlockType;

/**
 * Created by ebernie on 8/11/14.
 */
public class UnlockEvent {

    private final UnlockType type;
    private final String name;

    public UnlockEvent(UnlockType type, String name) {
        this.type = type;
        this.name = name;
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
