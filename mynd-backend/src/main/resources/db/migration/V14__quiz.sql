/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

CREATE TABLE index_card (
    id uuid not null PRIMARY KEY,
    createdAt timestamp(6) not null,
    creatorId varchar(255) not null,
    updatedAt timestamp(6) not null,
    topicId UUID REFERENCES topic(id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    answer TEXT NOT NULL
)