package com.me.utils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.me.timer.TimerS3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component("s3Utils")
public class S3Utils {

    @Autowired
    @Qualifier("timerS3")
    TimerS3 timerS3;

    private String accessKey = System.getenv("AWS_ACCESS_KEY");
    //    private String accessKey = "AKIAJ7VVI7PF3IZCOYDQ";
    private String secretKey = System.getenv("AWS_SECRET_KEY");
    //    private String secretKey = "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";
    private String bucketName = System.getenv("BUCKET_NAME");
    //    private String bucketName = "ass6test10000-mys3bucket-1296uof2jlx4n";
    private String region = System.getenv("AWS_REGION");
    private static final String UPLOAD_DIR = "attached_files/";


    /**
     * @return 0
     * upload success
     */
    public String uploadFile(String uniqueFileName, MultipartFile file) throws IOException {
        timerS3.start();
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .withForceGlobalBucketAccessEnabled(true)
                .build();

        //create bucket
        if (!s3client.doesBucketExist(bucketName)) {
            timerS3.recordTimeToStatdD("upload.file.fail");
            return "nobucket";
        }

        //upload file
        //validate if file exists
        if (s3client.doesObjectExist(bucketName, UPLOAD_DIR + uniqueFileName)) return "exist";

        InputStream stream = file.getInputStream();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());

        s3client.putObject(new PutObjectRequest(bucketName, UPLOAD_DIR + uniqueFileName, stream, metadata));
        if (stream != null) {
            stream.close();
        }
        timerS3.recordTimeToStatdD("upload.file.success");
        return s3client.getUrl(bucketName, uniqueFileName).toString();
    }

    public void deleteFile(String uniqueFileName) {
        timerS3.start();
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();

        s3client.deleteObject(bucketName, UPLOAD_DIR + uniqueFileName);
        timerS3.recordTimeToStatdD("delete.file.success");
    }

}
