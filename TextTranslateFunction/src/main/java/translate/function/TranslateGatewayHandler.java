package translate.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;
import com.amazonaws.services.translate.model.DescribeTextTranslationJobRequest;
import com.amazonaws.services.translate.model.DescribeTextTranslationJobResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import translate.helper.S3Helper;
import translate.mapper.JobInfoMapper;
import translate.model.FileInfo;
import translate.model.JobInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TranslateGatewayHandler implements RequestHandler<APIGatewayProxyRequestEvent,
        APIGatewayProxyResponseEvent> {

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        String jobId = input.getPathParameters().get("jobId");
        String fileId = input.getPathParameters().get("fileId");
        String jsonResponse;
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        try {
            JobInfo jobInfo = getJobDetails(jobId);

            if (input.getResource().contains("/file/{fileId}")) {
                FileInfo fileInfo = getFileInfo(fileId, jobInfo);
                jobInfo.setFileInfo(fileInfo);
            } else {
                setProcessedFiles(jobInfo);
            }

            jsonResponse = ow.writeValueAsString(jobInfo);
        } catch (Exception e) {
            return handleErrorResp(response, jobId, e);
        }

        return response.withStatusCode(200).withBody(jsonResponse);
    }
    private FileInfo getFileInfo(String fileId, JobInfo jobInfo) {
        String outputBucketName = System.getenv("OUTPUT_BUCKET_NAME");
        String s3Uri = jobInfo.getOutputS3Uri();
        String prefix = s3Uri.substring(s3Uri.indexOf("output/"));
        String fileKey = prefix + fileId;
        String presignedUrl = S3Helper.getPresignedUrl(outputBucketName, fileKey);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(fileId);
        fileInfo.setPresignedUrl(presignedUrl);
        return fileInfo;
    }

    private APIGatewayProxyResponseEvent handleErrorResp(APIGatewayProxyResponseEvent response, String jobId,
                                                         Exception e) {
        String errMsg = e.getMessage();
        String jsonResponse = String.format("{ \"errorMessage\": \"%s\", \"jobId\": \"%s\" }", errMsg, jobId);
        return response
                .withStatusCode(404)
                .withBody(jsonResponse);
    }

    private void setProcessedFiles(JobInfo jobInfo) {
        String outputBucketName = System.getenv("OUTPUT_BUCKET_NAME");
        String s3Uri = jobInfo.getOutputS3Uri();
        String prefix = s3Uri.substring(s3Uri.indexOf("output/"));
        List<String> processedFiles = S3Helper.getFileNames(outputBucketName, prefix);
        List<String> filterFilenames = processedFiles.stream()
                .map(file -> file.replace(prefix, ""))
                .collect(Collectors.toList());
        jobInfo.setProcessedFile(filterFilenames);
    }

    private JobInfo getJobDetails(String jobId) {
        AmazonTranslate translate = AmazonTranslateClient.builder().build();
        DescribeTextTranslationJobRequest describeTextTranslationJobRequest =
                new DescribeTextTranslationJobRequest().withJobId(jobId);
        DescribeTextTranslationJobResult describeTextTranslationJobResult =
                translate.describeTextTranslationJob(describeTextTranslationJobRequest);
        JobInfoMapper jobInfoMapper = new JobInfoMapper();
        return jobInfoMapper.mapTranslateResultToJobInfo(describeTextTranslationJobResult);
    }
}
