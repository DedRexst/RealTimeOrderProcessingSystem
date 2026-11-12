INSERT INTO product (id, quantity, version)
SELECT v.id, v.quantity, 0
FROM (VALUES
    (1,  25),
    (2, 150),
    (3,  80),
    (4,  40),
    (5, 120),
    (6,  60),
    (7,  75),
    (8,   0)
) AS v(id, quantity)
WHERE NOT EXISTS (SELECT 1 FROM product p WHERE p.id = v.id);

INSERT INTO product_reserve (created_time, updated_time, order_id, quantity)
SELECT v.created_time, v.updated_time, v.order_id, v.quantity
FROM (VALUES
    (CURRENT_TIMESTAMP - INTERVAL '10' DAY, CURRENT_TIMESTAMP - INTERVAL '9' DAY, 1, 3),
    (CURRENT_TIMESTAMP - INTERVAL '7' DAY,  CURRENT_TIMESTAMP - INTERVAL '7' DAY, 2, 1)
) AS v(created_time, updated_time, order_id, quantity)
WHERE NOT EXISTS (SELECT 1 FROM product_reserve r WHERE r.order_id = v.order_id);


INSERT INTO product_product_reserve (product_reserve_id, product_id)
SELECT r.id, v.product_id
FROM (VALUES
    (1, 1), (1, 2), (1, 3),
    (2, 4)
) AS v(order_id, product_id)
JOIN product_reserve r ON r.order_id = v.order_id
WHERE NOT EXISTS (
    SELECT 1 FROM product_product_reserve ppr
    WHERE ppr.product_reserve_id = r.id AND ppr.product_id = v.product_id
);

