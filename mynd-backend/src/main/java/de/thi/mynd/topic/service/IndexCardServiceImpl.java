/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.topic.service;

import de.thi.mynd.common.exception.EntityInstanceNotFoundException;
import de.thi.mynd.common.processor.MappingRegistry;
import de.thi.mynd.common.requests.AssociatedEntityRequest;
import de.thi.mynd.common.security.SecurityService;
import de.thi.mynd.topic.dto.IndexCardDto;
import de.thi.mynd.topic.entity.IndexCard;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.repository.IndexCardRepository;
import de.thi.mynd.topic.request.IndexCardRequest;
import de.thi.mynd.topic.security.IndexCardVoter;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public final class IndexCardServiceImpl implements IndexCardService {

  @Inject IndexCardRepository indexCardRepository;

  @Inject MappingRegistry mappingRegistry;

  @Inject SecurityService securityService;

  @Override
  public List<IndexCardDto> getIndexCardsForTopic(UUID topicId) {
    Log.tracef("Loading index cards for topic %s", topicId);
    List<IndexCard> cards = indexCardRepository.findByTopicId(topicId);
    return mappingRegistry.mapList(cards, IndexCardDto.class);
  }

  @Override
  @Transactional
  public IndexCardDto createIndexCard(IndexCardRequest request) {
    IndexCard card = new IndexCard();
    card.question = request.question;
    card.answer = request.answer;

    indexCardRepository.persistAndFlush(card);

    Log.infof("Created new index card");

    return mappingRegistry.map(card, IndexCardDto.class);
  }

  @Override
  @Transactional
  public void deleteIndexCard(UUID cardId) {
    Optional<IndexCard> cardOptional = indexCardRepository.findByIdOptional(cardId);
    if (cardOptional.isEmpty()) {
      throw new EntityInstanceNotFoundException("This card does not exist");
    }

    IndexCard card = cardOptional.get();
    securityService.denyUnlessGranted(card, IndexCardVoter.Delete);

    indexCardRepository.delete(card);
    indexCardRepository.flush();

    Log.infof("Successfully deleted index card %s", cardId);
  }

  @Override
  @Transactional
  public void updateTopicAssociation(Topic topic, List<AssociatedEntityRequest> elements) {
    List<UUID> ids = elements.stream().map(e -> e.id).toList();
    List<IndexCard> cards = indexCardRepository.findByIdsTypeSafe(ids);

    for (IndexCard card : cards) {
      securityService.denyUnlessGranted(card, IndexCardVoter.Assign);

      card.topic = topic;
      indexCardRepository.persistAndFlush(card);
    }

    indexCardRepository.flush();

    Log.infof("Successfully updated index card associations for topic %s", topic.id);
  }
}
