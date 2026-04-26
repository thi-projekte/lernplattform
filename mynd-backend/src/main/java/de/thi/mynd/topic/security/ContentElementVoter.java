package de.thi.mynd.topic.security;

import de.thi.mynd.common.security.Voter;
import de.thi.mynd.topic.entity.ContentElement;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;

@ApplicationScoped
public final class ContentElementVoter implements Voter<ContentElement> {

    public final static String Assign = "ASSIGN";
    public final static String Delete = "DELETE";

    private static final String[] allowedAttributes = new String[] {Assign, Delete};

    @Override
    public boolean supports(String attribute, Object subject) {
        return subject instanceof ContentElement && Arrays.asList(allowedAttributes).contains(attribute);
    }

    @Override
    public boolean vote(SecurityIdentity identity, String attribute, ContentElement subject) {
        String creatorId = identity.getPrincipal().getName();

        return switch (attribute) {
            case Assign -> creatorId.equals(subject.creatorId);
            case Delete -> creatorId.equals(subject.creatorId);
            default -> false;
        };
    }
}
