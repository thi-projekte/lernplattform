package de.thi.mynd.topic.request;

import jakarta.validation.constraints.NotBlank;

public final class IndexCardRequest {

    @NotBlank public String question;
    @NotBlank public String answer;
}
