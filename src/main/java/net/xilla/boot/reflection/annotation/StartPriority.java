package net.xilla.boot.reflection.annotation;

import net.xilla.boot.api.program.StartupPriority;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface StartPriority {

    StartupPriority priority();

}
