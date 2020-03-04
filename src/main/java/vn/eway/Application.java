package vn.eway;

import io.vertx.core.Launcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Application {
    private static final Logger LOGGER = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        LOGGER.warn("@Load argument: " + String.join(", ", args));
        new Launcher().dispatch(args);
    }
}