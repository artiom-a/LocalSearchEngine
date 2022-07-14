package club.dagomys.siteparcer.src.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainLog4jLogger {
    private static Logger MAIN_LOGGER;

    private MainLog4jLogger(){}

    public static Logger getInstance(
    ){
        if (MAIN_LOGGER == null){
            synchronized (MainLog4jLogger.class) {
                MAIN_LOGGER = LogManager.getLogger("mainLogger");
            }
        }
        return MAIN_LOGGER;
    }
}
