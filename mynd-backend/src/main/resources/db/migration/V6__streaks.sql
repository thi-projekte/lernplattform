/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

CREATE TABLE streak (
    id UUID PRIMARY KEY,
    createdAt TIMESTAMP(6) NOT NULL,
    creatorId VARCHAR(255) NOT NULL,
    updatedAt TIMESTAMP(6) NOT NULL,
    type VARCHAR(32) NOT NULL CHECK ((type IN ('DAILY', 'WEEKLY', 'MONTHLY'))),
    startedAt TIMESTAMP(6) NOT NULL,
    endedAt TIMESTAMP(6),
    lastContinuedAt TIMESTAMP(6) NOT NULL
);

CREATE TABLE streak_continuation (
    id UUID PRIMARY KEY,
    createdAt TIMESTAMP(6) NOT NULL,
    creatorId VARCHAR(255) NOT NULL,
    updatedAt TIMESTAMP(6) NOT NULL
);

CREATE TABLE join_streak_streak_continuation (
    streak_id UUID NOT NULL REFERENCES streak(id),
    streak_continuation_id UUID NOT NULL REFERENCES streak_continuation(id),
    PRIMARY KEY (streak_id, streak_continuation_id)
);

CREATE TABLE streak_preference (
    creatorId VARCHAR(255) PRIMARY KEY,
    createdAt TIMESTAMP(6) NOT NULL,
    updatedAt TIMESTAMP(6) NOT NULL,
    type VARCHAR(32) NOT NULL CHECK ((type IN ('DAILY', 'WEEKLY', 'MONTHLY'))),
    isPublic BOOLEAN NOT NULL
);