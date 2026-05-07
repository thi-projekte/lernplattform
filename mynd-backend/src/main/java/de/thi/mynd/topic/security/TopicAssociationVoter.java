package de.thi.mynd.topic.security;

import de.thi.mynd.common.security.Voter;
import de.thi.mynd.topic.entity.TopicAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Arrays;

@ApplicationScoped
public final class TopicAssociationVoter implements Voter<TopicAssociation> {

  public static final String Delete = "DELETE";

  private static final String[] allowedAttributes = new String[] {Delete};

  @Override
  public boolean supports(String attribute, Object subject) {
    return subject instanceof TopicAssociation
        && Arrays.asList(allowedAttributes).contains(attribute);
  }

  @Override
  public boolean vote(SecurityIdentity identity, String attribute, TopicAssociation subject) {
    String creatorId = identity.getPrincipal().getName();

    return switch (attribute) {
      case Delete -> creatorId.equals(subject.creatorId);
      default -> false;
    };
  }
}
