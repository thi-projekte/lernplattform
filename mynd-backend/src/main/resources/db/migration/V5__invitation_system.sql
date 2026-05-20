ALTER TABLE user_profile ADD COLUMN invitations_left INTEGER NOT NULL DEFAULT 0;

CREATE TABLE invitation (
    id UUID NOT NULL,
    createdAt TIMESTAMP(6) NOT NULL,
    creatorId VARCHAR(255) NOT NULL,
    updatedAt TIMESTAMP(6) NOT NULL,
    acceptedBy VARCHAR(255),
    mailSentTo VARCHAR(255) NOT NULL,
    redemptionSecret VARCHAR(255) NOT NULL
);