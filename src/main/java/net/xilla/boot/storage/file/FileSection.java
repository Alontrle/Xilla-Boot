package net.xilla.boot.storage.file;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import net.xilla.boot.storage.file.loader.JsonLoader;

import java.io.IOException;

/***
 * Stores the location and name of the section
 * for the loader to use.
 */
public class FileSection {

    @Expose
    @Getter
    private String key;

    @Setter
    private JsonObject data = null;

    @Setter
    @Getter
    private int start = -1;

    @Setter
    @Getter
    private int end = -1;

    @Setter
    private FileLoader fileLoader = null;

    public FileSection(String key, JsonObject data) {
        this.key = key;
        this.data = data;
    }

    public FileSection() {}

    public JsonObject getData() {
        if(data == null) {
            try {
                return fileLoader.loadData(this).getRawData();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        return data;
    }

    @Deprecated
    public JsonObject getRawData() {
        return data;
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("key", key);
        jsonObject.add("data", getData());
        jsonObject.addProperty("start", start);
        jsonObject.addProperty("end", end);
        return jsonObject.toString();
    }
}
