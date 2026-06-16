/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

ALTER TABLE user_profile ADD COLUMN invitationsLeft INTEGER NOT NULL DEFAULT 0;

CREATE TABLE invitation (
    id UUID NOT NULL PRIMARY KEY,
    createdAt TIMESTAMP(6) NOT NULL,
    creatorId VARCHAR(255) NOT NULL,
    updatedAt TIMESTAMP(6) NOT NULL,
    acceptedBy VARCHAR(255),
    mailSentTo VARCHAR(255) NOT NULL,
    redemptionSecret VARCHAR(255) NOT NULL
);