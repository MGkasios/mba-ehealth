package gr.hua.dit.gkasios.ehealth.exams.api;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import gr.hua.dit.gkasios.ehealth.exams.entity.ExamEntity;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

abstract public class CommonResource {

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    protected PutObjectRequest buildPutRequest(ExamEntity exam) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(exam.id.toHexString())
                .contentType(exam.fileMeta.getContentType())
                .build();
    }

    protected GetObjectRequest buildGetRequest(String objectKey) {
        return GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
    }

}
