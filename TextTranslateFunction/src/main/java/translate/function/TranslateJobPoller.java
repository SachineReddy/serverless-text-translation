package translate.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;
import com.amazonaws.services.translate.model.DescribeTextTranslationJobRequest;
import com.amazonaws.services.translate.model.DescribeTextTranslationJobResult;


public class TranslateJobPoller implements RequestHandler<String, String> {

    @Override
    public String handleRequest(String jobId, Context context) {
        AmazonTranslate translate = AmazonTranslateClient.builder().build();
        DescribeTextTranslationJobRequest describeTextTranslationJobRequest =
                new DescribeTextTranslationJobRequest().withJobId(jobId);
        DescribeTextTranslationJobResult describeTextTranslationJobResult =
                translate.describeTextTranslationJob(describeTextTranslationJobRequest);
        return describeTextTranslationJobResult.getTextTranslationJobProperties().getJobStatus();
    }
}