/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

create table learn_progress_topic (
    topicId uuid not null,
    creatorId varchar(255) not null,
    createdAt timestamp(6) not null,
    updatedAt timestamp(6) not null,
    status varchar(32) not null,
    contentElementsToComplete bigint not null,
    primary key (topicId, creatorId)
);

create index learn_progress_topic_creatorId ON learn_progress_topic (creatorId);

create table learn_progress_content_element (
    topicId uuid not null,
    creatorId varchar(255) not null,
    createdAt timestamp(6) not null,
    updatedAt timestamp(6) not null,
    contentElementId uuid not null,
    completed boolean not null default false,
    primary key (topicId, creatorId, contentElementId),
    foreign key (topicId, creatorId) references learn_progress_topic(topicId, creatorId)
);

create index learn_progress_content_element_creatorId ON learn_progress_topic (topicId, creatorId);