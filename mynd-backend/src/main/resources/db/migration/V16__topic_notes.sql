CREATE TABLE topic_note (
    topicId UUID NOT NULL,
    creatorId varchar(255) not null,
    createdAt timestamp(6) not null,
    updatedAt timestamp(6) not null,
    content TEXT NOT NULL,
    PRIMARY KEY (topicId, creatorId)
);