package gr.hua.dit.gkasios.ehealth.exams.entity;

import gr.hua.dit.gkasios.ehealth.exams.api.dto.ExamMeta;
import gr.hua.dit.gkasios.ehealth.exams.api.dto.FileMeta;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import io.smallrye.mutiny.Multi;

@MongoEntity(collection = "Exams")
public class ExamEntity extends ReactivePanacheMongoEntity {
    public ExamMeta examMeta;
    public FileMeta fileMeta;

    public ExamEntity() {
    }

    public ExamEntity(ExamMeta examMeta, FileMeta fileMeta) {
        this.examMeta = examMeta;
        this.fileMeta = fileMeta;
    }

    public static ExamEntity create(ExamMeta examMeta, FileMeta fileMeta) {
        return new ExamEntity(examMeta, fileMeta);
    }

    public static Multi<ExamEntity> streamAllExams() {
        return streamAll();
    }

}
