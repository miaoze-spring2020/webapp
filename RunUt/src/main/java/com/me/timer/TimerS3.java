package com.me.timer;

import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("timerS3")
public class TimerS3 extends StopWatch {
    @Autowired
    private StatsDClient statsDClient;

    public TimerS3() {
        super();
    }

    @Override
    public void recordTimeToStatdD(String aspect) {
        statsDClient.time("s3." + aspect, elapsedTime());
    }
}
