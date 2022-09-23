package net.xilla.boot.api.program;

import lombok.Getter;
import net.xilla.boot.reflection.ObjectProcessor;
import net.xilla.boot.storage.manager.Manager;

/***
 * Allows for control over the Program Manager's data
 */
public class ProgramController {

    @Getter
    protected final ProgramManager manager;

    public ProgramController(ProgramManager manager) {
        this.manager = manager;
    }

    public <T> void putObject(String managerName, T object) {
        Manager m = getManager(managerName);
        if(m != null) {
            String id = ObjectProcessor.getName(object);
            m.put(id, object);
        }
    }


    public <T> T getObject(String managerName, String key) {
        Manager m = getManager(managerName);
        if(m == null) {
            return null;
        }
        return (T)m.get(key);
    }

    public <T> T getObject(Class clazz, String key) {
        Manager m = getManager(clazz);
        if(m == null) {
            return null;
        }
        return (T)m.get(key);
    }

    public <T extends Manager> T getManager(String name) {
        return (T)this.manager.getNameManagerMap().get(name);
    }

    public <T extends Manager> T getManager(Class clazz) {
        return (T)this.manager.getClassManagerMap().get(clazz);
    }

//    public <T extends Settings> T getSettings(String name) {
//        return (T)manager.getXillaSettings().get(name);
//    }
//
//    public <T extends Settings> T getSettings(Class clazz) {
//        return (T)manager.getXillaSettingsRefl().get(clazz);
//    }
//
//    public <T extends Worker> T getWorker(String name) {
//        return (T)manager.getXillaWorkers().get(name);
//    }
//
//    public <T extends Worker> T getWorker(Class clazz) {
//        return (T)manager.getXillaWorkersRefl().get(clazz);
//    }

}
