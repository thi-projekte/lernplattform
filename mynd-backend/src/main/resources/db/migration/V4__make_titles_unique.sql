/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

ALTER TABLE topic
    ADD CONSTRAINT topic_title_unique UNIQUE (title);

ALTER TABLE category
    ADD CONSTRAINT category_title_unique UNIQUE (title);