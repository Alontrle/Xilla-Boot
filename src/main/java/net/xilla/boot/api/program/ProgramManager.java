package net.xilla.boot.api.program;

import lombok.Getter;
import net.xilla.boot.reflection.ObjectProcessor;
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

    public ProgramManager(String name) {
        this.controller = new ProgramController(this);

    };

    public void registerManager(Manager manager) {
        registerManager(manager, StartupPriority.MANAGER);
    }

    public void registerManager(Manager manager, StartupPriority priority) {
        System.out.println("Adding manager " + ObjectProcessor.getName(manager));
        startupSequence.add(new StartupProcess(manager.getName(), priority) {
            @Override
            public void run() {
                manager.loadStorage();
                manager.loadAll();
            }
        });

        classManagerMap.put(manager.getClazz(), manager);
        nameManagerMap.put(manager.getName(), manager);
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

//        Logger.log(LogLevel.DEBUG, "Starting the startup items: " + String.join(", ", items), getClass());
        System.out.println("Starting the startup items: " + String.join(", ", items));

        queue.forEach((q) -> {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> {
                System.out.println("Running startup item " + ObjectProcessor.getName(q) + " with priority " + q.getPriority());
//                Logger.log(LogLevel.DEBUG, "Running startup item " + q.getKey() + " with priority " + q.getPriority(), getClass());
                q.run();
            });
            executorService.shutdown();
            try {
                if(!executorService.awaitTermination(15, TimeUnit.SECONDS)) {
//                    Logger.log(LogLevel.ERROR, "Startup item " + q + " froze and took longer than 15s to start! The process will continue, however the next startup step will attempt to run.", getClass());
                }
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        });
    }

}
