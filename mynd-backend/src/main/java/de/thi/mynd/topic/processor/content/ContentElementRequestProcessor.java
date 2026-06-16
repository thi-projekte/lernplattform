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
import de.thi.mynd.topic.request.content.ContentElementRequest;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public interface ContentElementRequestProcessor<R extends ContentElementRequest> {

  ContentElement creteContentElementFromRequest(R request, FileUpload file);

  boolean supports(ContentElementRequest request);
}
