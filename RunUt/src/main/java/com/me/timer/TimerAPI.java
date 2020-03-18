package com.me.timer;

import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("timerAPI")
public class TimerAPI extends StopWatch {

    @Autowired
    private StatsDClient statsDClient;

    public TimerAPI() {
        super();
    }

    @Override
    public void recordTimeToStatdD(String aspect) {
        statsDClient.recordExecutionTime("endpoint." + aspect, elapsedTime());
    }

}
