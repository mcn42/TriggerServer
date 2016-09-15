package org.mtahq.pfc.turnstile;

import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Utils {
    private Utils() {
        super();
    }
    
    private static Logger logger = Logger.getLogger("org.mtahq.pfc.turnstile.server");
    private static ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    
    static {
        configureLog();
    }

    public static Logger getLogger() {
        return logger;
    }
    
    private static void configureLog() {
        try {
            FileHandler fh = new FileHandler("./TriggerServer_%g.log",100000,6,true);
            fh.setFormatter(new SimpleFormatter());      
            fh.setLevel(Level.ALL);
            logger.addHandler(fh);
        } catch (IOException e) {
            logger.log(Level.SEVERE,"Failed to add logging FileHandler",e);
        }
    //        try {
    //            RestLogHandler rlh = new RestLogHandler("./Announcements_%g.log");
    //
    //            rlh.setLevel(Level.INFO);
    //            logger.addHandler(rlh);
    //        } catch (Exception e) {
    //            logger.log(Level.SEVERE,"Failed to add logging RetLogHandler",e);
    //        }
    }
    
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //  no op
        }
    }


    public static ExecutorService getThreadPool() {
        return threadPool;
    }
}
