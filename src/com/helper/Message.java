/**
 * The program implements an application that
 * input from users and issues cheques.
 *
 * @author  Mayur Jodhani
 * @version 1.0
 * @since   2023-01-24
 */
package com.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {
    public static final String SUCCESS = "SUCCESS";
    public static final String ERROR = "ERROR";
    private static final Pattern pattern = Pattern.compile("^(\\w+)(::)(((\\w*)(\\W*))*)$");
    public static Message getSuccessMessage(String message) {
        return new Message(message, SUCCESS);
    }
    public static Message getErrorMessage(String message) {
        return new Message(message, ERROR);
    }
    private String message;
    private Message(String message, String type) {
        this.message = String.format("%s::%s", type, message);
    }
    public String getType() {
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return ERROR;
        }
    };
    public String extractMessage() {
        Matcher matcher = pattern.matcher(message);
        if (message == null) {
            return "";
        }
        if (matcher.find()) {
            return matcher.group(3);
        } else {
            return message;
        }
    };
    public String getMessage() {
        return message;
    }
    public void show() {
        if (getType().equals(SUCCESS)) {
            System.out.println(extractMessage());
        } else if (getType().equals(ERROR)) {
            System.out.println(extractMessage());
        }
    }
    public static Message generateMessageFromString(String str) {
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            String messageType = matcher.group(1);
            String messageInfo = matcher.group(3);
            return new Message(messageInfo, messageType);
        }
        return Message.getErrorMessage("");
    };
}
