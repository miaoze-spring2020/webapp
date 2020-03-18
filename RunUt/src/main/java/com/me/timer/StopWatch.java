package com.me.timer;

import com.timgroup.statsd.StatsDClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class StopWatch {

    private long start;

    Logger log = LogManager.getLogger(StopWatch.class);

    public StopWatch() {
        start = 0;
    }

    public void start() {
        start = System.currentTimeMillis();
    }

    public long elapsedTime() {
        if (start == 0) {
            log.warn("This stop watch has not started");
            return 0;
        }
        long elapsed = System.currentTimeMillis() - start;
        start = 0;
        return elapsed;
    }

    abstract public void recordTimeToStatdD(String aspect);
}
