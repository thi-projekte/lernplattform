/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

create table user_profile (
    creatorId varchar(255) not null primary key ,
    createdAt timestamp(6) not null,
    updatedAt timestamp(6) not null,
    profilePictureKey varchar(255)
);