CREATE TABLE feature_quota_tracking (
    creatorId VARCHAR(255) NOT NULL,
    feature VARCHAR(255) NOT NULL,
    createdAt TIMESTAMP(6) NOT NULL,
    updatedAt TIMESTAMP(6) NOT NULL,
    dayAccountedFor DATE,
    count INTEGER NOT NULL,
    PRIMARY KEY (creatorId, feature)
);