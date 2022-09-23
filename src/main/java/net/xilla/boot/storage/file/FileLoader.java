package net.xilla.boot.storage.file;

import java.io.IOException;
import java.util.Map;

public interface FileLoader extends Map<String, FileSection> {

    void put(FileSection section);

    void remove(String key);

    void saveSections() throws FileException;

    void readFile() throws IOException;

    FileSection loadData(FileSection section) throws IOException;

    class FileException extends Exception {

        private Exception exception;

        public FileException(String error) {
            super(error);
        }

        public FileException(String error, Exception exception) {
            super(error + " : " + exception.getMessage());
        }

    }

}
