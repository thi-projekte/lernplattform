/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.progressTracking.repository;

import de.thi.mynd.common.repository.MyndBaseCustomIdRepository;
import de.thi.mynd.progressTracking.entity.TopicNote;
import de.thi.mynd.progressTracking.entity.TopicNoteId;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class TopicNoteRepository extends MyndBaseCustomIdRepository<TopicNote, TopicNoteId> {}
