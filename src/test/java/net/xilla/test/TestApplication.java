package net.xilla.test;

import net.xilla.boot.XillaAPI;
import net.xilla.boot.XillaApplication;
import net.xilla.boot.api.program.StartupPriority;
import net.xilla.boot.api.program.StartupProcess;
import net.xilla.boot.storage.manager.Manager;
import net.xilla.test.onedeep.TestObject;

import java.util.UUID;

public class TestApplication {

    public static void start() {

        System.out.println("Print 1");
        print();
//
//        Manager<TestObject> manager = XillaAPI.getManager(TestObject.class);
//        TestObject a = new TestObject(UUID.randomUUID().toString(), "Test");
//        manager.put(a);
//        manager.save();

        System.out.println("Print 2");
        print();
//
//        System.out.println("Print 3");
//        print();
    }

    public static void print() {
        Manager<TestObject> manager = XillaAPI.getManager(TestObject.class);
        System.out.println("Printing manager data: " + manager.keySet());
        for(String key : manager.keySet()) {
            System.out.println("Getting object with key: " + key);
            System.out.println("Object ID " + manager.get(key).getId());
            System.out.println("Object Name " + manager.get(key).getObjectName());
        }
    }

    public static void main(String[] args) {
        XillaApplication.start("Test Application");
        XillaApplication.getInstance().startup();
        start();
    }

}
