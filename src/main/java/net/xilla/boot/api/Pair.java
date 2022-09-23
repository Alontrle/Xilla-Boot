package net.xilla.boot.api;

import lombok.Getter;
import lombok.Setter;

public class Pair<Key, Value> {

    @Setter
    @Getter
    private Key key;

    @Setter
    @Getter
    private Value value;

    public Pair(Key key, Value value) {
        this.key = key;
        this.value = value;
    }

}
