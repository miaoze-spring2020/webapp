package com.me.utils;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("snsUtils")
public class SNSUtils {
    @Autowired
    @Qualifier("awsCredential")
    static AWSCredential awsCredential;

    private static String topicArn = System.getenv("TOPIC_ARN");

    private static AmazonSNS snsClient = AmazonSNSClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(awsCredential.getCredentials()))
            .withRegion(awsCredential.getRegion())
            .build();

    public void publish(String message) {
        PublishRequest publicRequest = new PublishRequest()
                .withTopicArn(topicArn)
                .withMessage(message);
        snsClient.publish(publicRequest);
    }
}
