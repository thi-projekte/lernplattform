/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

CREATE TABLE feature_quota_tracking (
    creatorId VARCHAR(255) NOT NULL,
    feature VARCHAR(255) NOT NULL,
    createdAt TIMESTAMP(6) NOT NULL,
    updatedAt TIMESTAMP(6) NOT NULL,
    dayAccountedFor DATE,
    count INTEGER NOT NULL,
    PRIMARY KEY (creatorId, feature)
);