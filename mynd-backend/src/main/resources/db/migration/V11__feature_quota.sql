CREATE TABLE feature_quota_tracking (
    creatorId VARCHAR(255) NOT NULL PRIMARY KEY,
    createdAt TIMESTAMP(6) NOT NULL,
    updatedAt TIMESTAMP(6) NOT NULL,
    dayAccountedFor DATE NOT NULL,
    feature VARCHAR(255) NOT NULL,
    count INTEGER NOT NULL
);