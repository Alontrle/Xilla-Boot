package net.xilla.boot;

import net.xilla.boot.reflection.ObjectProcessor;
import net.xilla.boot.storage.manager.Manager;
import net.xilla.boot.storage.setting.SettingsFile;

public class XillaAPI {

    public static <T extends Manager> T getManager(String name) {
        return XillaApplication.getInstance().getController().getManager(name);
    }

    public static <T extends Manager> T getManager(Class clazz) {
        return XillaApplication.getInstance().getController().getManager(clazz);
    }

    public static <T> T getObject(Class<T> clazz, String key) {
        Manager<T> manager = getManager(clazz);
        if(manager == null) {throwNullManagerError(clazz.getName()); return null;}
        return manager.get(key);
    }

    public static <T> T getObject(String managerName, String key) {
        Manager<T> manager = getManager(managerName);
        if(manager == null) {throwNullManagerError(managerName); return null;}
        return manager.get(key);
    }

    public static <T> T setObject(Class<T> clazz, T obj) {
        Manager<T> manager = getManager(clazz);
        if(manager == null) {throwNullManagerError(clazz.getName()); return null;}
        return manager.put(obj);
    }
    public static <T> T setObject(String managerName, T obj) {
        Manager<T> manager = getManager(managerName);
        if(manager == null) {throwNullManagerError(managerName); return null;}
        return manager.put(obj);
    }

    public static <T> T removeObject(Class<T> clazz, T obj) {
        Manager<T> manager = getManager(clazz);
        if(manager == null) {throwNullManagerError(clazz.getName()); return null;}

        String id = ObjectProcessor.getName(obj);

        return manager.remove(id);
    }

    public static <T> T removeObject(String managerName, T obj) {
        Manager<T> manager = getManager(managerName);
        if(manager == null) {throwNullManagerError(managerName); return null;}

        String id = ObjectProcessor.getName(obj);

        return manager.remove(id);
    }

    public static <T> T getSetting(String settingsName, String key) {
        SettingsFile settingsFile = getManager(settingsName);
        if(settingsFile == null) {throwNullManagerError(settingsName); return null;}
        return settingsFile.getValue(key);
    }

    public static <T> void setSetting(String settingsName, String key, T value) {
        SettingsFile settingsFile = getManager(settingsName);
        if(settingsFile == null) {throwNullManagerError(settingsName); return;}
        settingsFile.setValue(key, value);
    }

    public static <T> void setSettingDefault(String settingsName, String key, T value) {
        SettingsFile settingsFile = getManager(settingsName);
        if(settingsFile == null) {throwNullManagerError(settingsName); return;}
        settingsFile.setDefault(key, value);
    }

    public static SettingsFile getSettingsFile(String settingsName) {
        SettingsFile settingsFile = getManager(settingsName);
        if(settingsFile == null) {throwNullManagerError(settingsName); return null;}
        return settingsFile;
    }

    public static void generateSettings(String fileName) {
        SettingsFile settingsFile = new SettingsFile(fileName);
        XillaApplication.getInstance().registerManager(settingsFile);
    }

    private static void throwNullManagerError(String name) {
        throw new RuntimeException("Manager " + name + " does not exist or is not registered!");
    }

}
