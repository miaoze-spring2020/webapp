package com.me.utils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("sqsUtils")
public class SQSUtils {
    static AWSCredential awsCredential = new AWSCredential();

    private static final String queueUrl = System.getenv("QUEUE_URL");

    private static AmazonSQS sqs = AmazonSQSClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(awsCredential.getCredentials()))
            .withRegion(awsCredential.getRegion())
            .build();

    public void sendMessage(String message) {
        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(message)
                .withDelaySeconds(5);
        sqs.sendMessage(send_msg_request);
    }

    public List<String> retrieveMessage() {

        List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();

        if (messages.size() <= 0) return null;
        List<String> messagestr = new ArrayList<>();
        for(Message m : messages){
            sqs.deleteMessage(queueUrl, m.getReceiptHandle());
            messagestr.add(m.getBody());
        }

        return messagestr;
    }
}
