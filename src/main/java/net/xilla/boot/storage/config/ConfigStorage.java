package net.xilla.boot.storage.config;

import net.xilla.boot.storage.manager.Manager;

public class ConfigStorage extends Manager<ConfigBuilder> {

    public ConfigStorage() {
        super(ConfigBuilder.class);
    }

}
