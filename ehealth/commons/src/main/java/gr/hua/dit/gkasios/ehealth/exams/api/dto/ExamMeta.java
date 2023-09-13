package gr.hua.dit.gkasios.ehealth.exams.api.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RegisterForReflection
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ExamMeta {
    private String patientAfm;
    private String uploaderAfm;
    private Type type;

    public enum Type {
        BLOOD_GENERIC,
        XRAY,
        MRI
    }
}
