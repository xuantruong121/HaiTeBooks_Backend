
DROP DATABASE IF EXISTS haitebooks_db;
CREATE DATABASE haitebooks_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE haitebooks_db;

-- ========================
-- 1️⃣ ROLES
-- ========================
CREATE TABLE roles (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO roles (name) VALUES ('ADMIN'), ('USER'), ('SELLER');

-- ========================
-- 2️⃣ USERS
-- ========================
CREATE TABLE users (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(100) NOT NULL UNIQUE,
password VARCHAR(255) NOT NULL,
email VARCHAR(150) NOT NULL UNIQUE,
full_name VARCHAR(150) NOT NULL,
address VARCHAR(255),
enabled BOOLEAN DEFAULT TRUE,
phone VARCHAR(255),
role_id BIGINT,
FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Mật khẩu mã hoá BCrypt cho "admin123", "user123", "seller123"
INSERT INTO users (username, password, email, full_name, address, role_id)
VALUES
('admin', '$2a$10$GB09.wpAwHAP09fsQvN/LON7RHE/jkGWExDuWuBuD1OYCuOSOxfuW', 'admin@bookstore.com',
'Administrator', 'Hà Nội', 1),
('user1', '$2a$10$6Tp/gz0GSxWd/vvsLQzcYOhRXVpyrhKj9qCzPKTjmZZqgdR18evxi', 'user1@gmail.com',
'Nguyen Van A', 'TP. Hồ Chí Minh', 2);

-- ========================
-- 3️⃣ BOOK CATEGORIES
-- ========================
CREATE TABLE book_categories (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(100) NOT NULL UNIQUE,
description TEXT
);

INSERT INTO book_categories (name, description) VALUES
('Công nghệ thông tin', 'Sách lập trình, công nghệ, phần mềm'),
('Kinh doanh', 'Sách về kinh tế, quản lý, marketing'),
('Tiểu thuyết', 'Sách truyện dài, văn học'),
('Thiếu nhi', 'Sách cho trẻ em'),
('Khoa học', 'Sách nghiên cứu và khoa học ứng dụng');

-- ========================
-- 4️⃣ BOOKS
-- ========================
CREATE TABLE books (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
title VARCHAR(255) NOT NULL,
author VARCHAR(255) NOT NULL,
barcode VARCHAR(255) NOT NULL UNIQUE,
price DOUBLE NOT NULL,
stock INT NOT NULL,
description VARCHAR(1000) NOT NULL,
image_url VARCHAR(255),
category_id BIGINT,
FOREIGN KEY (category_id) REFERENCES book_categories(id)
);

INSERT INTO books (title, author, barcode, price, stock, description, image_url, category_id) VALUES
('Clean Code', 'Robert C. Martin', '9780132350884', 350000, 20,
'A handbook of agile software craftsmanship.', 'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/cleancode_kwld08.png', 1),
('The Pragmatic Programmer', 'Andrew Hunt', '9780201616224', 420000, 15,
'Journey to mastery in software development.', 'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/pragmatic_nmcybl.png', 1),
('Design Patterns', 'Erich Gamma', '9780201633610', 480000, 10,
'Elements of reusable object-oriented software.', 'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848266/designpatterns_bjpzpe.jpg', 1),
('Rich Dad Poor Dad', 'Robert Kiyosaki', '9780446677455', 250000, 30,
'What the rich teach their kids about money.', 'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848266/richdad_fnvbwv.png', 2),
('Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', '9780747532699', 320000, 50,
'Fantasy novel for all ages.', 'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848266/harrypotter_kwpopd.webp', 3);

-- ========================
-- 5️⃣ CART ITEMS
-- ========================
CREATE TABLE cart_items (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id BIGINT NOT NULL,
book_id BIGINT NOT NULL,
quantity INT NOT NULL DEFAULT 1,
FOREIGN KEY (user_id) REFERENCES users(id),
FOREIGN KEY (book_id) REFERENCES books(id),
UNIQUE(user_id, book_id)
);

INSERT INTO cart_items (user_id, book_id, quantity) VALUES
(2, 1, 1),
(2, 2, 2);

-- ========================
-- 6️⃣ ORDERS
-- ========================
CREATE TABLE orders (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id BIGINT NOT NULL,
order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
total DOUBLE NOT NULL,
status_order ENUM('PENDING','PROCESSING','SHIPPING','COMPLETED','CANCELLED') DEFAULT 'PENDING',
address VARCHAR(255),
note VARCHAR(500),
FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Lưu ý: thêm address và note cho bản ghi mẫu
INSERT INTO orders (user_id, total, status_order, address, note) VALUES
(2, 770000, 'COMPLETED', 'TP. Hồ Chí Minh', 'Giao trong ngày');

-- ========================
-- 7️⃣ ORDER ITEMS
-- ========================
CREATE TABLE order_items (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
order_id BIGINT NOT NULL,
book_id BIGINT NOT NULL,
quantity INT NOT NULL,
price DOUBLE NOT NULL,
FOREIGN KEY (order_id) REFERENCES orders(id),
FOREIGN KEY (book_id) REFERENCES books(id)
);

INSERT INTO order_items (order_id, book_id, quantity, price) VALUES
(1, 1, 1, 350000),
(1, 2, 1, 420000);

-- ========================
-- 8️⃣ REVIEWS
-- ========================
CREATE TABLE reviews (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
book_id BIGINT NOT NULL,
user_id BIGINT NOT NULL,
rating INT CHECK (rating BETWEEN 1 AND 5),
comment VARCHAR(1000),
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (book_id) REFERENCES books(id),
FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO reviews (book_id, user_id, rating, comment) VALUES
(1, 2, 5, 'Sách cực hay, đáng đọc!'),
(2, 2, 4, 'Rất bổ ích cho lập trình viên.'),
(4, 2, 5, 'Truyền cảm hứng tài chính.');

-- ========================
-- 9️⃣ PAYMENTS
-- ========================
CREATE TABLE payments (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
order_id BIGINT NOT NULL UNIQUE,
method ENUM('CASH','CREDIT_CARD','MOMO','ZALO_PAY') DEFAULT 'CASH',
amount DOUBLE NOT NULL,
payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
status_payment ENUM('PENDING','SUCCESS','FAILED') DEFAULT 'SUCCESS',
FOREIGN KEY (order_id) REFERENCES orders(id)
);

INSERT INTO payments (order_id, method, amount, status_payment)
VALUES (1, 'MOMO', 770000, 'SUCCESS');

-- ========================
-- 🔟 BOOK EMBEDDINGS
-- ========================
CREATE TABLE book_embeddings (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
book_id BIGINT NOT NULL UNIQUE,
embedding_json TEXT,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (book_id) REFERENCES books(id)
);

INSERT INTO book_embeddings (book_id, embedding_json)
VALUES
    (1, '[0.12, 0.45, 0.33, 0.87, 0.56, 0.22]'),
    (2, '[0.77, 0.42, 0.11, 0.93, 0.21, 0.34]');