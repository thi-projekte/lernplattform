package de.thi.mynd.topic.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public final class CreateTopicAssociationRequest {

  @NotNull public UUID owningTopicId;

  @NotNull public UUID foreignTopicId;
}
