-- ============================================
-- 商品测试数据（各3条）
-- ============================================

-- 清空数据（可选，测试时使用）
-- TRUNCATE TABLE item_sku;
-- TRUNCATE TABLE item_detail;
-- TRUNCATE TABLE item;

-- ============================================
-- 1. 插入 item 表数据（3条商品）
-- ============================================

INSERT INTO item (name, sub_title, image, price, original_price, stock, sold, comment_count, avg_score, tags, status, category, brand, description, is_ad, category_id) VALUES
('Apple iPhone 15 Pro Max', '全新钛金属设计，A17 Pro芯片，6.7英寸超视网膜XDR显示屏', 'https://example.com/images/iphone15pro.jpg', 999900, 1099900, 100, 523, 89, 4.8, '新品,热销,推荐', 1, '手机数码', 'Apple', 'Apple iPhone 15 Pro Max，采用全新钛金属设计，搭载A17 Pro芯片，带来强大性能表现。', 0, 1),
('华为 Mate 60 Pro', '麒麟9000S芯片，鸿蒙OS 4.0，超可靠玄武架构', 'https://example.com/images/mate60pro.jpg', 699900, 799900, 150, 823, 156, 4.7, '国产,热销,旗舰', 1, '手机数码', '华为', '华为Mate 60 Pro，搭载麒麟9000S芯片，支持卫星通信，超可靠玄武架构设计。', 0, 1),
('MacBook Pro 14英寸', 'M3芯片，14.2英寸Liquid Retina XDR显示屏，18小时电池续航', 'https://example.com/images/macbookpro14.jpg', 1599900, 1799900, 80, 312, 67, 4.9, '笔记本,办公,高性能', 1, '电脑办公', 'Apple', 'MacBook Pro 14英寸，搭载M3芯片，性能强劲，适合专业创作和高效办公。', 0, 2);

-- ============================================
-- 2. 插入 item_detail 表数据（3条详情，关联上面的商品）
-- ============================================

-- iPhone 15 Pro Max 详情
INSERT INTO item_detail (item_id, banner_images, detail_html, spec_template, video_url) VALUES
(1, 
 '["https://example.com/images/iphone15pro/banner1.jpg", "https://example.com/images/iphone15pro/banner2.jpg", "https://example.com/images/iphone15pro/banner3.jpg", "https://example.com/images/iphone15pro/banner4.jpg"]',
 '<div class="detail-content"><h2>产品详情</h2><p>Apple iPhone 15 Pro Max采用全新钛金属设计，更轻更坚固。搭载A17 Pro芯片，性能提升显著。</p><img src="https://example.com/images/iphone15pro/detail1.jpg" /></div>',
 '[{"name": "选择颜色", "values": ["深空黑色", "原色钛金属", "白色钛金属", "蓝色钛金属"]}, {"name": "选择容量", "values": ["256GB", "512GB", "1TB"]}]',
 'https://example.com/videos/iphone15pro.mp4');

-- 华为 Mate 60 Pro 详情
INSERT INTO item_detail (item_id, banner_images, detail_html, spec_template, video_url) VALUES
(2,
 '["https://example.com/images/mate60pro/banner1.jpg", "https://example.com/images/mate60pro/banner2.jpg", "https://example.com/images/mate60pro/banner3.jpg"]',
 '<div class="detail-content"><h2>产品详情</h2><p>华为Mate 60 Pro搭载麒麟9000S芯片，支持卫星通信功能，超可靠玄武架构。</p><img src="https://example.com/images/mate60pro/detail1.jpg" /></div>',
 '[{"name": "选择颜色", "values": ["雅川青", "羽砂紫", "羽砂黑", "白沙银"]}, {"name": "选择容量", "values": ["256GB", "512GB"]}]',
 'https://example.com/videos/mate60pro.mp4');

-- MacBook Pro 14英寸 详情
INSERT INTO item_detail (item_id, banner_images, detail_html, spec_template, video_url) VALUES
(3,
 '["https://example.com/images/macbookpro14/banner1.jpg", "https://example.com/images/macbookpro14/banner2.jpg", "https://example.com/images/macbookpro14/banner3.jpg", "https://example.com/images/macbookpro14/banner4.jpg", "https://example.com/images/macbookpro14/banner5.jpg"]',
 '<div class="detail-content"><h2>产品详情</h2><p>MacBook Pro 14英寸搭载M3芯片，14.2英寸Liquid Retina XDR显示屏，18小时电池续航。</p><img src="https://example.com/images/macbookpro14/detail1.jpg" /></div>',
 '[{"name": "选择颜色", "values": ["深空灰色", "银色"]}, {"name": "选择配置", "values": ["M3 8核CPU/10核GPU/16GB/512GB", "M3 Pro 11核CPU/14核GPU/18GB/512GB", "M3 Pro 12核CPU/18核GPU/18GB/1TB"]}]',
 'https://example.com/videos/macbookpro14.mp4');

-- ============================================
-- 3. 插入 item_sku 表数据（每个商品3-4个SKU，共9-12条）
-- ============================================

-- iPhone 15 Pro Max 的 SKU（3个）
INSERT INTO item_sku (item_id, specs, price, stock, image) VALUES
-- 深空黑色 256GB
(1, '{"选择颜色": "深空黑色", "选择容量": "256GB"}', 999900, 30, 'https://example.com/images/iphone15pro/sku-black-256.jpg'),
-- 深空黑色 512GB
(1, '{"选择颜色": "深空黑色", "选择容量": "512GB"}', 1149900, 25, 'https://example.com/images/iphone15pro/sku-black-512.jpg'),
-- 原色钛金属 256GB
(1, '{"选择颜色": "原色钛金属", "选择容量": "256GB"}', 999900, 28, 'https://example.com/images/iphone15pro/sku-titanium-256.jpg');

-- 华为 Mate 60 Pro 的 SKU（3个）
INSERT INTO item_sku (item_id, specs, price, stock, image) VALUES
-- 雅川青 256GB
(2, '{"选择颜色": "雅川青", "选择容量": "256GB"}', 699900, 50, 'https://example.com/images/mate60pro/sku-green-256.jpg'),
-- 羽砂紫 256GB
(2, '{"选择颜色": "羽砂紫", "选择容量": "256GB"}', 699900, 48, 'https://example.com/images/mate60pro/sku-purple-256.jpg'),
-- 羽砂黑 512GB
(2, '{"选择颜色": "羽砂黑", "选择容量": "512GB"}', 799900, 45, 'https://example.com/images/mate60pro/sku-black-512.jpg');

-- MacBook Pro 14英寸 的 SKU（3个）
INSERT INTO item_sku (item_id, specs, price, stock, image) VALUES
-- 深空灰色 M3 基础配置
(3, '{"选择颜色": "深空灰色", "选择配置": "M3 8核CPU/10核GPU/16GB/512GB"}', 1599900, 25, 'https://example.com/images/macbookpro14/sku-gray-m3.jpg'),
-- 深空灰色 M3 Pro 配置
(3, '{"选择颜色": "深空灰色", "选择配置": "M3 Pro 11核CPU/14核GPU/18GB/512GB"}', 1999900, 20, 'https://example.com/images/macbookpro14/sku-gray-m3pro.jpg'),
-- 银色 M3 Pro 高配
(3, '{"选择颜色": "银色", "选择配置": "M3 Pro 12核CPU/18核GPU/18GB/1TB"}', 2199900, 18, 'https://example.com/images/macbookpro14/sku-silver-m3pro-max.jpg');

-- ============================================
-- 数据说明：
-- 1. item表：3条商品记录（ID: 1,2,3）
-- 2. item_detail表：3条详情记录，分别关联item的ID 1,2,3
-- 3. item_sku表：共9条SKU记录（每个商品3条）
--    - 商品1（iPhone 15 Pro Max）：3个SKU
--    - 商品2（华为 Mate 60 Pro）：3个SKU
--    - 商品3（MacBook Pro 14英寸）：3个SKU
-- ============================================
