package gr.hua.dit.gkasios.ehealth.notifications.api.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RegisterForReflection
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Notification {
    private String from;
    private String to;
    private Type type;

    public enum Type {
        EXAM_UPLOADED,
        PERMISSIONS_REQUEST,
        PERMISSIONS_REPLY
    }
}
