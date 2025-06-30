package net.xilla.boot.storage.file.loader;

import net.xilla.boot.reflection.ObjectProcessor;
import net.xilla.boot.storage.file.FileLoader;
import net.xilla.boot.storage.file.FileSection;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class CacheLoader extends ConcurrentHashMap<String, FileSection> implements FileLoader {

    @Override
    public void put(FileSection section) {
        put(ObjectProcessor.getName(section), section);
    }

    @Override
    public void remove(String key) {
        super.remove(key);
    }

    @Override
    public void saveSections() throws FileException {

    }

    @Override
    public void readFile() throws IOException {

    }

    @Override
    public FileSection loadData(FileSection section) throws IOException {
        return null;
    }

    @Override
    public void unloadData(String section) {
        remove(section);
    }

}
