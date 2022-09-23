package net.xilla.boot.reflection.annotation;

import net.xilla.boot.storage.file.FileLoader;
import net.xilla.boot.storage.file.loader.CacheLoader;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JsonManager {

    String fileName();

}
