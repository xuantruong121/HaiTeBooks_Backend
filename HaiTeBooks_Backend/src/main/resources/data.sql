-- ============================================
--  DATABASE: haitebooks_db
--  Version: 1.0
--  Author: ChatGPT
-- ============================================

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
   full_name VARCHAR(150),
   address VARCHAR(255),
   enabled BOOLEAN DEFAULT TRUE,
   role_id BIGINT,
   FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Mật khẩu mã hoá BCrypt cho "admin123", "user123", "seller123"
INSERT INTO users (username, password, email, full_name, address, role_id)
VALUES
('admin', '$2a$10$GB09.wpAwHAP09fsQvN/LON7RHE/jkGWExDuWuBuD1OYCuOSOxfuW', 'admin@bookstore.com', 'Administrator', 'Hà Nội', 1),
('user1', '$2a$10$9LqE0g4Gvgp1Vk19hknPeODSMYw1dKNXobR9q1rH6cCBxFM8m2JIm', 'user1@gmail.com', 'Nguyen Van A', 'TP. Hồ Chí Minh', 2),
('seller1', '$2a$10$GA3eixK0yQ1EAmHbFWxPauoav3FjvA8M6eC4OHeLMcmDWVbOQvweO', 'seller1@gmail.com', 'Book Seller', 'Đà Nẵng', 3);

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
    author VARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT 0,
    description TEXT,
    image_url VARCHAR(255),
    category_id BIGINT,
    FOREIGN KEY (category_id) REFERENCES book_categories(id)
);

ALTER TABLE books
    ADD COLUMN barcode VARCHAR(100) UNIQUE AFTER author;

INSERT INTO books (title, author, price, stock, description, image_url, category_id) VALUES
 ('Clean Code', 'Robert C. Martin', 350000, 20, 'A handbook of agile software craftsmanship.', 'https://example.com/cleancode.jpg', 1),
 ('The Pragmatic Programmer', 'Andrew Hunt', 420000, 15, 'Journey to mastery in software development.', 'https://example.com/pragmatic.jpg', 1),
 ('Design Patterns', 'Erich Gamma', 480000, 10, 'Elements of reusable object-oriented software.', 'https://example.com/designpatterns.jpg', 1),
 ('Rich Dad Poor Dad', 'Robert Kiyosaki', 250000, 30, 'What the rich teach their kids about money.', 'https://example.com/richdad.jpg', 2),
 ('Harry Potter and the Sorcerer\'s Stone', 'J.K. Rowling', 320000, 50, 'Fantasy novel for all ages.', 'https://example.com/harrypotter.jpg', 3);

UPDATE books SET barcode = '9780132350884' WHERE title = 'Clean Code';
UPDATE books SET barcode = '9780201616224' WHERE title = 'The Pragmatic Programmer';
UPDATE books SET barcode = '9780201633610' WHERE title = 'Design Patterns';
UPDATE books SET barcode = '9780446677455' WHERE title = 'Rich Dad Poor Dad';
UPDATE books SET barcode = '9780747532699' WHERE title = 'Harry Potter and the Sorcerer''s Stone';

-- ========================
-- 5️⃣ CART ITEMS
-- ========================
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INT DEFAULT 1,
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
    total DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING','PROCESSING','COMPLETED','CANCELLED') DEFAULT 'PENDING',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO orders (user_id, total, status) VALUES
    (2, 770000, 'COMPLETED');

-- ========================
-- 7️⃣ ORDER ITEMS
-- ========================
CREATE TABLE order_items (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     order_id BIGINT NOT NULL,
     book_id BIGINT NOT NULL,
     quantity INT DEFAULT 1,
     price DECIMAL(10,2) NOT NULL,
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
                         comment TEXT,
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
      order_id BIGINT NOT NULL,
      method ENUM('CASH','CREDIT_CARD','MOMO','ZALOPAY') DEFAULT 'CASH',
      amount DECIMAL(10,2) NOT NULL,
      payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
      status ENUM('PENDING','SUCCESS','FAILED') DEFAULT 'SUCCESS',
      FOREIGN KEY (order_id) REFERENCES orders(id)
);

INSERT INTO payments (order_id, method, amount, status)
VALUES (1, 'MOMO', 770000, 'SUCCESS');

-- ========================
-- 🔟 BOOK_EMBEDDING
-- ========================
CREATE TABLE book_embeddings (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     book_id BIGINT NOT NULL,
     embedding JSON NOT NULL,
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     FOREIGN KEY (book_id) REFERENCES books(id)
);


