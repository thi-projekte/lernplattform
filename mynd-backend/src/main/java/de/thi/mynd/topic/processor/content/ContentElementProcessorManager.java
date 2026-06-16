/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.processor.content;

import de.thi.mynd.topic.entity.ContentElement;
import de.thi.mynd.topic.request.content.ContentElementRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
public final class ContentElementProcessorManager {

  @Inject Instance<ContentElementRequestProcessor<? extends ContentElementRequest>> processors;

  @SuppressWarnings("unchecked")
  public ContentElement createContentElementFromRequest(
      ContentElementRequest request, FileUpload file) {
    ContentElementRequestProcessor<ContentElementRequest> processor =
        (ContentElementRequestProcessor<ContentElementRequest>)
            processors.stream()
                .filter(p -> p.supports(request))
                .findFirst()
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "No processor for type: " + request.getClass()));

    return processor.creteContentElementFromRequest(request, file);
  }
}
