package net.xilla.test.onedeep;

import lombok.Getter;
import net.xilla.boot.reflection.annotation.JsonFolderManager;
import net.xilla.boot.reflection.annotation.JsonManager;

@JsonFolderManager(folderName = "data/test/")
public class TestObject {

    @Getter
    private String id;

    @Getter
    private String objectName;

    private transient Object notStored = System.currentTimeMillis(); // Marked as transient, won't be stored

    public TestObject(String id, String objectName) {
        this.id = id;
        this.objectName = objectName;
    }

    public TestObject() {}

}
