INSERT INTO customer (id, created_time, updated_time, name, email, phone, date_of_birth, address) VALUES
    (1, now(), now(), 'Olena Kovalenko',   'olena.kovalenko@example.com',   '+380501112233',  '1990-04-12', 'Kyiv, Khreshchatyk St 22'),
    (2, now(), now(), 'Andrii Shevchenko', 'andrii.shevchenko@example.com', '+380672223344',  '1985-09-03', 'Lviv, Svobody Ave 15'),
    (3, now(), now(), 'John Smith',        'john.smith@example.com',        '+14165550101',   '1978-01-25', 'Toronto, 100 King St W'),
    (4, now(), now(), 'Anna Müller',       'anna.mueller@example.com',      '+4915112345678', '1995-11-30', 'Berlin, Unter den Linden 7'),
    (5, now(), now(), 'Maria Garcia',      'maria.garcia@example.com',      '+34600123456',   NULL,         NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO product (id, created_time, updated_time, name, decription) VALUES
    (1, now(), now(), 'Laptop',              '15-inch laptop, 16 GB RAM, 512 GB SSD'),
    (2, now(), now(), 'Wireless Mouse',      'Ergonomic 2.4 GHz wireless mouse'),
    (3, now(), now(), 'Mechanical Keyboard', 'Tenkeyless keyboard with brown switches'),
    (4, now(), now(), '27" Monitor',         '27-inch 4K IPS monitor'),
    (5, now(), now(), 'USB-C Hub',           '7-in-1 USB-C hub with HDMI and card reader'),
    (6, now(), now(), 'Headphones',          'Noise-cancelling over-ear headphones'),
    (7, now(), now(), 'Webcam',              '1080p webcam with built-in microphone'),
    (8, now(), now(), 'External SSD',        '1 TB portable SSD, USB 3.2')
ON CONFLICT (id) DO NOTHING;

INSERT INTO orders (id, created_time, updated_time, customer_id, currency, order_address, status) VALUES
    (1, now() - interval '10 days', now() - interval '9 days', 1, 'UAH', 'Kyiv, Khreshchatyk St 22',   'SUCCESSFUL'),
    (2, now() - interval '7 days',  now() - interval '7 days', 2, 'UAH', 'Lviv, Svobody Ave 15',       'RESERVED'),
    (3, now() - interval '5 days',  now() - interval '4 days', 3, 'USD', 'Toronto, 100 King St W',     'RELEASED'),
    (4, now() - interval '3 days',  now() - interval '3 days', 4, 'EUR', 'Berlin, Unter den Linden 7', 'FAILED'),
    (5, now() - interval '1 day',   now() - interval '1 day',  1, 'UAH', 'Kyiv, Velyka Vasylkivska 5', 'PENDING'),
    (6, now(),                      now(),                     5, 'EUR', 'Madrid, Gran Vía 28',        'PENDING')
ON CONFLICT (id) DO NOTHING;

INSERT INTO order_product (order_id, product_id)
SELECT v.order_id, v.product_id
FROM (VALUES
    (1, 1), (1, 2), (1, 3),
    (2, 4),
    (3, 6), (3, 7),
    (4, 8),
    (5, 2), (5, 5),
    (6, 1), (6, 6)
) AS v(order_id, product_id)
WHERE NOT EXISTS (
    SELECT 1 FROM order_product op
    WHERE op.order_id = v.order_id AND op.product_id = v.product_id
);

SELECT setval(pg_get_serial_sequence('customer', 'id'), (SELECT COALESCE(MAX(id), 0) + 1 FROM customer), false);
SELECT setval(pg_get_serial_sequence('product',  'id'), (SELECT COALESCE(MAX(id), 0) + 1 FROM product),  false);
SELECT setval(pg_get_serial_sequence('orders',   'id'), (SELECT COALESCE(MAX(id), 0) + 1 FROM orders),   false);
