package net.xilla.boot;

import com.google.gson.stream.JsonReader;
import lombok.Getter;
import net.xilla.boot.api.program.ProgramManager;
import net.xilla.boot.reflection.ClassScanner;

public class XillaApplication extends ProgramManager {

    public static void start(String applicationName) {
        start(applicationName, true);
    }

    public static void start(String applicationName, boolean reflection) {
        XillaApplication xillaApplication = new XillaApplication(applicationName);
        if(reflection)
            xillaApplication.start();
    }

    @Getter
    private static XillaApplication instance;

    public XillaApplication(String name) {
        super(name);

        instance = this;
    }

    public void start() {
        ClassScanner scanner = new ClassScanner(this);
        scanner.load();
    }

}
