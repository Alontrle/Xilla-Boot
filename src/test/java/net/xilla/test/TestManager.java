//package net.xilla.test;
//
//import net.xilla.boot.api.program.StartupPriority;
//import net.xilla.boot.reflection.annotation.StartPriority;
//import net.xilla.boot.storage.file.loader.JsonFolderLoader;
//import net.xilla.boot.storage.manager.Manager;
//import net.xilla.test.onedeep.TestObject;
//
//import java.io.File;
//
//@StartPriority(priority = StartupPriority.CORE)
//public class TestManager extends Manager<TestObject> {
//
//    public TestManager() {
//        super(TestObject.class, new JsonFolderLoader("/testing"));
//    }
//
//}
