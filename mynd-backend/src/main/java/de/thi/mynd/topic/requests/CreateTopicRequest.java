package de.thi.mynd.topic.requests;

import de.thi.mynd.common.requests.AssociatedEntityRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public final class CreateTopicRequest {
    @NotBlank
    public String title;

    @NotBlank
    @Max(512)
    public String teaser;

    @Min(1)
    @Max(3)
    public List<AssociatedEntityRequest> categories;

    @NotBlank
    public int estimatedLearningDuration;

    @Min(1)
    @Max(4)
    public List<AssociatedEntityRequest> relatedTopics;

    @Min(1)
    public List<AssociatedContentElementRequest> contentElements;
}
