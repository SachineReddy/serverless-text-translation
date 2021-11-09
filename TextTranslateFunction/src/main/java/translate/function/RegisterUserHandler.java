package translate.function;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.HashMap;
import java.util.Map;

public class RegisterUserHandler implements RequestHandler<APIGatewayProxyRequestEvent,
        APIGatewayProxyResponseEvent> {

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        String userName =  input.getPathParameters().get("userName");
        String jsonResponse;
        String userFolder;
        String userRegistryTable = System.getenv("TRANSLATE_USER_TABLE");
        final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();

        HashMap<String, AttributeValue> userNameKey = new HashMap<>();
        userNameKey.put("userName", new AttributeValue(userName));

        GetItemRequest request;
        request = new GetItemRequest().withKey(userNameKey).withTableName(userRegistryTable);

        try {
            Map<String, AttributeValue> userItem = ddb.getItem(request).getItem();
            if (userItem != null) {
                userFolder = userItem.get("userFolder").getS();
            } else {
                userFolder = userName + System.currentTimeMillis();
                HashMap<String,AttributeValue> userAttributes = new HashMap<>();
                userAttributes.put("userName", new AttributeValue(userName));
                userAttributes.put("userFolder", new AttributeValue(userFolder));
                ddb.putItem(userRegistryTable, userAttributes);
            }
            jsonResponse = String.format("{ \"userName\": \"%s\", \"userFolder\": \"%s\" }", userName, userFolder);
        } catch (Exception e) {
            return handleErrorResp(response, userName, e);
        }

        return response.withStatusCode(200).withBody(jsonResponse);
    }

    private APIGatewayProxyResponseEvent handleErrorResp(APIGatewayProxyResponseEvent response, String userName,
                                                         Exception e) {
        String errMsg = e.getMessage();
        String jsonResponse = String.format("{ \"errorMessage\": \"%s\", \"userName\": \"%s\" }", errMsg, userName);
        return response
                .withStatusCode(404)
                .withBody(jsonResponse);
    }

}
