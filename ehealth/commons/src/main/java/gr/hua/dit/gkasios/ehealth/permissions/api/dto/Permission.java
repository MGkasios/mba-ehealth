package gr.hua.dit.gkasios.ehealth.permissions.api.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RegisterForReflection
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Permission {
    private String id;
    private String patientAfm;
    private String userAfm;
    private Status status;

    public Permission(String patientAfm, String userAfm) {
        this.patientAfm = patientAfm;
        this.userAfm = userAfm;
    }

    public enum Status {
        PENDING,
        ACCEPTED,
        DECLINED,
        REVOKED
    }
}
