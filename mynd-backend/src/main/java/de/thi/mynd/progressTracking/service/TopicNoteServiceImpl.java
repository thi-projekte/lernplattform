/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.progressTracking.service;

import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.progressTracking.dto.TopicNoteDto;
import de.thi.mynd.progressTracking.entity.TopicNote;
import de.thi.mynd.progressTracking.entity.TopicNoteId;
import de.thi.mynd.progressTracking.repository.TopicNoteRepository;
import de.thi.mynd.progressTracking.request.TopicNoteRequest;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;

@ApplicationScoped
public final class TopicNoteServiceImpl implements TopicNoteService {

  @Inject TopicNoteRepository topicNoteRepository;

  @Inject SecurityIdentity securityIdentity;

  @Inject MappingRegistry mappingRegistry;

  @Override
  public TopicNoteDto getTopicNoteForCurrentUser(UUID topicId) {
    TopicNoteId id = new TopicNoteId();
    id.creatorId = securityIdentity.getPrincipal().getName();
    id.topicId = topicId;
    TopicNote note =
        topicNoteRepository
            .findByIdOptional(id)
            .orElseGet(() -> createDefaultForCurrentUser(topicId));

    return mappingRegistry.map(note, TopicNoteDto.class);
  }

  @Override
  @Transactional
  public TopicNoteDto updateTopicNoteForCurrentUser(UUID topicId, TopicNoteRequest request) {
    TopicNoteId id = new TopicNoteId();
    id.creatorId = securityIdentity.getPrincipal().getName();
    id.topicId = topicId;
    TopicNote note =
        topicNoteRepository
            .findByIdOptional(id)
            .orElseGet(() -> createDefaultForCurrentUser(topicId));

    note.content = request.content;
    topicNoteRepository.persistAndFlush(note);

    return mappingRegistry.map(note, TopicNoteDto.class);
  }

  @Transactional
  @Override
  public TopicNote createDefaultForCurrentUser(UUID topicId) {
    TopicNoteId id = new TopicNoteId();
    id.creatorId = securityIdentity.getPrincipal().getName();
    id.topicId = topicId;

    TopicNote topicNote = new TopicNote();
    topicNote.id = id;
    topicNote.content = "";

    topicNoteRepository.persistAndFlush(topicNote);

    return topicNote;
  }
}
