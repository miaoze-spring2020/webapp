package com.me.timer;

import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("timerSQL")
public class TimerSQL extends StopWatch{
    @Autowired
    private StatsDClient statsDClient;

    public TimerSQL() {
        super();
    }

    @Override
    public void recordTimeToStatdD(String aspect) {
        statsDClient.recordExecutionTime("sql." + aspect, elapsedTime());
    }
}
