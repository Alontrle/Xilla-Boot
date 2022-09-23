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

    public TestObject(String id, String objectName) {
        this.id = id;
        this.objectName = objectName;
    }

    public TestObject() {}

}
