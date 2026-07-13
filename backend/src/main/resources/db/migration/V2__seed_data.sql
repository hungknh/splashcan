INSERT INTO categories (name, slug) VALUES
    ('Soda', 'soda'),
    ('Nước tăng lực', 'nang-luong'),
    ('Sữa Protein', 'protein');

INSERT INTO products (name, description, base_price, category_id, thumbnail_url, model_3d_url, is_active) VALUES
    ('SplashCan Classic Soda', 'Soda vị cổ điển, sảng khoái tức thì.', 15000, (SELECT id FROM categories WHERE slug = 'soda'), '/images/classic-soda.png', '/models/classic-soda.glb', true),
    ('SplashCan Citrus Blast', 'Soda vị cam chanh mát lạnh.', 15000, (SELECT id FROM categories WHERE slug = 'soda'), '/images/citrus-blast.png', '/models/citrus-blast.glb', true),
    ('SplashCan Energy Rush', 'Nước tăng lực bùng nổ năng lượng.', 20000, (SELECT id FROM categories WHERE slug = 'nang-luong'), '/images/energy-rush.png', '/models/energy-rush.glb', true);

INSERT INTO product_variants (product_id, flavor, size_ml, price, stock_quantity, sku) VALUES
    ((SELECT id FROM products WHERE name = 'SplashCan Classic Soda'), 'Original', 330, 15000, 100, 'SC-CLS-330'),
    ((SELECT id FROM products WHERE name = 'SplashCan Classic Soda'), 'Original', 500, 20000, 80, 'SC-CLS-500'),
    ((SELECT id FROM products WHERE name = 'SplashCan Citrus Blast'), 'Cam chanh', 330, 15000, 100, 'SC-CIT-330'),
    ((SELECT id FROM products WHERE name = 'SplashCan Energy Rush'), 'Berry', 250, 20000, 60, 'SC-ENR-250');
