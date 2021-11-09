# serverless-text-translation
 
The application is used to convert multiple text (.txt) files in english to a different language specified by the user.  

Project 
[serverless-text-translation] : 
	Translating text documents placed in S3 to specified language using Amazon Translate Batch API.
	The solution overview and the details on how to access the application can be found in the Solution Overview section.
	
Project goal : 
	The goal was to use most of the serverless resources such as Lambdas, DynamoDB, Step-functions.
Also, to explore some of the AWS custom services, in the project, I used AWS translate services.
Lastly, use some framework such as SAM/Amplify for building serverless applications.


	A small note, you may not be able to receive email notifications as your email is not subscribed to the SNS service.

---------------------------------------------------------------------------------------------------------------
Project :  Translating text documents to specified language using Amazon Translate Batch API

Project name : serverless-text-translation
SAM project which creates Lambdas, API gateway, step function based state machine, S3, DynamoDB, SNS using template.yaml

---------------------------------------------------------------------------------------------------------------
Solution overview :
The workflow contains the following steps:

1. Users upload one or more text documents to Amazon S3. [The source language for the documents should be english]
2. The API gateway specifies the target language and triggers a Lambda function (TextTranslateFunction).
3. The function invokes Amazon Translate in batch mode to translate the text documents into the target language and
stores the output files in a different bucket.
4. The Step Functions-based job poller (state machine) triggered by the function polls for the translation job to
complete.
5. Step Functions sends an SNS notification when the translation is complete.
6. An email notification is sent via an Amazon Simple Notification Service (Amazon SNS) topic.
7. A Lambda function is triggered via SNS topic which moves the processed documents to output Amazon S3 bucket.

---------------------------------------------------------------------------------------------------------------
Endpoint to register user and create/retrieve input folder (user registration API):

POST / GET request -
https://<URL>.execute-api.us-east-1.amazonaws.com/Prod/user/<userName>

eg: https://<URL>.execute-api.us-east-1.amazonaws.com/Prod/user/roger

---------------------------------------------------------------------------------------------------------------
Endpoint to trigger translation process:

https://<URL>.execute-api.us-east-1.amazonaws.com/Prod/Job
The default target language is Spanish, you could specify a different language using the Amazon Translate language code.
By default the files are placed in a folder named input.

To specify user specific folder, pass the userName in the header of the below request.
In postman, you can add a header named 'userName' and specify the value.
Please make sure that the you register the user using the 'user registration API' api provide above.

Example: POST request -
https://<URL>.execute-api.us-east-1.amazonaws.com/Prod/Job?targetLanguage=ru
Converts the documents to russian language.

API response : It returns 'jobId' and 'jobStatus' of the Amazon batch translate process.
eg:
{
    "jobStatus": "SUBMITTED",
    "jobId": "a61d48efbce54de9a3a2eaba6ebe3622"
}

---------------------------------------------------------------------------------------------------------------
Endpoint to retrieve translation job details:

GET request -
https://<URL>.execute-api.us-east-1.amazonaws.com/Prod/job/{jobId}
eg :
https://<URL>.execute-api.us-east-1.amazonaws.com/Prod/job/6a9adf4b3a0mm29667c2a70233c3e501

########################################################################
Endpoint to retrieve presigned url for translated file:

GET request -
https://<URL>.execute-api.us-east-1.amazonaws.com/Prod/job/{jobId}/file/{fileName}
eg:
https://<URL>.execute-api.us-east-1.amazonaws.com/Prod/job/6a9adf4b3a03529644css70212c3e501/file/es.abc.txt

---------------------------------------------------------------------------------------------------------------