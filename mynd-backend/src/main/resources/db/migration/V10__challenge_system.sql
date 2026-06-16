/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

CREATE TABLE challenge
(
    id            UUID PRIMARY KEY,
    creatorId     VARCHAR(255) NOT NULL,
    createdAt TIMESTAMP(6) NOT NULL,
    updatedAt TIMESTAMP(6) NOT NULL,
    type          VARCHAR(50)  NOT NULL,
    startDate     DATE         NOT NULL,
    endDate       DATE         NOT NULL,
    targetCount   INTEGER      NOT NULL,
    currentCount  INTEGER      NOT NULL DEFAULT 0,
    completed     BOOLEAN      NOT NULL DEFAULT FALSE,
    rewardClaimed BOOLEAN      NOT NULL DEFAULT FALSE
);