package de.thi.mynd.topic.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public final class IndexCardRequest {

    @NotBlank public String question;
    @NotBlank public String answer;
}
