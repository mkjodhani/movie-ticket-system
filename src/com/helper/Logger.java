/**
 * The program implements an application that
 * input from users and issues cheques.
 *
 * @author  Mayur Jodhani
 * @version 1.0
 * @since   2023-01-24
 */
package com.helper;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class Logger {
    public static java.util.logging.Logger getLogger(String fileName, boolean debugLogs) {
        java.util.logging.Logger LOGGER = null;
//        FileHandler fileHandler = null;
//        String logFilePath = "";
//        if (debugLogs) {
//            logFilePath = System.getProperty("user.dir") + "/src/logs/server/" + fileName + ".txt";
//        } else {
//            logFilePath = System.getProperty("user.dir") + "/src/logs/client/" + fileName + ".txt";
//        }
        LOGGER = java.util.logging.Logger.getLogger(Logger.class.getName());
//        try {
//            fileHandler = new FileHandler(logFilePath, true);
//            fileHandler.setFormatter(new SimpleFormatter());
//            LOGGER.setUseParentHandlers(debugLogs);
//            LOGGER.addHandler(fileHandler);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        return LOGGER;
    }

    public static String getFullMessage(String line1, String line2) {
        return line1 + " => " + line2;
    }
}
