package net.xilla.boot.api.program;

import lombok.Getter;

public abstract class StartupProcess implements Runnable {

    @Getter
    private String name;

    @Getter
    private StartupPriority priority;

    public StartupProcess(String name, StartupPriority priority) {
        this.name = name;
        this.priority = priority;
    }

}
