/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.security;

import de.thi.mynd.common.security.Voter;
import de.thi.mynd.topic.entity.ContentElement;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Arrays;

@ApplicationScoped
public final class ContentElementVoter implements Voter<ContentElement> {

  public static final String Assign = "ASSIGN";
  public static final String Delete = "DELETE";

  private static final String[] allowedAttributes = new String[] {Assign, Delete};

  @Override
  public boolean supports(String attribute, Object subject) {
    return subject instanceof ContentElement
        && Arrays.asList(allowedAttributes).contains(attribute);
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
