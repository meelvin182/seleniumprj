package ru.sokolov;

import org.slf4j.Logger;

public class LogAllExceptonsHandler implements Thread.UncaughtExceptionHandler{

    private static Logger LOGGER;

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.error("GOT EXCEPTION IN THREAD: {} {}", t.getId(), t.getName());
        LOGGER.error("Exception is : ", e);
    }
}
