package com.baiyanliu.mangareader;

import lombok.RequiredArgsConstructor;

import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class CustomLogger {
    private static final String MSG_FORMAT = "%s%s- %s.";

    private final Logger logger;
    private final String commonMsg;

    public void log(Level level, String status, String params) {
        logger.log(level, String.format(MSG_FORMAT, commonMsg, params, status));
    }

    public void log(Level level, String status, String params, Throwable thrown) {
        logger.log(level, String.format(MSG_FORMAT, commonMsg, params, status), thrown);
    }
}
