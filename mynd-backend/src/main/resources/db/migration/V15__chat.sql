CREATE TABLE chat_message (
    id uuid not null,
    createdAt timestamp(6) not null,
    creatorId varchar(255) not null,
    updatedAt timestamp(6) not null,
    topicId UUID NOT NULL,
    message TEXT NOT NULL
);

CREATE INDEX idx_chat_message_topic_id ON chat_message USING hash(topicId);