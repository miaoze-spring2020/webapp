package com.me.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PollingTask {
    @Autowired
    @Qualifier("sqsUtils")
    SQSUtils sqsUtils;

    @Autowired
    @Qualifier("snsUtils")
    SNSUtils snsUtils;

    public void post(String message) {
        sqsUtils.sendMessage(message);
    }

    @Scheduled(fixedRate = 5000)
    public void poll(){
        List<String> messages = sqsUtils.retrieveMessage();
        for(String s : messages){
            snsUtils.publish(s);
        }
    }

}
