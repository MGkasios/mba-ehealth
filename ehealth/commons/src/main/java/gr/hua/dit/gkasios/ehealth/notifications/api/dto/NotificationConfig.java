package gr.hua.dit.gkasios.ehealth.notifications.api.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RegisterForReflection
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotificationConfig {
    private Boolean email;
}
