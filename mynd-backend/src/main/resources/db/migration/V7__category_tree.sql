/*
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

CREATE EXTENSION IF NOT EXISTS ltree;
ALTER TABLE category ADD COLUMN path LTREE;

CREATE INDEX category_path_gist ON category USING GIST(path);
CREATE INDEX category_path_btree ON category USING BTREE(path);