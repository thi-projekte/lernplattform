ALTER TABLE topic
    ADD COLUMN title_search_vector  tsvector,
    ADD COLUMN teaser_search_vector tsvector;
ADD COLUMN title_search_vector tsvector
        GENERATED ALWAYS AS (to_tsvector('german', coalesce(title, ''))) STORED,
    ADD COLUMN teaser_search_vector tsvector
        GENERATED ALWAYS AS (to_tsvector('german', coalesce(teaser, ''))) STORED;

CREATE INDEX idx_topic_title_search_vector  ON topic USING GIN (title_search_vector);
CREATE INDEX idx_topic_title_search_vector ON topic USING GIN (title_search_vector);
CREATE INDEX idx_topic_teaser_search_vector ON topic USING GIN (teaser_search_vector);

UPDATE topic
SET title_search_vector  = to_tsvector('german', coalesce(title, '')),
    teaser_search_vector = to_tsvector('german', coalesce(teaser, ''));