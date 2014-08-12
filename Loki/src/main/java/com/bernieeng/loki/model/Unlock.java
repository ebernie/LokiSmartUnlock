package com.bernieeng.loki.model;

/**
 * Created by ebernie on 12/25/13.
 */
public class Unlock {

    private final UnlockType type;
    private final String name;
    private final String key;

    public Unlock(UnlockType type, String name, String key) {
        this.type = type;
        this.name = name;
        this.key = key;
    }

    public String getKey() {
        return key;
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
                ", key='" + key + '\'' +
                '}';
    }
}
