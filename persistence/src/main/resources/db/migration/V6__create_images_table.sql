CREATE TABLE images (
    id SERIAL PRIMARY KEY,
    data BYTEA NOT NULL,
    content_type VARCHAR(255) NOT NULL
);

ALTER TABLE packs ADD COLUMN image_id BIGINT REFERENCES images(id) ON DELETE SET NULL;

DO $$
DECLARE
    r RECORD;
    new_img_id BIGINT;
BEGIN
    FOR r IN SELECT id, image_data, image_content_type FROM packs WHERE image_data IS NOT NULL LOOP
        INSERT INTO images (data, content_type) VALUES (r.image_data, r.image_content_type) RETURNING id INTO new_img_id;
        UPDATE packs SET image_id = new_img_id WHERE id = r.id;
    END LOOP;
END $$;

ALTER TABLE packs DROP COLUMN image_data;
ALTER TABLE packs DROP COLUMN image_content_type;
