ALTER TABLE topic
    ADD CONSTRAINT topic_title_unique UNIQUE (title);

ALTER TABLE category
    ADD CONSTRAINT category_title_unique UNIQUE (title);