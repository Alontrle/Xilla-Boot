package net.xilla.boot.storage.setting;

import net.xilla.boot.storage.file.FileLoader;
import net.xilla.boot.storage.file.loader.JsonLoader;
import net.xilla.boot.storage.manager.Manager;

public class SettingsFile extends Manager<SettingsValue> {

    public SettingsFile(String name, FileLoader loader) {
        super(name, SettingsValue.class, loader);
    }

    public SettingsFile(String name) {
        super(name, SettingsValue.class, new JsonLoader(name));
    }

    public <T> T getValue(String name) {
        SettingsValue value = get(name);
        if(value == null) return null;

        return (T)value.getValue();
    }

    public <T> void setValue(String name, T obj) {
        put(new SettingsValue<>(name, obj));
    }

    public void setDefault(SettingsValue setting) {
        if(!containsKey(setting.getName()))
            put(setting);
    }

    public <T> void setDefault(String name, T obj) {
        setDefault(new SettingsValue<>(name, obj));
    }

}
