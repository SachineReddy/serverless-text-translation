package translate.mapper;

import com.amazonaws.services.translate.model.DescribeTextTranslationJobResult;
import translate.model.JobInfo;

public class JobInfoMapper {

    public JobInfo mapTranslateResultToJobInfo(DescribeTextTranslationJobResult describeTextTranslationJobResult) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobId(describeTextTranslationJobResult.getTextTranslationJobProperties().getJobId());
        jobInfo.setJobName(describeTextTranslationJobResult.getTextTranslationJobProperties().getJobName());
        jobInfo.setJobStatus(describeTextTranslationJobResult.getTextTranslationJobProperties().getJobStatus());
        jobInfo.setSourceLanguageCode(describeTextTranslationJobResult.getTextTranslationJobProperties().getSourceLanguageCode());
        jobInfo.setTargetLanguageCodes(describeTextTranslationJobResult.getTextTranslationJobProperties().getTargetLanguageCodes());
        jobInfo.setInputS3Uri(describeTextTranslationJobResult.getTextTranslationJobProperties().getInputDataConfig().getS3Uri());
        jobInfo.setOutputS3Uri(describeTextTranslationJobResult.getTextTranslationJobProperties().getOutputDataConfig().getS3Uri());
        return jobInfo;
    }
}
