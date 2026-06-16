/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.service;

import de.thi.mynd.topic.dto.importer.FullImportDto;
import de.thi.mynd.topic.dto.importer.ImportCategoryDto;
import de.thi.mynd.topic.dto.importer.ImportTopicDto;
import de.thi.mynd.topic.importer.ImportContext;
import java.util.List;
import java.util.Map;

public interface ImportService {

  void importFull(FullImportDto importDto, boolean backendMode);

  ImportContext importCategories(List<ImportCategoryDto> categoryDtos, ImportContext ctx);

  ImportContext importTopics(List<ImportTopicDto> topicDtos, ImportContext context);

  void importAssociations(Map<String, List<String>> topicAssociations, ImportContext context);
}
