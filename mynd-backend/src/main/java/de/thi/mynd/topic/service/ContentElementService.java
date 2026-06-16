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
import de.thi.mynd.topic.dto.content.ContentElementDto;
import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.request.AssociatedContentElementRequest;
import de.thi.mynd.topic.request.content.ContentElementRequest;
import java.util.List;
import java.util.UUID;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public interface ContentElementService {

  ContentElementDto createContentElement(ContentElementRequest request, FileUpload uploadedFile);

  List<ContentElementDto> getContentElementsForTopic(UUID topicId);

  ContentElement getContentElementEntityById(UUID contentElementId)
      throws EntityInstanceNotFoundException;

  long getCountOfElementsForTopicId(UUID topicId);

  void deleteContentElement(UUID elementId);

  void deleteContentElementFiles(ContentElement element);

  void updateTopicAssociation(Topic topic, List<AssociatedContentElementRequest> elements);
}
