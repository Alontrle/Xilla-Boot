package net.xilla.boot.reflection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.xilla.boot.XillaAPI;
import net.xilla.boot.XillaApplication;
import net.xilla.boot.reflection.annotation.Ignored;
import net.xilla.boot.storage.manager.Manager;

import java.lang.reflect.*;
import java.util.List;

/**
 * The object processor contains all the serialization methods needed for objects
 * Allows you to serialize an object, and initialize a new object from that data.
 * This can be useful for data storage.
 */
public class ObjectProcessor {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     *
     * @param object
     * @return
     */
    public static <T> JsonObject toJson(T object, Class<T> clazz) throws ProcessorException {
        try {
            JsonObject json = new JsonObject();
            pullFields(json, clazz, object);

            return json;
        } catch (Exception exception) {
            throw new ProcessorException("(" + clazz.getName() + ") Failed to create json for OBJ (" + getName(object) + ")! Check cause.", exception);
        }

//        // TODO: Customize it to ignore ignored variables
//        // TODO: then add support to rename stuff to make cleaner files
//        return gson.toJsonTree(object).getAsJsonObject();
    }

    /**
     * Converts JSON data into an object that has an empty constructor available
     *
     * @param json JSON Data
     * @param clazz Class to build from
     * @param <T> Final object type, keeps things cast-safe
     * @return Final object
     * @throws ProcessorException Encapsulated error if something happens
     */
    public static <T> T toObject(JsonObject json, Class<T> clazz) throws ProcessorException {
        try {
            T obj = initializeObject(clazz);
            fillFields(json, clazz, obj);
            return obj;
        } catch (Exception exception) {
            throw new ProcessorException("(" + clazz.getName() + ") Failed to create object for JSON (" + json + ")! Check cause.", exception);
        }
    }

    /**
     * Creates an empty base instance of the class using an empty constructor.
     *
     * @param clazz Class to build from
     * @param <T> Final object type, keeps things cast-safe
     * @return Initialized Object
     * @throws Exception Any errors during object creation
     */
    private static <T> T initializeObject(Class<T> clazz) throws Exception {
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> c : constructors) {
            if (c.getParameterTypes().length == 0) {
                return (T) c.newInstance();
            }
        }
        throw new Exception("No matching constructors!");
    }
    /**
     * Fills the variables of an object using reflection. Pulls data from JSON.
     *
     * @param json
     * @param clazz
     * @param obj
     * @param <T>
     * @throws IllegalAccessException
     */
    private static <T> void fillFields(JsonObject json, Class<T> clazz, T obj) throws IllegalAccessException {
        for (Field field : clazz.getDeclaredFields()) {
            if (!isIgnored(field)) {
                field.setAccessible(true);
                if (loadFromManager(field)) {
                    Manager<?> manager = XillaAPI.getManager(field.getType());
                    if (manager.containsKey(json.get(getStorageName(field)).getAsString())) {
                        setField(field, obj, XillaAPI.getObject(field.getType(), json.get(getStorageName(field)).getAsString()));
                    } else {
                        setField(field, obj, null);
                    }
                } else if (List.class.isAssignableFrom(field.getType())) {
                    // Handle lists explicitly
                    ParameterizedType listType = (ParameterizedType) field.getGenericType();
                    Class<?> listGenericType = (Class<?>) listType.getActualTypeArguments()[0];
                    List<?> list = gson.fromJson(json.get(getStorageName(field)), TypeToken.getParameterized(List.class, listGenericType).getType());
                    setField(field, obj, list);
                } else {
                    setField(field, obj, gson.fromJson(json.get(getStorageName(field)), field.getType()));
                }
            }
        }
    }

    /**
     * Fills the json map using reflection. Pulls data from the POJO.
     *
     * @param json
     * @param clazz
     * @param obj
     * @param <T>
     * @throws IllegalAccessException
     */
    private static <T> void pullFields(JsonObject json, Class<T> clazz, T obj) {
        for (Field field : clazz.getDeclaredFields()) {
            try {
                if (!isIgnored(field)) {
                    field.setAccessible(true);
                    if (loadFromManager(field)) {
                        String data = getName(obj);
                        json.addProperty(getStorageName(field), data);
                    } else {
                        Object fieldValue = pullField(field, obj);
                        if (fieldValue instanceof List<?>) {
                            // Handle lists explicitly
                            ParameterizedType listType = (ParameterizedType) field.getGenericType();
                            Class<?> listGenericType = (Class<?>) listType.getActualTypeArguments()[0];
                            JsonElement data = gson.toJsonTree(fieldValue, TypeToken.getParameterized(List.class, listGenericType).getType());
                            json.add(getStorageName(field), data);
                        } else {
                            JsonElement data = gson.toJsonTree(fieldValue);
                            json.add(getStorageName(field), data);
                        }
                    }
                }
            } catch (Exception exception) {
                System.out.println("Failed to load FIELD " + field + " for OBJ " + obj);
                exception.printStackTrace();
            }
        }
    }

    /**
     *
     * @param field
     * @return
     */
    private static String getStorageName(Field field) {
        // add annotation check for customization
        return field.getName();
    }

    /**
     *
     * @param field
     * @param obj
     * @param value
     * @throws IllegalAccessException
     */
    private static void setField(Field field, Object obj, Object value) throws IllegalAccessException {
        boolean accessible = true;
        if(!field.isAccessible()) {
            accessible = false;
            field.setAccessible(true);
        }
        field.set(obj, value);
        if(!accessible) {
            field.setAccessible(false);
        }
    }


    /**
     *
     * @param field
     * @param obj
     * @throws IllegalAccessException
     */
    private static Object pullField(Field field, Object obj) throws IllegalAccessException {
        boolean accessible = true;
        if(!field.isAccessible()) {
            accessible = false;
            field.setAccessible(true);
        }
        Object object = field.get(obj);
        if(!accessible) {
            field.setAccessible(false);
        }
        return object;
    }

    private static boolean loadFromManager(Field field) {
        return XillaApplication.getInstance().getClassManagerMap().containsKey(field.getType());
//        return field.isAnnotationPresent(LoadFromManager.class);
    }

    /**
     *
     * @param field
     * @return
     */
    private static boolean isIgnored(Field field) {
        return field.isAnnotationPresent(Ignored.class);
    }

    /**
     *
     * @param object
     * @return
     */
    public static String toJsonString(Object object) {
        return gson.toJson(object);
    }

    /**
     *
     * @param object
     * @return
     */
    public static String getName(Object object) {
        try {
            Method method;
            String response;

            method = getMethod(object, "getKey");
            if(method != null) {
                response = method.invoke(object).toString();
                if(response != null) return response;
            }

            method = getMethod(object, "getID");
            if(method != null) {
                response = method.invoke(object).toString();
                if(response != null) return response;
            }

            method = getMethod(object, "getId");
            if(method != null) {
                response = method.invoke(object).toString();
                if(response != null) return response;
            }

            method = getMethod(object, "getName");
            if(method != null) {
                response = method.invoke(object).toString();
                if(response != null) return response;
            }

            return object.toString();
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return object.toString();
        }
    }

    /***
     *
     * @param method
     * @param object
     * @return
     */
    private static String tryMethod(Method method, Object object) {
        try {
            return method.invoke(object).toString();
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    /***
     *
     * @param object
     * @param method
     * @return
     */
    private static Method getMethod(Object object, String method) {
        Class clazz = object.getClass();
        try {
            return clazz.getMethod(method);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

}
