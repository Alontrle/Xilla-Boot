package net.xilla.boot.storage.manager;

import lombok.Getter;
import lombok.Setter;
import net.xilla.boot.Logger;
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

    @Getter
    private final ConcurrentHashMap<String, Value> loadedObjects = new ConcurrentHashMap<>();

    @Getter
    private final ConcurrentHashMap<String, Long> lastAccessed = new ConcurrentHashMap<>();

    private final List<ManagerCache<Value>> cacheList = new Vector<>();

    // Variables

    private FileLoader storage = new CacheLoader();
    @Getter private final Class<Value> clazz;
    @Getter private String name;

    // Settings

    @Setter @Getter private boolean autoCleanup = false;
    @Setter @Getter private boolean autoSave = false;
    @Setter @Getter private int cleanupTime = 60;

    @Setter @Getter private int autoCleanupTime = 300;
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
                try {
                    storage.put(key, new FileSection(key, ObjectProcessor.toJson(obj, clazz), storage));
                } catch (ProcessorException e) {
                    Logger.error("Failed to load object " + key);
                    throw new RuntimeException(e);
                }
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    public void startWorkers() {
        if(autoSave) {
            Logger.debug("Starting auto save for " + getName());
            new Thread(() -> {
                while (autoSave) {
                    long start = System.currentTimeMillis();
                    Logger.debug("Auto saving manager " + getName());
                    save();
                    try {
                        long time = (autoSaveTime * 1000L) - (System.currentTimeMillis() - start);
                        if(time <= 0)
                            Logger.debug("Manager " + getName() + " is taking longer then the autosave period to save!");
                        else
                            Thread.sleep(time);
                    } catch (InterruptedException ignored) {
                    }
                }
            }).start();
        }
        if(autoCleanup) {
            Logger.debug("Starting auto cleanup for " + getName());
            new Thread(() -> {
                while (autoCleanup) {
                    // Saving before cleaning up
                    long start = System.currentTimeMillis();
                    Logger.debug("Auto cleaning up manager " + getName());
                    save();

                    for (String key : loadedObjects.keySet()) {
                        long time = lastAccessed.get(key);
                        if (System.currentTimeMillis() - time > cleanupTime * 1000L) {
                            unloadObject(key);
                        }
                    }
                    try {
                        long time = (autoCleanupTime * 1000L) - (System.currentTimeMillis() - start);
                        if(time <= 0)
                            Logger.debug("Manager " + getName() + " is taking longer then the cleanup period to cleanup!");
                        else
                            Thread.sleep(time);
                    } catch (InterruptedException ignored) {
                    }
                }
            }).start();
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
        if(key == null) {
            throw new RuntimeException("FAILED TO LOAD OBJECT BECAUSE KEY IS NULL");
        }
        FileSection section = storage.get(key);
        if(section == null) {
            throw new RuntimeException("FAILED TO LOAD STORAGE FOR " + key);
        }
        Value obj = loadObject(section);
        if(obj == null) {
            throw new RuntimeException("FAILED TO LOAD DATA FOR " + key);
        }
        loadedObjects.put(key, obj);
    }

    public void initialLoad() {
        if(autoCleanup) return;
        ExecutorService executor = Executors.newFixedThreadPool(loadingThreads);
        storage.forEach((key, section) ->
            executor.execute(() -> {
                    loadedObjects.put(key, loadObject(section));
                    lastAccessed.put(key, System.currentTimeMillis());
                }
            )
        );
    }

    public void loadAll() {
        ExecutorService executor = Executors.newFixedThreadPool(loadingThreads);
        storage.forEach((key, section) ->
            executor.execute(() -> {
                        loadedObjects.put(key, loadObject(section));
                        lastAccessed.put(key, System.currentTimeMillis());
                    }
            )
        );
    }

    public void reload() {
        loadStorage();
        initialLoad();
    }

    private Value loadObject(FileSection section) {
        try {
            return ObjectProcessor.toObject(section.getData(), clazz);
        } catch (Exception e) {
            Logger.error("Failed to load object " + section.getKey());
            e.printStackTrace();
            Logger.error("Reason Below");
            e.getCause().printStackTrace();
            Logger.error("JSON Below");
            Logger.error(section.getData().toString());
            return null;
        }
    }

    public void unloadObject(String key) {
        loadedObjects.remove(key);
        lastAccessed.remove(key);
        storage.unloadData(key);
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
        lastAccessed.put(key.toString(), System.currentTimeMillis());
        return loadedObjects.get(key);
    }

    @Override
    public Value put(String key, Value value) {
        lastAccessed.put(key, System.currentTimeMillis());
        return loadedObjects.put(key, value);
    }

    public Value put(Value value) {
        String key = ObjectProcessor.getName(value);
        lastAccessed.put(key, System.currentTimeMillis());
        return put(key, value);
    }

    @Override
    public Value remove(Object key) {
        storage.remove(key.toString());
        lastAccessed.remove(key);
        return loadedObjects.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Value> m) {

    }

    @Override
    public void clear() {
        loadedObjects.clear();
        lastAccessed.clear();
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

    /***
     * Do not use unless you know exactly what you are doing.
     *
     * @return FileLoader
     */
    @Deprecated
    protected FileLoader getFileLoader() {
        return storage;
    }

}
