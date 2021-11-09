package translate.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.MessageAttribute;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;
import com.amazonaws.services.translate.model.DescribeTextTranslationJobRequest;
import com.amazonaws.services.translate.model.DescribeTextTranslationJobResult;
import translate.helper.S3Helper;

import java.util.List;
import java.util.Map;

public class PostTranslateEventProcessor implements RequestHandler<SNSEvent, Void> {

    static String fileKeyDelimiter = "/";

    @Override
    public Void handleRequest(SNSEvent event, Context context) {
        for (SNSRecord record : event.getRecords()) {
            Map<String, MessageAttribute> messageAttributes = record.getSNS().getMessageAttributes();
            String jobId = messageAttributes.get("JobId").getValue();
            String jobStatus = messageAttributes.get("JobStatus").getValue();

            if ("COMPLETED".equals(jobStatus)) {
                String inputBucketName = System.getenv("INPUT_BUCKET_NAME");
                String outputBucketName = System.getenv("OUTPUT_BUCKET_NAME");
                String filePrefix = getFilePrefix(jobId, inputBucketName);
                List<String> fileNames = S3Helper.getFileNames(inputBucketName, filePrefix);
                S3Helper.moveFiles(fileNames, inputBucketName, filePrefix.replace(fileKeyDelimiter, ""),
                        outputBucketName, jobId);
            }
            break;
        }
        return null;
    }

    private String getFilePrefix(String jobId, String inputBucketName) {
        AmazonTranslate translate = AmazonTranslateClient.builder().build();
        DescribeTextTranslationJobRequest describeTextTranslationJobRequest =
                new DescribeTextTranslationJobRequest().withJobId(jobId);
        DescribeTextTranslationJobResult describeTextTranslationJobResult =
                translate.describeTextTranslationJob(describeTextTranslationJobRequest);
        String inputS3Uri =
                describeTextTranslationJobResult.getTextTranslationJobProperties().getInputDataConfig().getS3Uri();
        String filePrefix =
                inputS3Uri.substring(inputS3Uri.indexOf(inputBucketName)).replace(inputBucketName + fileKeyDelimiter,
                        "");
        return filePrefix;
    }
}
