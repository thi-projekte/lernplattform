/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.topic.security;

import de.thi.mynd.common.security.Voter;
import de.thi.mynd.topic.entity.Topic;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Arrays;

@ApplicationScoped
public final class TopicVoter implements Voter<Topic> {

  public static final String Create = "CREATE";
  public static final String Update = "UPDATE";
  public static final String Delete = "DELETE";
  public static final String AssignForeignTopics = "ASSIGN_FOREIGN_TOPICS";

  private static final String[] allowedAttributes =
      new String[] {Create, Update, Delete, AssignForeignTopics};

  @Override
  public boolean supports(String attribute, Object subject) {
    return subject instanceof Topic && Arrays.asList(allowedAttributes).contains(attribute);
  }

  @Override
  public boolean vote(SecurityIdentity identity, String attribute, Topic subject) {
    String creatorId = identity.getPrincipal().getName();

    return switch (attribute) {
      case Create -> identity.getRoles().contains("builder");
      case Update, AssignForeignTopics, Delete -> creatorId.equals(subject.creatorId);
      default -> false;
    };
  }
}
