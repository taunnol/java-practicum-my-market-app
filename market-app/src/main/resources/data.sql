INSERT INTO users(username, password_hash, role)
SELECT
    'user',
    '$2b$10$TEiiV3aHCKmvv1AsW55Q8uF0v3Ql.4DA5ZIzs4njSQf4abwp8urj2',
    'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user');  -- password

INSERT INTO users(username, password_hash, role)
SELECT
    'user2',
    '$2b$10$4fnYiyd4HPpOLIef3VJJAOem1ucc3N/MiPOvkKcFBh.bM2sOS60aa',  -- password2
    'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user2');

INSERT INTO users(username, password_hash, role)
SELECT
    'admin',
    '$2b$10$eGP2XZ86cHXlNxkcUdFyV.TtZ2qC6W8oyWiZJvSeBY0DlHLzyDuLO',  -- admin
    'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO items (title, description, img_path, price) VALUES
('Картофель', 'Крупный картофель 1 кг', 'images/potato.jpg', 67),
('Томаты', 'Томаты черри на ветке 250 г', '/images/tomato.jpg', 250),
('Яблоки', 'Яблоки зеленые 700 г', '/images/apple.jpg', 100),
('Вода питьевая', 'Вода питьевая негазированная 5 л', '/images/water.jpg', 150),
('Сахар', 'Сахар песок 1 кг', '/images/sugar.jpg', 72),
('Огурцы', 'Огурцы свежие 500 г', '/images/cucumber.jpg', 200),
('Дикий огурец', 'Дикий огурец 1 шт', '/images/wild-cucumber.jpg', 10000);