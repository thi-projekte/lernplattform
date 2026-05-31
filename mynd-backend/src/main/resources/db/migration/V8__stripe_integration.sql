CREATE TABLE subscription (
    creatorId VARCHAR(255) NOT NULL PRIMARY KEY,
    createdAt TIMESTAMP(6) NOT NULL,
    updatedAt TIMESTAMP(6) NOT NULL,
    stripeCustomerId VARCHAR(255),
    subscriptionStatus VARCHAR(50) NOT NULL DEFAULT 'FREE',
    stripeSubscriptionId VARCHAR(255)
);