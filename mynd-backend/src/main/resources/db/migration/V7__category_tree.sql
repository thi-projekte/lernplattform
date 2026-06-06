CREATE EXTENSION IF NOT EXISTS ltree;
ALTER TABLE category ADD COLUMN path LTREE;

CREATE INDEX category_path_gist ON category USING GIST(path);
CREATE INDEX category_path_btree ON category USING BTREE(path);