package net.xilla.boot.storage.setting;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.xilla.boot.reflection.annotation.Ignored;

@AllArgsConstructor
public class SettingsValue<T> {

    @Getter
    @Ignored
    private transient String name;

    @Setter
    @Getter
    private T value;

    public SettingsValue() {}

    public String getAsString() {
        return value.toString();
    }

    public int getAsInteger() {
        return (int)Double.parseDouble(value.toString());
    }

    public double getAsDouble() {
        return Double.parseDouble(value.toString());
    }

    public Float getAsFloat() {
        return Float.parseFloat(value.toString());
    }

}
