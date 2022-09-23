package net.xilla.boot.storage.config.type;

import net.xilla.boot.api.storage.ConfigFile;
import net.xilla.boot.storage.config.ConfigSection;
import net.xilla.boot.storage.file.FileLoader;
import net.xilla.boot.storage.file.loader.JsonLoader;

import java.io.IOException;
import java.util.List;

public class JsonFile implements ConfigFile {

    private JsonLoader fileLoader;

    public JsonFile(String file) {
        this.fileLoader = new JsonLoader(file);
    }

    @Override
    public void save() {
        try {
            fileLoader.saveSections();
        } catch (FileLoader.FileException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reload() {
        try {
            fileLoader.readFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear() {

    }

    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public void set(String key, Object object) {

    }

    @Override
    public void remove(String key) {

    }

    @Override
    public boolean contains(String key) {
        return false;
    }

    @Override
    public ConfigSection getSection(String key) {
        return null;
    }

    @Override
    public String getExtension() {
        return null;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public List<String> getKeys() {
        return null;
    }

}
