CREATE TABLE IF NOT EXISTS images (
    id SERIAL PRIMARY KEY,
    data BYTEA NOT NULL,
    content_type VARCHAR(255) NOT NULL
);

ALTER TABLE packs ADD COLUMN IF NOT EXISTS image_id BIGINT REFERENCES images(id) ON DELETE SET NULL;

DO $$
DECLARE
    r RECORD;
    new_img_id BIGINT;
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'packs'
          AND column_name = 'image_data'
    ) THEN
        FOR r IN SELECT id, image_data, image_content_type FROM packs WHERE image_data IS NOT NULL LOOP
            INSERT INTO images (data, content_type) VALUES (r.image_data, r.image_content_type) RETURNING id INTO new_img_id;
            UPDATE packs SET image_id = new_img_id WHERE id = r.id;
        END LOOP;
    END IF;
END $$;

ALTER TABLE packs DROP COLUMN IF EXISTS image_data;
ALTER TABLE packs DROP COLUMN IF EXISTS image_content_type;
