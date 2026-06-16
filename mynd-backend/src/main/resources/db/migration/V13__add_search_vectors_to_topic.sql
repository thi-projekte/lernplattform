/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

ALTER TABLE topic
    ADD COLUMN title_search_vector  tsvector GENERATED ALWAYS AS (to_tsvector('german', coalesce(title, ''))) STORED,
    ADD COLUMN teaser_search_vector tsvector GENERATED ALWAYS AS (to_tsvector('german', coalesce(teaser, ''))) STORED;

CREATE INDEX idx_topic_title_search_vector  ON topic USING GIN (title_search_vector);
CREATE INDEX idx_topic_teaser_search_vector ON topic USING GIN (teaser_search_vector);