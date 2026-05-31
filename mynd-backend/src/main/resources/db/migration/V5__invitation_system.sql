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