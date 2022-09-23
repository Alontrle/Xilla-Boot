package net.xilla.boot.storage.file.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.xilla.boot.storage.file.FileLoader;
import net.xilla.boot.storage.file.FileSection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/***
 * The FileLoader class is used to efficiently read
 * files. Using a buffered reader and only keeping a
 * cache of where objects are located.
 *
 * Best coupled with a data manager that also works as
 * a cache. As constant read/write's can degrade disk
 * health and application performance over time.
 */
public class JsonFolderLoader extends ConcurrentHashMap<String, FileSection> implements FileLoader {

    private File folder;


    public JsonFolderLoader(String filePath) {
        this.folder = new File(filePath);
    }

    public void put(FileSection object) {
        super.put(object.getKey(), object);
    }

    @Override
    public void remove(String key) {
        super.remove(key);
    }

    // Writing to the file

    public void saveSections() throws FileException {
        for(FileSection section : values()) {
            File file = new File(folder + "/" + section.getKey() + ".json");
            File tempFile = new File(folder + "/" + section.getKey() + ".json.temp");
            tempFile.delete();
            try(FileWriter fileWriter = new FileWriter(tempFile)) {
                try {
                    writeSection(fileWriter, section);
                } catch (IOException e) {
                    throw new FileException("Failed to write to the temporary file!", e);
                }
            } catch (IOException e) {
                throw new FileException("Failed to open the file writer for the temporary file!", e);
            }

            if(!file.delete() && file.exists()) {
                throw new FileException("Failed to delete the original file!");
            }
            if (!tempFile.renameTo(file))
                throw new FileException("Failed to rename temporary file!");
        }
    }

    private void writeSection(FileWriter writer, FileSection section) throws IOException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

        String json = gson.toJson(section);

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        jsonObject.add("data", section.getData());
        writer.append(gson.toJson(jsonObject));
    }

    // Reading from the file

    public void readFile() throws IOException {
        if(!folder.exists()) {
            if(!folder.mkdirs()) {
                throw new IOException("Failed to create folder " + folder);
            }
        }

        for(File file : folder.listFiles()) {
            FileInputStream stream = new FileInputStream(file);

            StringBuilder builder = new StringBuilder();
            StringBuilder tempBuffer = new StringBuilder();
            int start = 0;

            int index = 0;
            while(stream.available() > 0) {

                char character = (char)stream.read();

                builder.append(character);

                tempBuffer.append(character);
                if(tempBuffer.length() > 4) {
                    tempBuffer.deleteCharAt(0);
                }

                if(tempBuffer.toString().endsWith("\n}")) {

                    String json;

                    json = builder.substring(0, builder.length());

                    try {
                        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

                        FileSection section = gson.fromJson(json, FileSection.class);
                        section.setFileLoader(this);
                        section.setStart(start);
                        section.setEnd(index);

                        put(section);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    builder = new StringBuilder();
                    start = index;
                }
                index++;
            }
        }
    }

    public FileSection loadData(FileSection section) throws IOException {
        return readSectionWithData(new File(folder + "/" + section.getKey() + ".json"), section.getStart(), section.getEnd());
    }

    public FileSection readSectionWithData(File file, int start, int end) throws IOException {
        FileInputStream stream = new FileInputStream(file);

        StringBuilder builder = new StringBuilder();

        stream.skip(start);

        for(int i = start; i <= end; i++) {

            char character = (char)stream.read();

            builder.append(character);
        }
        String json = builder.toString();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        JsonObject jsonObject = null;
        try {
            jsonObject = new GsonBuilder().setLenient().create().fromJson(json, JsonObject.class);

            FileSection section = gson.fromJson(json, FileSection.class);
            section.setFileLoader(this);
            section.setStart(start);
            section.setEnd(end);
            section.setData(jsonObject.getAsJsonObject("data"));
//            System.out.println("Raw data " + jsonObject);
            return section;
        } catch (Exception ex) {
            System.out.println("Failed to load object with starting position " + start);
            System.out.println("Raw json string: " + json);
            ex.printStackTrace();
            return null;
        }
    }


}
