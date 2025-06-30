package net.xilla.boot;

import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.Setter;
import net.xilla.boot.api.program.ProgramManager;
import net.xilla.boot.reflection.ClassScanner;

public class XillaApplication extends ProgramManager {

    public static void start() {
        if(getInstance() == null) {
            initialize();
        }
        XillaApplication.getInstance().startup();
    }

    public static void initialize() {
        initialize(true);
    }

    public static void initialize(boolean reflection) {
        XillaApplication xillaApplication = new XillaApplication();
        if(reflection)
            xillaApplication.loadClassScanner();
    }

    @Getter
    private static XillaApplication instance = null;

    public XillaApplication() {
        super();

        Logger.info("Initializing a Xilla Boot powered application!");
        instance = this;
    }

    public void loadClassScanner() {
        ClassScanner scanner = new ClassScanner(this);
        scanner.load();
    }

}
