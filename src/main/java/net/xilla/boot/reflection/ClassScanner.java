package net.xilla.boot.reflection;

import net.xilla.boot.Logger;
import net.xilla.boot.api.program.ProgramManager;
import net.xilla.boot.api.program.StartupPriority;
import net.xilla.boot.reflection.annotation.CacheManager;
import net.xilla.boot.reflection.annotation.JsonFolderManager;
import net.xilla.boot.reflection.annotation.StartPriority;
import net.xilla.boot.reflection.annotation.JsonManager;
import net.xilla.boot.storage.file.loader.CacheLoader;
import net.xilla.boot.storage.file.loader.JsonFolderLoader;
import net.xilla.boot.storage.file.loader.JsonLoader;
import net.xilla.boot.storage.manager.Manager;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassScanner {

    private ProgramManager programManager;

    private List<ClassScan> scans = new ArrayList<>();

    public ClassScanner(ProgramManager programManager) {
        this.programManager = programManager;
        loadCheckManager();
        loadManagerCheck();
        loadJsonManagerCheck();
    }

    public void load() {

        Logger.debug("Loading classes from " + Arrays.toString(getRoot().getDefinedPackages()));

        for(var pack : getRoot().getDefinedPackages()) {
            try {
                Set<Class> classes = findAllClassesUsingReflectionsLibrary(pack.getName());
                Logger.debug("Searched package " + pack);
                Logger.debug("Found " + classes.size() + " classes");
                if (classes.size() > 0) {
                    List<Class> xillaClasses = new ArrayList<>();
                    for (var clazz : classes) {
                        scans.forEach((scan) -> {
                            if(scan.scan(clazz)) xillaClasses.add(clazz);
                        });
                    }
                    Logger.debug("------------------------------------");
                    Logger.debug("Package: " + pack.getName());
                    Logger.debug("Xilla Classes: " + xillaClasses);
                    Logger.debug("------------------------------------");
                }
            } catch (NoClassDefFoundError ex) {
                ex.printStackTrace();
            }
        }
    }
//
//    private List<Class> loadPackage(Package pack) {
//        List<Class> classes = new ArrayList<>();
//        pack.
//    }

    private void loadCheckManager() {
        scans.add(clazz -> {
            if(clazz.getSuperclass() != null && clazz.getSuperclass().equals(Manager.class)) {
                Manager manager = loadManager(clazz);
                if(manager != null) {
                    programManager.registerManager(manager, getPriority(manager.getClass()));
                    return true;
                }
            }
            return false;
        });
    }

    private void loadJsonManagerCheck() {
        scans.add(clazz -> {
            Annotation[] annotations = clazz.getAnnotationsByType(JsonManager.class);
            if(annotations.length > 0){
                System.out.println("Loading json file manager! " + clazz);
                JsonManager annotation = (JsonManager) annotations[0];
                Manager manager = new Manager(clazz, new JsonLoader(annotation.fileName()));
                programManager.registerManager(manager, getPriority(clazz));
                return true;
            }
            annotations = clazz.getAnnotationsByType(JsonFolderManager.class);
            if(annotations.length > 0) {
                System.out.println("Loading json folder manager! " + clazz);
                JsonFolderManager annotation = (JsonFolderManager) annotations[0];
                Manager manager = new Manager(clazz, new JsonFolderLoader(annotation.folderName()));
                programManager.registerManager(manager, getPriority(clazz));
                return true;
            }
            return false;
        });
    }

    private void loadManagerCheck() {
        scans.add(clazz -> {
            Annotation[] annotations = clazz.getAnnotationsByType(CacheManager.class);
            if(annotations.length > 0) {
                System.out.println("Loading cache manager! " + clazz);
                CacheManager annotation = (CacheManager) annotations[0];
                Manager manager = new Manager(clazz);
                programManager.registerManager(manager, getPriority(clazz));
                return true;
            }
            return false;
        });
    }

    private Manager loadManager(Class clazz) {
        for (var constructor : clazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) {
                try {
                    Manager manager = (Manager) constructor.newInstance();
                    return manager;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        return null;
    }

    private StartupPriority getPriority(Class clazz) {
        Annotation[] annotations = clazz.getAnnotationsByType(StartPriority.class);
        if(annotations.length == 0) return StartupPriority.MANAGER;
        return ((StartPriority) annotations[0]).priority();
    }

    public Set<Class> findAllClassesUsingReflectionsLibrary(String packageName) throws NoClassDefFoundError {
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
        return reflections.getSubTypesOf(Object.class)
                .stream()
                .collect(Collectors.toSet());
    }

    //
//    public Set<Class> findAllClassesUsingClassLoader(String packageName) {
//        InputStream stream = ClassLoader.getSystemClassLoader()
//                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
//        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
//        return reader.lines()
//                .filter(line -> line.endsWith(".class"))
//                .map(line -> getClass(line, packageName))
//                .collect(Collectors.toSet());
//    }
//
//    private Class getClass(String className, String packageName) {
//        try {
//            return Class.forName(packageName + "."
//                    + className.substring(0, className.lastIndexOf('.')));
//        } catch (ClassNotFoundException e) {
//            // handle the exception
//        }
//        return null;
//    }

    private ClassLoader getRoot() {
        return ClassLoader.getSystemClassLoader();
    }

}
