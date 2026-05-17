create table user_profile (
    creatorId varchar(255) primary key,
    createdAt timestamp(6) not null,
    updatedAt timestamp(6) not null,
    profilePictureKey varchar(255)
);
