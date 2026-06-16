/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package de.thi.mynd.topic.processor.content;

import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.entity.ContentType;
import de.thi.mynd.topic.entity.RtfElement;
import de.thi.mynd.topic.repository.ContentElementRepository;
import de.thi.mynd.topic.request.content.ContentElementRequest;
import de.thi.mynd.topic.request.content.RtfElementRequest;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
public final class RtfElementRequestProcessor
    implements ContentElementRequestProcessor<RtfElementRequest> {

  @Inject ContentElementRepository contentElementRepository;

  @Override
  @Transactional
  public ContentElement creteContentElementFromRequest(RtfElementRequest request, FileUpload file) {

    RtfElement contentElement = new RtfElement();
    contentElement.title = request.title;
    contentElement.type = ContentType.RTF;
    contentElement.icon = request.icon;
    contentElement.rtfText = request.rtfText;
    contentElementRepository.persistAndFlush(contentElement);

    Log.infof("Successfully created RTF content element with id: %s", contentElement.id);

    return contentElement;
  }

  @Override
  public boolean supports(ContentElementRequest request) {
    return request instanceof RtfElementRequest;
  }
}
