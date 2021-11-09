package translate.model;

import java.util.List;

public class JobInfo {
    private String jobId;
    private String jobName;
    private String jobStatus;
    private String sourceLanguageCode;
    private java.util.List<String> targetLanguageCodes;
    private String inputS3Uri;
    private String outputS3Uri;
    private FileInfo fileInfo;
    private List<String> processedFile;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getSourceLanguageCode() {
        return sourceLanguageCode;
    }

    public void setSourceLanguageCode(String sourceLanguageCode) {
        this.sourceLanguageCode = sourceLanguageCode;
    }

    public List<String> getTargetLanguageCodes() {
        return targetLanguageCodes;
    }

    public void setTargetLanguageCodes(List<String> targetLanguageCodes) {
        this.targetLanguageCodes = targetLanguageCodes;
    }

    public String getInputS3Uri() {
        return inputS3Uri;
    }

    public void setInputS3Uri(String inputS3Uri) {
        this.inputS3Uri = inputS3Uri;
    }

    public String getOutputS3Uri() {
        return outputS3Uri;
    }

    public void setOutputS3Uri(String outputS3Uri) {
        this.outputS3Uri = outputS3Uri;
    }

    public List<String> getProcessedFile() {
        return processedFile;
    }

    public void setProcessedFile(List<String> processedFile) {
        this.processedFile = processedFile;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }
}
