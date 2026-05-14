package de.thi.mynd.progressTracking.rest;

import de.thi.mynd.progressTracking.dto.TopicLearnProgressDto;
import de.thi.mynd.progressTracking.service.LearnProgressService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/learn-progress")
@Authenticated
@Tag(name = "Learn Progress")
@SecurityRequirement(name = "keycloak")
public final class LearnProgressResource {

  @Inject LearnProgressService learnProgressService;

  @GET
  @Path("/topics/{topicId}")
  @Operation(
      summary = "Get personal learn progress for a topic",
      description =
          "Returns the current learn progress of the authenticated user for the given topic, including completion status and progress on individual content elements.")
  @Parameter(
      name = "topicId",
      description = "ID of the topic to retrieve progress for",
      required = true)
  @APIResponse(
      responseCode = "200",
      description = "Progress found and returned successfully",
      content = @Content(schema = @Schema(implementation = TopicLearnProgressDto.class)))
  @APIResponse(responseCode = "409", description = "The user has not started this topic yet")
  @APIResponse(responseCode = "401", description = "User is not authenticated")
  public TopicLearnProgressDto getPersonalProgressForTopic(UUID topicId) {
    return learnProgressService.getLearnProgressForTopic(topicId);
  }

  @POST
  @Path("/topics/{topicId}/start")
  @Operation(
      summary = "Start learning a topic",
      description =
          "Marks the given topic as started for the authenticated user and records the number of content elements to complete at the time of starting. Has no effect on existing progress.")
  @Parameter(name = "topicId", description = "ID of the topic to start", required = true)
  @APIResponse(responseCode = "200", description = "Topic successfully started")
  @APIResponse(responseCode = "409", description = "The user has already started this topic")
  @APIResponse(responseCode = "401", description = "User is not authenticated")
  public Response startLearningTopic(UUID topicId) {
    learnProgressService.startLearningTopicAsCurrentUser(topicId);
    return Response.ok().build();
  }

  @POST
  @Path("/topics/{topicId}/complete")
  @Operation(
      summary = "Manually complete a topic",
      description =
          "Marks the given topic as manually completed for the authenticated user, regardless of how many content elements have been completed. The topic must have been started first.")
  @Parameter(name = "topicId", description = "ID of the topic to complete", required = true)
  @APIResponse(responseCode = "200", description = "Topic successfully marked as completed")
  @APIResponse(responseCode = "409", description = "The user has not started this topic yet")
  @APIResponse(responseCode = "401", description = "User is not authenticated")
  public Response manuallyCompleteTopic(UUID topicId) {
    learnProgressService.manuallyCompleteTopicAsCurrentUser(topicId);
    return Response.ok().build();
  }

  @POST
  @Path("/content-elements/{contentElementId}/complete")
  @Operation(
      summary = "Mark a content element as completed",
      description =
          """
            Records the authenticated user's completion of a single content element. \
            The parent topic must have been started beforehand. \
            If this is the final content element required, the topic status is automatically \
            promoted to ALL_CONTENT_ELEMENTS_COMPLETED.\
            """)
  @Parameter(
      name = "contentElementId",
      description = "ID of the content element to mark as completed",
      required = true)
  @APIResponse(
      responseCode = "200",
      description = "Content element successfully marked as completed")
  @APIResponse(responseCode = "409", description = "The parent topic has not been started yet")
  @APIResponse(
      responseCode = "409",
      description = "This content element has already been completed by the user")
  @APIResponse(responseCode = "401", description = "User is not authenticated")
  public Response completeContentElement(UUID contentElementId) {
    learnProgressService.completeLearningContentElementAsCurrentUser(contentElementId);
    return Response.ok().build();
  }
}
