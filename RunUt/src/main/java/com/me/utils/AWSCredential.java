package com.me.utils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import org.springframework.stereotype.Component;

@Component("awsCredential")
public class AWSCredential {
    private static String accessKey = System.getenv("AWS_ACCESS_KEY");
    private static String secretKey = System.getenv("AWS_SECRET_KEY");
    private static String region = System.getenv("AWS_REGION");

    private static AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

    public AWSCredentials getCredentials() {
        return credentials;
    }

    public String getRegion(){
        return region;
    }
}
