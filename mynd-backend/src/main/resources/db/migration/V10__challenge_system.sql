CREATE TABLE challenge
(
    id            UUID PRIMARY KEY,
    creatorId     VARCHAR(255) NOT NULL,
    type          VARCHAR(50)  NOT NULL,
    startDate     DATE         NOT NULL,
    endDate       DATE         NOT NULL,
    targetCount   INTEGER      NOT NULL,
    currentCount  INTEGER      NOT NULL DEFAULT 0,
    completed     BOOLEAN      NOT NULL DEFAULT FALSE,
    rewardClaimed BOOLEAN      NOT NULL DEFAULT FALSE
);