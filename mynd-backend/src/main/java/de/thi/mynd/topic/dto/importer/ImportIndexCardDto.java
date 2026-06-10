package de.thi.mynd.topic.dto.importer;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@RegisterForReflection
@Getter
public class ImportIndexCardDto {
    @NotBlank public String question;
    @NotBlank public String answer;
}
