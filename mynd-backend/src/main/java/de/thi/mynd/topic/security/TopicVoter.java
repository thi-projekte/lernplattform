package de.thi.mynd.topic.security;

import de.thi.mynd.common.security.Voter;
import de.thi.mynd.topic.entity.Topic;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Arrays;

@ApplicationScoped
public final class TopicVoter implements Voter<Topic> {

  public static final String Update = "UPDATE";
  public static final String Delete = "DELETE";

  private static final String[] allowedAttributes = new String[] {Update, Delete};

  @Override
  public boolean supports(String attribute, Object subject) {
    return subject instanceof Topic && Arrays.asList(allowedAttributes).contains(attribute);
  }

  @Override
  public boolean vote(SecurityIdentity identity, String attribute, Topic subject) {
    String creatorId = identity.getPrincipal().getName();

    return switch (attribute) {
      case Update -> creatorId.equals(subject.creatorId);
      case Delete -> creatorId.equals(subject.creatorId);
      default -> false;
    };
  }
}
