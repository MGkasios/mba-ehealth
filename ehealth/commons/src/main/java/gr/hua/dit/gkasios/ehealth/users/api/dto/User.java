package gr.hua.dit.gkasios.ehealth.users.api.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RegisterForReflection
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    private String afm;
    private String name;
    private String lastname;
    private String email;
}
