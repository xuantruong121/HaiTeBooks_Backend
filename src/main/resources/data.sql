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

INSERT INTO roles (name) VALUES 
    ('ADMIN'), 
    ('USER');

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
    role_id BIGINT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

INSERT INTO users (username, password, email, full_name, address, role_id) VALUES
    ('admin', '$2a$10$GB09.wpAwHAP09fsQvN/LON7RHE/jkGWExDuWuBuD1OYCuOSOxfuW',
     'admin@bookstore.com', 'Administrator', 'Hà Nội', 1),
    ('user1', '$2a$10$6Tp/gz0GSxWd/vvsLQzcYOhRXVpyrhKj9qCzPKTjmZZqgdR18evxi',
     'user1@gmail.com', 'Nguyen Van A', 'TP. Hồ Chí Minh', 2);

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
    category_id BIGINT NOT NULL,
    FOREIGN KEY (category_id) REFERENCES book_categories(id)
);

INSERT INTO books (title, author, barcode, price, stock, description, image_url, category_id) VALUES
    -- 1️⃣ Công nghệ thông tin (category_id = 1)
    ('Refactoring', 'Martin Fowler', '9790000000001', 380000, 10,
     'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/refactoring.jpg', 1),
    ('Clean Architecture', 'Robert C. Martin', '9790000000002', 400000, 13,
     'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/clean-architecture.jpg', 1),
    ('Code Complete', 'Steve McConnell', '9790000000003', 420000, 16,
     'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/code-complete.jpg', 1),
    ('Introduction to Algorithms', 'Thomas H. Cormen', '9790000000004', 440000, 19,
     'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/introduction-to-algorithms.jpg', 1),
    ('Head First Design Patterns', 'Eric Freeman', '9790000000005', 460000, 22,
     'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/head-first-design-patterns.jpg', 1),
    ('Domain-Driven Design', 'Eric Evans', '9790000000006', 380000, 10,
     'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/domain-driven-design.jpg', 1),
    ('You Don''t Know JS: Up & Going', 'Kyle Simpson', '9790000000007', 400000, 13,
     'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/you-don-t-know-js-up-going.jpg', 1),
    ('Java Concurrency in Practice', 'Brian Goetz', '9790000000008', 420000, 16,
     'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/java-concurrency-in-practice.jpg', 1),
    ('Spring in Action', 'Craig Walls', '9790000000009', 440000, 19,
     'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/spring-in-action.jpg', 1),
    ('Learning SQL', 'Alan Beaulieu', '9790000000010', 460000, 22,
     'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/learning-sql.jpg', 1),
    ('Cracking the Coding Interview', 'Gayle Laakmann McDowell', '9790000000011', 380000, 10,
     'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/cracking-the-coding-interview.jpg', 1),
    ('Effective Java', 'Joshua Bloch', '9790000000012', 400000, 13,
     'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/effective-java.jpg', 1),
    ('Designing Data-Intensive Applications', 'Martin Kleppmann', '9790000000013', 420000, 16,
     'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/designing-data-intensive-applications.jpg', 1),
    ('The Mythical Man-Month', 'Frederick P. Brooks Jr.', '9790000000014', 440000, 19,
     'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/the-mythical-man-month.jpg', 1),

    -- 2️⃣ Kinh doanh & self-help (category_id = 2)
    ('The Lean Startup', 'Eric Ries', '9790000000015', 300000, 30,
     'Sách về kinh doanh, quản trị và phát triển bản thân.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/the-lean-startup.jpg', 2),
    ('Start With Why', 'Simon Sinek', '9790000000016', 320000, 35,
     'Sách về kinh doanh, quản trị và phát triển bản thân.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/start-with-why.jpg', 2),
    ('Atomic Habits', 'James Clear', '9790000000017', 340000, 20,
     'Sách về kinh doanh, quản trị và phát triển bản thân.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/atomic-habits.jpg', 2),
    ('Thinking, Fast and Slow', 'Daniel Kahneman', '9790000000018', 360000, 25,
     'Sách về kinh doanh, quản trị và phát triển bản thân.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/thinking-fast-and-slow.jpg', 2),
    ('Zero to One', 'Peter Thiel', '9790000000019', 260000, 30,
     'Sách về kinh doanh, quản trị và phát triển bản thân.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/zero-to-one.jpg', 2),
    ('Blue Ocean Strategy', 'W. Chan Kim', '9790000000020', 280000, 35,
     'Sách về kinh doanh, quản trị và phát triển bản thân.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/blue-ocean-strategy.jpg', 2),
    ('Good to Great', 'Jim Collins', '9790000000021', 300000, 20,
     'Sách về kinh doanh, quản trị và phát triển bản thân.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/good-to-great.jpg', 2),
    ('The Psychology of Money', 'Morgan Housel', '9790000000022', 320000, 25,
     'Sách về kinh doanh, quản trị và phát triển bản thân.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/the-psychology-of-money.jpg', 2),
    ('Principles', 'Ray Dalio', '9790000000023', 340000, 30,
     'Sách về kinh doanh, quản trị và phát triển bản thân.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/principles.jpg', 2),

    -- 3️⃣ Tiểu thuyết (category_id = 3)
    ('To Kill a Mockingbird', 'Harper Lee', '9790000000024', 190000, 25,
     'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/to-kill-a-mockingbird.jpg', 3),
    ('1984', 'George Orwell', '9790000000025', 205000, 30,
     'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/1984.jpg', 3),
    ('The Great Gatsby', 'F. Scott Fitzgerald', '9790000000026', 220000, 35,
     'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/the-great-gatsby.jpg', 3),
    ('The Alchemist', 'Paulo Coelho', '9790000000027', 235000, 25,
     'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/the-alchemist.jpg', 3),
    ('The Little Prince', 'Antoine de Saint-Exupéry', '9790000000028', 190000, 30,
     'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/the-little-prince.jpg', 3),
    ('The Hobbit', 'J.R.R. Tolkien', '9790000000029', 205000, 35,
     'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/the-hobbit.jpg', 3),
    ('The Kite Runner', 'Khaled Hosseini', '9790000000030', 220000, 25,
     'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/the-kite-runner.jpg', 3),
    ('Norwegian Wood', 'Haruki Murakami', '9790000000031', 235000, 30,
     'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/norwegian-wood.jpg', 3),
    ('The Adventures of Sherlock Holmes', 'Arthur Conan Doyle', '9790000000032', 190000, 35,
     'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/the-adventures-of-sherlock-holmes.jpg', 3),
    ('Pride and Prejudice', 'Jane Austen', '9790000000033', 205000, 25,
     'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/pride-and-prejudice.jpg', 3),

    -- 4️⃣ Thiếu nhi (category_id = 4)
    ('Diary of a Wimpy Kid', 'Jeff Kinney', '9790000000034', 150000, 40,
     'Sách truyện dành cho thiếu nhi, dễ đọc và sinh động.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/diary-of-a-wimpy-kid.jpg', 4),
    ('Doraemon: Tuyển tập truyện ngắn', 'Fujiko F. Fujio', '9790000000035', 160000, 45,
     'Sách truyện dành cho thiếu nhi, dễ đọc và sinh động.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/doraemon-tuyen-tap-truyen-ngan.jpg', 4),
    ('Kính Vạn Hoa - Tập 1', 'Nguyễn Nhật Ánh', '9790000000036', 170000, 50,
     'Sách truyện dành cho thiếu nhi, dễ đọc và sinh động.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/kinh-van-hoa-tap-1.jpg', 4),

    -- 5️⃣ Khoa học (category_id = 5)
    ('A Brief History of Time', 'Stephen Hawking', '9790000000037', 300000, 12,
     'Sách khoa học, giúp mở rộng kiến thức và tư duy.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/a-brief-history-of-time.jpg', 5),
    ('Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', '9790000000038', 330000, 16,
     'Sách khoa học, giúp mở rộng kiến thức và tư duy.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/sapiens-a-brief-history-of-humankind.jpg', 5),
    ('Cosmos', 'Carl Sagan', '9790000000039', 360000, 20,
     'Sách khoa học, giúp mở rộng kiến thức và tư duy.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/cosmos.jpg', 5),
    ('The Selfish Gene', 'Richard Dawkins', '9790000000040', 390000, 24,
     'Sách khoa học, giúp mở rộng kiến thức và tư duy.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/the-selfish-gene.jpg', 5),
    ('Homo Deus', 'Yuval Noah Harari', '9790000000041', 300000, 12,
     'Sách khoa học, giúp mở rộng kiến thức và tư duy.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/homo-deus.jpg', 5),
    ('The Gene: An Intimate History', 'Siddhartha Mukherjee', '9790000000042', 330000, 16,
     'Sách khoa học, giúp mở rộng kiến thức và tư duy.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/the-gene-an-intimate-history.jpg', 5),
    ('Astrophysics for People in a Hurry', 'Neil deGrasse Tyson', '9790000000043', 360000, 20,
     'Sách khoa học, giúp mở rộng kiến thức và tư duy.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/astrophysics-for-people-in-a-hurry.jpg', 5),

    -- 6️⃣ Thêm 2 sách business cho đủ 45 cuốn mới
    ('Deep Work', 'Cal Newport', '9790000000044', 320000, 35,
     'Sách về kinh doanh, quản trị và phát triển bản thân.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/deep-work.jpg', 2),
    ('Hooked', 'Nir Eyal', '9790000000045', 260000, 20,
     'Sách về kinh doanh, quản trị và phát triển bản thân.',
     'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/hooked.jpg', 2);

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
-- 6️⃣ PROMOTIONS
-- ========================
CREATE TABLE promotions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_percent DOUBLE NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    quantity INT NOT NULL,
    minimum_order_amount DOUBLE NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_by_user_id BIGINT,
    approved_by_user_id BIGINT,
    FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    FOREIGN KEY (approved_by_user_id) REFERENCES users(id)
);

INSERT INTO promotions (name, code, discount_percent, start_date, end_date, quantity, minimum_order_amount, is_active, created_by_user_id) VALUES
    ('Giảm 20% tháng 12', 'SALE20', 20, '2025-12-01', '2025-12-31', 50, 99000, TRUE, 1),
    ('Tặng 10% khách hàng mới', 'NEW10', 10, '2025-01-01', '2025-12-31', 100, NULL, TRUE, 1);

-- ========================
-- 7️⃣ PROMOTION LOGS
-- ========================
CREATE TABLE promotion_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    promotion_id BIGINT NOT NULL,
    actor_user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    log_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (promotion_id) REFERENCES promotions(id),
    FOREIGN KEY (actor_user_id) REFERENCES users(id)
);

INSERT INTO promotion_logs (promotion_id, actor_user_id, action) VALUES
    (1, 1, 'CREATE'),
    (2, 1, 'CREATE');

-- ========================
-- 8️⃣ ORDERS
-- ========================
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    total DOUBLE NOT NULL,
    applied_promotion_id BIGINT NULL,
    status_order ENUM('PENDING','PROCESSING','SHIPPING','COMPLETED','CANCELLED') DEFAULT 'PENDING',
    address VARCHAR(255),
    note VARCHAR(500),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (applied_promotion_id) REFERENCES promotions(id)
);

INSERT INTO orders (user_id, total, status_order, address, note) VALUES
    (2, 770000, 'COMPLETED', 'TP. Hồ Chí Minh', 'Giao trong ngày');

-- Lấy ID đơn hàng vừa tạo
SET @last_order_id = LAST_INSERT_ID();

-- ========================
-- 9️⃣ ORDER ITEMS
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
    (@last_order_id, 1, 1, 350000),
    (@last_order_id, 2, 1, 420000);

-- ========================
-- 🔟 PAYMENTS
-- ========================
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    method ENUM('CASH', 'VNPAY') DEFAULT 'CASH',
    amount DOUBLE NOT NULL,
    payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status_payment ENUM('PENDING','SUCCESS','FAILED') DEFAULT 'PENDING',
    vnp_txn_ref VARCHAR(100) NULL,
    vnp_transaction_no VARCHAR(100) NULL,
    vnp_response_code VARCHAR(20) NULL,
    vnp_bank_code VARCHAR(50) NULL,
    vnp_pay_date VARCHAR(20) NULL,
    raw_response MEDIUMTEXT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

INSERT INTO payments (order_id, method, amount, status_payment) VALUES
    (@last_order_id, 'CASH', 770000, 'SUCCESS');

-- ========================
-- 1️⃣1️⃣ REVIEWS
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
-- 1️⃣2️⃣ BOOK EMBEDDINGS
-- ========================
CREATE TABLE book_embeddings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL UNIQUE,
    embedding_json MEDIUMTEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id)
);

INSERT INTO book_embeddings (book_id, embedding_json) VALUES
    (1, '[0.12, 0.45, 0.33, 0.87, 0.56, 0.22]'),
    (2, '[0.77, 0.42, 0.11, 0.93, 0.21, 0.34]');

-- ========================
-- 1️⃣3️⃣ NOTIFICATIONS
-- ========================
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    sender_id BIGINT NULL,
    receiver_id BIGINT NOT NULL,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);

INSERT INTO notifications (title, content, sender_id, receiver_id, created_at, is_read) VALUES
    ('Khuyến mãi tháng 12', 'Giảm giá toàn bộ sách CNTT đến 30%', 1, 2, NOW(), FALSE),
    ('Đặt hàng thành công', 'Đơn hàng #1 đã được tạo thành công!', NULL, 2, NOW(), FALSE),
    ('Đơn hàng đang xử lý', 'Đơn hàng #1 của bạn đang được xử lý.', 1, 2, NOW(), FALSE),
    ('Đơn hàng đang vận chuyển', 'Đơn hàng #1 đang được giao.', 1, 2, NOW(), FALSE),
    ('Đơn hàng đã hoàn tất', 'Hãy đánh giá sản phẩm nhé!', 1, 2, NOW(), TRUE),
    ('Cập nhật tài khoản', 'Thông tin tài khoản của bạn đã được cập nhật.', 1, 2, NOW(), FALSE);

-- ========================
-- 1️⃣4️⃣ FAVORITE BOOKS
-- ========================
CREATE TABLE favorite_books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_book (user_id, book_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

INSERT INTO favorite_books (user_id, book_id, created_at) VALUES
    (2, 1, NOW()),
    (2, 2, NOW()),
    (2, 3, NOW());

-- ========================
-- 1️⃣5️⃣ ĐƠN HÀNG THỨ 2 (VNPAY)
-- ========================
INSERT INTO orders (user_id, total, status_order, address, note) VALUES
    (2, 350000, 'COMPLETED', 'TP. Hồ Chí Minh', 'Đơn hàng VNPAY');

SET @last_order_id_vnpay = LAST_INSERT_ID();

INSERT INTO order_items (order_id, book_id, quantity, price) VALUES
    (@last_order_id_vnpay, 1, 1, 350000);

INSERT INTO payments (order_id, method, amount, status_payment, vnp_txn_ref, vnp_transaction_no, vnp_response_code, vnp_bank_code, vnp_pay_date) VALUES
    (@last_order_id_vnpay, 'VNPAY', 350000, 'SUCCESS',
     'VNP123456', '987654321', '00', 'NCB', '20250101123045');