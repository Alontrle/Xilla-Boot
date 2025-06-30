package net.xilla.boot.reflection.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// TODO
@Retention(RetentionPolicy.RUNTIME)
public @interface Aliased {

    String name();

}
