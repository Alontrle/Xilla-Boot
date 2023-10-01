package net.xilla.boot.api.program;

import lombok.Getter;
import net.xilla.boot.Logger;
import net.xilla.boot.XillaApplication;
import net.xilla.boot.reflection.ObjectProcessor;
import net.xilla.boot.storage.file.FileLoader;
import net.xilla.boot.storage.manager.Manager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/***
 * The Program Manager is used to store important program
 * data. It also contains the startup sequence for proper
 * startup.
 *
 * A Program Controller is used to make external access easier
 * and safe, however it is not necessarily required.
 */
public class ProgramManager {

    // Startup Sequence

    @Getter
    private final List<StartupProcess> startupSequence = new Vector<>();

    @Getter
    private final ProgramController controller;

    @Getter
    private Map<Class, Manager> classManagerMap = new ConcurrentHashMap<>();

    @Getter
    private Map<String, Manager> nameManagerMap = new ConcurrentHashMap<>();

    // Constructor, duh

    public ProgramManager() {
        this.controller = new ProgramController(this);
    };

    public List<Manager> getRegisteredManagers() {
        return new ArrayList<>(classManagerMap.values());
    }

    public void registerManager(Manager manager) {
        registerManager(manager, StartupPriority.MANAGER);
    }

    public void registerManager(Manager manager, StartupPriority priority) {
        startupSequence.add(new StartupProcess(manager.getName(), priority) {
            @Override
            public void run() {
                manager.loadStorage();
                manager.initialLoad();
                manager.startWorkers();
            }
        });

        classManagerMap.put(manager.getClazz(), manager);
        nameManagerMap.put(manager.getName(), manager);
    }

    public <T> Manager createManager(Class<T> object, FileLoader loader) {
        return createManager(object, loader, StartupPriority.MANAGER);
    }

    public <T> Manager createManager(Class<T> object, FileLoader loader, StartupPriority priority) {
        Manager manager = new Manager(object, loader);
        XillaApplication.getInstance().registerManager(manager, priority);
        return manager;
    }

    public void registerStartupProcess(StartupProcess process) {
        startupSequence.add(process);
    }

    // Actually starting the dang thing

    public void startup() {
        List<StartupProcess> queue = new ArrayList<>(startupSequence);

        queue.sort(Comparator.comparingInt(o -> o.getPriority().ordinal()));

        List<String> items = new ArrayList<>();
        queue.forEach((q) -> {
            items.add(ObjectProcessor.getName(q) + "(" + q.getPriority() + ")");
        });

//        Logger.getGlobal().info("Starting the startup items: " + String.join(", ", items));
        Logger.debug("Starting the startup items: " + String.join(", ", items));

        queue.forEach((q) -> {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> {
//                Logger.getGlobal().info("Running startup item " + ObjectProcessor.getName(q) + " with priority " + q.getPriority());
                Logger.debug("Running startup item " + ObjectProcessor.getName(q) + " with priority " + q.getPriority());
//                Logger.log(LogLevel.DEBUG, "Running startup item " + q.getKey() + " with priority " + q.getPriority(), getClass());
                q.run();
            });
            executorService.shutdown();
            try {
                if(!executorService.awaitTermination(15, TimeUnit.SECONDS)) {
                    Logger.error("Startup item " + q + " froze and took longer than 15s to start! The process will continue, however the next startup step will attempt to run.");
                }
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        });
    }

}
