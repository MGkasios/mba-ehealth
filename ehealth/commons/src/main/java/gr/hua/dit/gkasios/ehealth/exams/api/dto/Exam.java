package gr.hua.dit.gkasios.ehealth.exams.api.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RegisterForReflection
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Exam {
    private String fileId;
    private ExamMeta examMeta;
    private FileMeta fileMeta;
}
