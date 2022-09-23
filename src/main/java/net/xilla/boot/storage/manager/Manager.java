package net.xilla.boot.storage.manager;

import lombok.Getter;
import lombok.Setter;
import net.xilla.boot.api.storage.ConfigFile;
import net.xilla.boot.reflection.ObjectProcessor;
import net.xilla.boot.reflection.ProcessorException;
import net.xilla.boot.storage.file.FileLoader;
import net.xilla.boot.storage.file.FileSection;
import net.xilla.boot.storage.file.loader.CacheLoader;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Manager<Value> implements Map<String, Value> {

    // Storage

    private final ConcurrentHashMap<String, Value> loadedObjects = new ConcurrentHashMap<>();

    private final List<ManagerCache<Value>> cacheList = new Vector<>();

    // Variables

    private FileLoader storage = new CacheLoader();
    @Getter private final Class<Value> clazz;
    @Getter private String name;

    // Settings

    @Setter @Getter private boolean autoCleanup = true;
    @Setter @Getter private boolean autoSave = true;
    @Setter @Getter private int cleanupTime = 60;
    @Setter @Getter private int autoSaveTime = 60 * 5;
    @Setter @Getter private int loadingThreads = 1;
    @Setter @Getter private int savingThreads = 1;

    private long saving = -1;

    public Manager(Class<Value> clazz) {
        this.clazz = clazz;
        this.name = clazz.getName();
    }

    public Manager(Class<Value> clazz, FileLoader loader) {
        this(clazz);

        this.storage = loader;
    }

    public Manager(String name, Class<Value> clazz, FileLoader loader) {
        this(clazz, loader);
        this.name = name;
    }

    // Manager Functions

    public void save() {
        if(saving != -1)
            throw new RuntimeException("The manager (" + getName() + ") is already saving, this attempt will be ignored. Time spent: " + (System.currentTimeMillis() - saving) + "s.");

        saving = System.currentTimeMillis();
        loadToFileLoader();
        try {
            storage.saveSections();
        } catch (FileLoader.FileException e) {
            e.printStackTrace();
        }
        saving = -1;
    }

    private void loadToFileLoader() {
        ExecutorService executor = Executors.newFixedThreadPool(savingThreads);
        for(String key : loadedObjects.keySet()) {
            executor.execute(() -> {
                Value obj = loadedObjects.get(key);
                storage.put(key, new FileSection(key, ObjectProcessor.toJson(obj)));
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    public void loadStorage() {
        storage.clear();
        try {
            storage.readFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSingle(String key) {
        loadedObjects.put(key, loadObject(storage.get(key)));
    }

    public void loadAll() {
        ExecutorService executor = Executors.newFixedThreadPool(loadingThreads);
        storage.forEach((key, section) ->
            executor.execute(() ->
                    loadedObjects.put(key, loadObject(section))
            )
        );
    }

    private Value loadObject(FileSection section) {
        try {
            return ObjectProcessor.toObject(section.getData(), clazz);
        } catch (ProcessorException e) {
            System.out.println("Failed to load object " + section);
            e.printStackTrace();
            System.out.println("Reason Below");
            e.getCause().printStackTrace();
            return null;
        }
    }

    // Map Functions

    @Override
    public int size() {
        return keySet().size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0 && storage.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return loadedObjects.containsKey(key) || storage.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return containsKey(ObjectProcessor.getName(value));
    }

    @Override
    public Value get(Object key) {
        if(!loadedObjects.containsKey(key) && storage.containsKey(key)) {
            loadedObjects.put((String) key, loadObject(storage.get(key)));
        }
        return loadedObjects.get(key);
    }

    @Override
    public Value put(String key, Value value) {
        return loadedObjects.put(key, value);
    }

    public Value put(Value value) {
        return put(ObjectProcessor.getName(value), value);
    }

    @Override
    public Value remove(Object key) {
        storage.remove(key);
        return loadedObjects.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Value> m) {

    }

    @Override
    public void clear() {
        loadedObjects.clear();
        storage.clear();
        try {
            storage.saveSections();
        } catch (FileLoader.FileException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> keySet() {
        ArrayList<String> keys = new ArrayList<>(storage.keySet());
        keys.addAll(loadedObjects.keySet());
        return new HashSet<>(keys);
    }

    @Deprecated
    @Override
    public Collection<Value> values() {
        return loadedObjects.values();
    }

    @Deprecated
    @Override
    public Set<Entry<String, Value>> entrySet() {
        return loadedObjects.entrySet();
    }

    public String getUniqueID() {
        String id = UUID.randomUUID().toString();

        if(containsKey(id)) {
            return getUniqueID();
        }
        return id;
    }

}
