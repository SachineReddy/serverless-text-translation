package translate.helper;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class S3Helper {

    private final static Logger logger = LoggerFactory.getLogger(S3Helper.class);

    public static void moveFiles(List<String> fileNames, String inputBucket, String prefix, String outputBucket, String jobId) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
        for (String fileKey : fileNames) {
            try {
                String destinationKey = fileKey.replace(prefix, jobId);
                s3Client.copyObject(inputBucket, fileKey, outputBucket, destinationKey);
            } catch (AmazonS3Exception e) {
                logger.error("Error not able to copy file : " + fileKey);
            }
        }

        for (String fileKey : fileNames) {
            try {
                s3Client.deleteObject(inputBucket, fileKey);
            } catch (AmazonS3Exception e) {
                logger.error("Error not able to delete file : " + fileKey);
            }
        }
    }

    public static List<String> getFileNames(String bucketName, String prefix) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(prefix);
        ListObjectsV2Result result;
        List<String> fileNames = new ArrayList<>();
        try {
            do {
                result = s3Client.listObjectsV2(req);
                result.getObjectSummaries().stream()
                        .filter(obj -> obj.getKey().endsWith(".txt"))
                        .forEach(obj -> fileNames.add(obj.getKey()));

                String token = result.getNextContinuationToken();
                req.setContinuationToken(token);
            } while (result.isTruncated());
        } catch (AmazonS3Exception e) {
            throw e;
        }
        return fileNames;
    }

    public static String getPresignedUrl(String bucketName, String objectKey) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .build();

        // Set the presigned URL to expire after 10 minutes.
        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 10;
        expiration.setTime(expTimeMillis);

        // Generate the presigned URL.
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, objectKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

}
