package de.thi.mynd.progressTracking.request;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;

@RegisterForReflection
public final class TopicNoteRequest {
  @NotBlank public String content;
}
