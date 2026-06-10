CREATE TABLE index_card (
    id uuid not null PRIMARY KEY,
    createdAt timestamp(6) not null,
    creatorId varchar(255) not null,
    updatedAt timestamp(6) not null,
    topicId UUID REFERENCES topic(id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    answer TEXT NOT NULL
)