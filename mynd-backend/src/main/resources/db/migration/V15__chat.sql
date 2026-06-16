/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

CREATE TABLE chat_message (
    id uuid not null,
    createdAt timestamp(6) not null,
    creatorId varchar(255) not null,
    updatedAt timestamp(6) not null,
    topicId UUID NOT NULL,
    message TEXT NOT NULL
);

CREATE INDEX idx_chat_message_topic_id ON chat_message USING hash(topicId);