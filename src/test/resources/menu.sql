INSERT INTO menus (id, name, price, status, created_at, updated_at)
VALUES
    (1, '아메리카노', 4500, 'ACTIVE', NOW(), NOW()),
    (2, '카페라떼', 5000, 'ACTIVE', NOW(), NOW()),
    (3, '바닐라라떼', 5500, 'ACTIVE', NOW(), NOW()),
    (4, '카푸치노', 5000, 'ACTIVE', NOW(), NOW()),
    (5, '카라멜마키아토', 5900, 'ACTIVE', NOW(), NOW()),
    (6, '콜드브루', 4800, 'ACTIVE', NOW(), NOW()),
    (7, '에스프레소', 4000, 'ACTIVE', NOW(), NOW()),
    (8, '디카페인 아메리카노', 5000, 'ACTIVE', NOW(), NOW()),
    (9, '돌체라떼', 5800, 'ACTIVE', NOW(), NOW()),
    (10, '판매중단 메뉴', 3000, 'INACTIVE', NOW(), NOW())
    ON DUPLICATE KEY UPDATE
                         name = VALUES(name),
                         price = VALUES(price),
                         status = VALUES(status),
                         updated_at = NOW();