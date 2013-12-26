package com.bernieeng.loki.model;

import com.bernieeng.loki.UnlockType;

/**
 * Created by ebernie on 12/25/13.
 */
public class Unlock {

    private final UnlockType type;
    private final String name;

    public Unlock(UnlockType type, String name) {
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
        return "Unlock{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
