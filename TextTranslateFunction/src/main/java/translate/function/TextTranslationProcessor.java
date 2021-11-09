package translate.function;


import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;
import com.amazonaws.services.stepfunctions.model.StartExecutionRequest;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;
import com.amazonaws.services.translate.model.InputDataConfig;
import com.amazonaws.services.translate.model.OutputDataConfig;
import com.amazonaws.services.translate.model.StartTextTranslationJobRequest;
import com.amazonaws.services.translate.model.StartTextTranslationJobResult;
import com.amazonaws.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class TextTranslationProcessor implements RequestHandler<APIGatewayProxyRequestEvent,
        APIGatewayProxyResponseEvent> {

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        String targetLanguage = null;
        String userName = null;

        Map<String, String> headerParams = input.getHeaders();
        if (headerParams != null)
            userName = headerParams.get("userName");

        Map<String, String> queryParams = input.getQueryStringParameters();
        if (queryParams != null)
            targetLanguage = queryParams.get("targetLanguage");

        StartTextTranslationJobResult startTextTranslationJobResult;
        try {
            startTextTranslationJobResult = getStartTextTranslationJobResult(userName, targetLanguage);
        } catch (Exception e) {
            String errorMessage = "Batch translation process initiation failed !!";
            String errorPayload = String.format("{ \"errorMessage\": \"%s\", \"stackTrace\": \"%s\"  }", errorMessage, e.getMessage());
            return response
                    .withStatusCode(500)
                    .withBody(errorPayload);
        }

        String msgPayload = String.format("{ \"jobStatus\": \"%s\", \"jobId\": \"%s\" }",
                startTextTranslationJobResult.getJobStatus(), startTextTranslationJobResult.getJobId());

        String translateStateMachineArn = System.getenv("STATE_MACHINE_ARN");
        AWSStepFunctions client = AWSStepFunctionsClientBuilder.standard()
                .build();
        StartExecutionRequest startExecutionRequest = new StartExecutionRequest();
        startExecutionRequest.setStateMachineArn(translateStateMachineArn);
        startExecutionRequest.setInput(msgPayload);
        client.startExecution(startExecutionRequest);

        return response
                .withStatusCode(200)
                .withBody(msgPayload);
    }

    private StartTextTranslationJobResult getStartTextTranslationJobResult(String userName, String destinationLanguageCode) {
        AmazonTranslate translate = AmazonTranslateClient.builder()
                .build();

        InputDataConfig inputDataConfig = new InputDataConfig();
        inputDataConfig.setContentType("text/plain");
        String inputBucketName = System.getenv("INPUT_BUCKET_NAME");
        String folderName = getFolderName(userName);
        String inputS3Uri = String.format("s3://%s/%s/", inputBucketName, folderName);
        inputDataConfig.setS3Uri(inputS3Uri);

        OutputDataConfig outputDataConfig = new OutputDataConfig();
        String outputBucketName = System.getenv("OUTPUT_BUCKET_NAME");
        String outputS3Uri = String.format("s3://%s/output/", outputBucketName);
        outputDataConfig.setS3Uri(outputS3Uri);

        String jobName = String.format("TranslateJob-text-%s", System.currentTimeMillis());
        String sourceLanguageCode = System.getenv("SOURCE_LANG_CODE");
        if (StringUtils.isNullOrEmpty(destinationLanguageCode))
            destinationLanguageCode = System.getenv("TARGET_LANG_CODE");

        String dataAccessRoleArn = System.getenv("S3_ROLE_ARN");

        StartTextTranslationJobRequest startTextTranslationJobRequest = new StartTextTranslationJobRequest();
        startTextTranslationJobRequest.withJobName(jobName)
                .withDataAccessRoleArn(dataAccessRoleArn)
                .withInputDataConfig(inputDataConfig)
                .withOutputDataConfig(outputDataConfig)
                .withSourceLanguageCode(sourceLanguageCode)
                .withTargetLanguageCodes(destinationLanguageCode);

        return translate.startTextTranslationJob(startTextTranslationJobRequest);
    }

    private String getFolderName(String userName) {
        String folderName = "input"; //default value
        String userRegistryTable = System.getenv("TRANSLATE_USER_TABLE");
        final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();

        HashMap<String, AttributeValue> userNameKey = new HashMap<>();
        userNameKey.put("userName", new AttributeValue(userName));

        GetItemRequest request;
        request = new GetItemRequest().withKey(userNameKey).withTableName(userRegistryTable);

        Map<String, AttributeValue> userItem = ddb.getItem(request).getItem();
        if (userItem != null) {
            folderName = userItem.get("userFolder").getS();
        }
        return folderName;
    }
}