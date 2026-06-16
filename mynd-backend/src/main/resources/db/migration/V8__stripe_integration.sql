/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

CREATE TABLE subscription (
    creatorId VARCHAR(255) NOT NULL PRIMARY KEY,
    createdAt TIMESTAMP(6) NOT NULL,
    updatedAt TIMESTAMP(6) NOT NULL,
    stripeCustomerId VARCHAR(255),
    subscriptionStatus VARCHAR(50) NOT NULL DEFAULT 'FREE',
    stripeSubscriptionId VARCHAR(255)
);