/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

CREATE TABLE topic_note (
    topicId UUID NOT NULL,
    creatorId varchar(255) not null,
    createdAt timestamp(6) not null,
    updatedAt timestamp(6) not null,
    content TEXT NOT NULL,
    PRIMARY KEY (topicId, creatorId)
);