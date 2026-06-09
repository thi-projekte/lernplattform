package de.thi.mynd.topic.security;

import de.thi.mynd.common.security.Voter;
import de.thi.mynd.topic.entity.IndexCard;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Arrays;

@ApplicationScoped
public final class IndexCardVoter implements Voter<IndexCard> {

  public static final String Assign = "ASSIGN";
  public static final String Delete = "DELETE";

  private static final String[] allowedAttributes = new String[] {Assign, Delete};

  @Override
  public boolean supports(String attribute, Object subject) {
    return subject instanceof IndexCard && Arrays.asList(allowedAttributes).contains(attribute);
  }

  @Override
  public boolean vote(SecurityIdentity identity, String attribute, IndexCard subject) {
    String creatorId = identity.getPrincipal().getName();

    return switch (attribute) {
      case Assign -> creatorId.equals(subject.creatorId);
      case Delete -> creatorId.equals(subject.creatorId);
      default -> false;
    };
  }
}
