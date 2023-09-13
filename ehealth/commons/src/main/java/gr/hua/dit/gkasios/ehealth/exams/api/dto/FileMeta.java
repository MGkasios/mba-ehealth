package gr.hua.dit.gkasios.ehealth.exams.api.dto;

import java.time.LocalDateTime;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RegisterForReflection
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FileMeta {
    private String name;
    private String contentType;
    private Long size;
    public LocalDateTime created;
}
