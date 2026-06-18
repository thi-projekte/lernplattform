/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

/*
 * This file is part of the nwsnet/nws-event-bundle package.
 *
 *  © 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *
 *  For the full copyright and license information, please read the
 *  LICENSE file that was distributed with this source code.
 */

create table audio_file_element (
    originalFileName varchar(255) not null,
    s3Key varchar(255) not null,
    id uuid not null,
    primary key (id)
);

create table category (
    id uuid not null,
    createdAt timestamp(6) not null,
    creatorId varchar(255) not null,
    updatedAt timestamp(6) not null,
    color varchar(255),
    title varchar(255) not null,
    primary key (id)
);

create table content_element (
    type varchar(31) not null check ((type in ('PDF','VIDEO_FILE','URI','SPOTIFY_LINK','IMAGE','RTF','AUDIO_FILE','YOUTUBE_LINK'))),
    id uuid not null,
    createdAt timestamp(6) not null,
    creatorId varchar(255) not null,
    updatedAt timestamp(6) not null,
    icon varchar(64),
    rank integer,
    title varchar(255) not null,
    topic_id uuid,
    primary key (id)
);

create table image_element (
    originalFileName varchar(255) not null,
    s3Key varchar(255) not null,
    id uuid not null,
    primary key (id)
);

create table join_topic_category (
    topic_id uuid not null,
    category_id uuid not null
);

create table pdf_element (
    originalFileName varchar(255) not null,
    s3Key varchar(255) not null,
    id uuid not null,
    primary key (id)
);

create table rtf_element (
    rtfText TEXT not null,
    id uuid not null,
    primary key (id)
);

create table spotify_link_element (
    uri varchar(255) not null,
    id uuid not null,
    primary key (id)
);

create table topic (
    id uuid not null,
    createdAt timestamp(6) not null,
    creatorId varchar(255) not null,
    updatedAt timestamp(6) not null,
    estimatedLearningDuration integer,
    popularityScore integer,
    teaser TEXT not null,
    title varchar(255) not null,
    primary key (id)
);

create table topic_association (
    id uuid not null,
    createdAt timestamp(6) not null,
    creatorId varchar(255) not null,
    updatedAt timestamp(6) not null,
    foreign_topic_id uuid,
    owning_topic_id uuid,
    primary key (id)
);

create table uri_element (
    uri varchar(255) not null,
    id uuid not null,
    primary key (id)
);

create table video_file_element (
    originalFileName varchar(255) not null,
    s3Key varchar(255) not null,
    id uuid not null,
    primary key (id)
);

create table youtube_link_element (
    uri varchar(255) not null,
    id uuid not null,
    primary key (id)
);

alter table if exists audio_file_element
   add constraint FKbgmxsfy4cwecdmk2uob8rvo3h
   foreign key (id)
   references content_element;

alter table if exists content_element
   add constraint FKkrdbyuw5cpkoqpev75qig6mv3
   foreign key (topic_id)
   references topic
   on delete cascade;

alter table if exists image_element
   add constraint FKtov47h1wnfx4ar7us4b1d63bc
   foreign key (id)
   references content_element;

alter table if exists join_topic_category
   add constraint FKlxi950g0wb9x3jqqg0tj1g6dq
   foreign key (category_id)
   references category on delete cascade;

alter table if exists join_topic_category
   add constraint FKk8mvrj1ocn3jqinxyabi8gu9d
   foreign key (topic_id)
   references topic on delete cascade;

alter table if exists pdf_element
   add constraint FK62539yhj2ylbmwxqx6o9x05sq
   foreign key (id)
   references content_element;

alter table if exists rtf_element
   add constraint FKk0js6r29fd7kg1ebi6xqavysy
   foreign key (id)
   references content_element;

alter table if exists spotify_link_element
   add constraint FKgk2vqjtyvsfe7kbsscnx013v1
   foreign key (id)
   references content_element;

alter table if exists topic_association
   add constraint FK5jcuawwq1eft0ngdfh3en1eq4
   foreign key (foreign_topic_id)
   references topic
   on delete cascade;

alter table if exists topic_association
   add constraint FK49o0g26kfkppgb8fark3whm6o
   foreign key (owning_topic_id)
   references topic
   on delete cascade;

alter table if exists uri_element
   add constraint FKrlqhor939u0ky1oyvqm8nhrl0
   foreign key (id)
   references content_element;

alter table if exists video_file_element
   add constraint FKiaskso6qolxuwi40v7dygmp8i
   foreign key (id)
   references content_element;

alter table if exists youtube_link_element
   add constraint FKeypnhhp6hk0tolbrm55svid7k
   foreign key (id)
   references content_element;
