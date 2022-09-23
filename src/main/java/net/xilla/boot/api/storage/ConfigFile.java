package net.xilla.boot.api.storage;

import com.google.gson.Gson;
import net.xilla.boot.storage.config.ConfigSection;

import java.util.List;

public interface ConfigFile {

    // CONTROLS

    void save();

    void reload();

    void clear();

    // GETTERS AND SETTERS

    Object get(String key);

    void set(String key, Object object);

    void remove(String key);

    // INTERNAL CALLS

    // EXTERNAL CALLS

    boolean contains(String key);

    ConfigSection getSection(String key);

    String getExtension();

    int getSize();

    List<String> getKeys();

}
