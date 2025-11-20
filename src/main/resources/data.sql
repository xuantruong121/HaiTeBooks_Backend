DROP DATABASE IF EXISTS haitebooks_db;
CREATE DATABASE haitebooks_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE haitebooks_db;

-- ========================
-- 1️⃣ ROLES
-- ========================
CREATE TABLE roles
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO roles (name)
VALUES ('ADMIN'),
       ('USER');

-- ========================
-- 2️⃣ USERS
-- ========================
CREATE TABLE users
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    username  VARCHAR(100) NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    email     VARCHAR(150) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    address   VARCHAR(255),
    enabled   BOOLEAN DEFAULT TRUE,
    phone     VARCHAR(255),
    role_id   BIGINT       NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

INSERT INTO users (username, password, email, full_name, address, role_id)
VALUES ('admin', '$2a$10$GB09.wpAwHAP09fsQvN/LON7RHE/jkGWExDuWuBuD1OYCuOSOxfuW',
        'admin@bookstore.com', 'Administrator', 'Hà Nội', 1),
       ('user1', '$2a$10$6Tp/gz0GSxWd/vvsLQzcYOhRXVpyrhKj9qCzPKTjmZZqgdR18evxi',
        'user1@gmail.com', 'Nguyen Van A', 'TP. Hồ Chí Minh', 2);

-- ========================
-- 3️⃣ BOOK CATEGORIES
-- ========================
CREATE TABLE book_categories
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

INSERT INTO book_categories (name, description)
VALUES ('Công nghệ thông tin', 'Sách lập trình, công nghệ, phần mềm'),
       ('Kinh doanh', 'Sách về kinh tế, quản lý, marketing'),
       ('Tiểu thuyết', 'Sách truyện dài, văn học'),
       ('Thiếu nhi', 'Sách cho trẻ em'),
       ('Khoa học', 'Sách nghiên cứu và khoa học ứng dụng');

-- ========================
-- 4️⃣ BOOKS
-- ========================
CREATE TABLE books
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255)  NOT NULL,
    author      VARCHAR(255)  NOT NULL,
    barcode     VARCHAR(255)  NOT NULL UNIQUE,
    price       DOUBLE        NOT NULL,
    stock       INT           NOT NULL,
    description VARCHAR(1000) NOT NULL,
    image_url   VARCHAR(255),
    category_id BIGINT        NOT NULL,
    FOREIGN KEY (category_id) REFERENCES book_categories (id)
);

INSERT INTO books (title, author, barcode, price, stock, description, image_url, category_id)
VALUES ('Clean Code', 'Robert C. Martin', '9780132350884', 350000, 20,
        'A handbook of agile software craftsmanship.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/cleancode_kwld08.png', 1),
       ('The Pragmatic Programmer', 'Andrew Hunt', '9780201616224', 420000,
        15, 'Journey to mastery in software development.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848267/pragmatic_nmcybl.png', 1),
       ('Design Patterns', 'Erich Gamma', '9780201633610', 480000, 10,
        'Elements of reusable object-oriented software.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848266/designpatterns_bjpzpe.jpg', 1),
       ('Rich Dad Poor Dad', 'Robert Kiyosaki', '9780446677455', 250000, 30,
        'What the rich teach their kids about money.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848266/richdad_fnvbwv.png', 2),
       ('Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', '9780747532699', 320000, 50,
        'Fantasy novel for all ages.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1761848266/harrypotter_kwpopd.webp', 3),
       -- 1️⃣ Công nghệ thông tin (category_id = 1)
       ('Refactoring', 'Martin Fowler', '9790000000001', 380000, 10,
        'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646399/refactoring_kcdy2m.jpg', 1),
       ('Clean Architecture', 'Robert C. Martin', '9790000000002', 400000, 13,
        'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646396/Clean_Architecture_ggb0gj.jpg', 1),
       ('Code Complete', 'Steve McConnell', '9790000000003', 420000, 16,
        'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646397/Code_Complete_rsbj9i.jpg', 1),
       ('Introduction to Algorithms', 'Thomas H. Cormen', '9790000000004', 440000, 19,
        'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646395/Introduction_to_Algorithms_kn5osw.jpg', 1),
       ('Head First Design Patterns', 'Eric Freeman', '9790000000005', 460000, 22,
        'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646400/Head_First_Design_Patterns_quzvrf.jpg', 1),
       ('Domain-Driven Design', 'Eric Evans', '9790000000006', 380000, 10,
        'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646399/Domain-Driven_Design_biimid.jpg', 1),
       ('You Don''t Know JS: Up & Going', 'Kyle Simpson', '9790000000007', 400000, 13,
        'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646403/You_Dont_Know_JS_Up_and_Going_wcaybn.jpg', 1),
       ('Java Concurrency in Practice', 'Brian Goetz', '9790000000008', 420000, 16,
        'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646396/Java_Concurrency_in_Practice_esl2yd.jpg', 1),
       ('Spring in Action', 'Craig Walls', '9790000000009', 440000, 19,
        'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646400/Spring_in_Action_yt4a15.jpg', 1),
       ('Learning SQL', 'Alan Beaulieu', '9790000000010', 460000, 22,
        'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646397/Learning_SQL_pq9bkv.jpg', 1),
       ('Cracking the Coding Interview', 'Gayle Laakmann McDowell', '9790000000011', 380000, 10,
        'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646399/Cracking_the_Coding_Interview_pclvnv.jpg', 1),
       ('Effective Java', 'Joshua Bloch', '9790000000012', 400000, 13,
        'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646400/Effective_Java_ddlcw6.jpg', 1),
       ('Designing Data-Intensive Applications', 'Martin Kleppmann', '9790000000013', 420000, 16,
        'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646398/Designing_Data-Intensive_Applications_ek1okq.webp', 1),
       ('The Mythical Man-Month', 'Frederick P. Brooks Jr.', '9790000000014', 440000, 19,
        'Sách chuyên sâu về lập trình và kỹ thuật phần mềm.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646402/The_Mythical_Man-Month_zbaxyz.jpg', 1),

       -- 2️⃣ Kinh doanh & self-help (category_id = 2)
       ('The Lean Startup', 'Eric Ries', '9790000000015', 300000, 30,
        'Sách về kinh doanh, quản trị và phát triển bản thân.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646402/The_Lean_Startup_z2wik1.jpg', 2),
       ('Start With Why', 'Simon Sinek', '9790000000016', 320000, 35,
        'Sách về kinh doanh, quản trị và phát triển bản thân.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646400/Start_With_Why_g4hfj9.webp', 2),
       ('Atomic Habits', 'James Clear', '9790000000017', 340000, 20,
        'Sách về kinh doanh, quản trị và phát triển bản thân.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646396/Atomic_Habits_dksx6w.jpg', 2),
       ('Thinking, Fast and Slow', 'Daniel Kahneman', '9790000000018', 360000, 25,
        'Sách về kinh doanh, quản trị và phát triển bản thân.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646403/Thinking_Fast_and_Slow_rw2odf.jpg', 2),
       ('Zero to One', 'Peter Thiel', '9790000000019', 260000, 30,
        'Sách về kinh doanh, quản trị và phát triển bản thân.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646404/Zero_to_One_mhpygt.jpg', 2),
       ('Blue Ocean Strategy', 'W. Chan Kim', '9790000000020', 280000, 35,
        'Sách về kinh doanh, quản trị và phát triển bản thân.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646397/Blue_Ocean_Strategy_jmicoq.jpg', 2),
       ('Good to Great', 'Jim Collins', '9790000000021', 300000, 20,
        'Sách về kinh doanh, quản trị và phát triển bản thân.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646401/Good_to_Great_sslwgh.jpg', 2),
       ('The Psychology of Money', 'Morgan Housel', '9790000000022', 320000, 25,
        'Sách về kinh doanh, quản trị và phát triển bản thân.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646403/The_Psychology_of_Money_zvhfiq.jpg', 2),
       ('Principles', 'Ray Dalio', '9790000000023', 340000, 30,
        'Sách về kinh doanh, quản trị và phát triển bản thân.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646398/Principles_jt6yfc.webp', 2),

       -- 3️⃣ Tiểu thuyết (category_id = 3)
       ('To Kill a Mockingbird', 'Harper Lee', '9790000000024', 190000, 25,
        'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646403/To_Kill_a_Mockingbird_u2e96l.jpg', 3),
       ('1984', 'George Orwell', '9790000000025', 205000, 30,
        'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646396/1984_lado1p.jpg', 3),
       ('The Great Gatsby', 'F. Scott Fitzgerald', '9790000000026', 220000, 35,
        'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646402/The_Great_Gatsby_kq2hlh.webp', 3),
       ('The Alchemist', 'Paulo Coelho', '9790000000027', 235000, 25,
        'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646401/The_Alchemist_pdnqyl.jpg', 3),
       ('The Little Prince', 'Antoine de Saint-Exupéry', '9790000000028', 190000, 30,
        'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646402/The_Little_Prince_m6i5x4.jpg', 3),
       ('The Hobbit', 'J.R.R. Tolkien', '9790000000029', 205000, 35,
        'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646402/The_Hobbit_suqppq.jpg', 3),
       ('The Kite Runner', 'Khaled Hosseini', '9790000000030', 220000, 25,
        'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646404/The_Kite_Runner_ecouvq.jpg', 3),
       ('Norwegian Wood', 'Haruki Murakami', '9790000000031', 235000, 30,
        'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646397/Norwegian_Wood_qcin4t.jpg', 3),
       ('The Adventures of Sherlock Holmes', 'Arthur Conan Doyle', '9790000000032', 190000, 35,
        'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646401/The_Adventures_of_Sherlock_Holmes_gvxtxq.jpg', 3),
       ('Pride and Prejudice', 'Jane Austen', '9790000000033', 205000, 25,
        'Tiểu thuyết đặc sắc, nội dung lôi cuốn.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646398/Pride_and_Prejudice_ropdcn.jpg', 3),

       -- 4️⃣ Thiếu nhi (category_id = 4)
       ('Diary of a Wimpy Kid', 'Jeff Kinney', '9790000000034', 150000, 40,
        'Sách truyện dành cho thiếu nhi, dễ đọc và sinh động.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646399/Diary_of_a_Wimpy_Kid_v38lop.jpg', 4),
       ('Doraemon: Tuyển tập truyện ngắn', 'Fujiko F. Fujio', '9790000000035', 160000, 45,
        'Sách truyện dành cho thiếu nhi, dễ đọc và sinh động.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646399/Doraemon_Tuy%E1%BB%83n_t%E1%BA%ADp_truy%E1%BB%87n_ng%E1%BA%AFn_azhs4o.jpg', 4),
       ('Kính Vạn Hoa - Tập 1', 'Nguyễn Nhật Ánh', '9790000000036', 170000, 50,
        'Sách truyện dành cho thiếu nhi, dễ đọc và sinh động.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646396/K%C3%ADnh_V%E1%BA%A1n_Hoa_-_T%E1%BA%ADp_1_ouvnlr.jpg', 4),

       -- 5️⃣ Khoa học (category_id = 5)
       ('A Brief History of Time', 'Stephen Hawking', '9790000000037', 300000, 12,
        'Sách khoa học, giúp mở rộng kiến thức và tư duy.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646396/A_Brief_History_of_Time_lxjrg1.jpg', 5),
       ('Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', '9790000000038', 330000, 16,
        'Sách khoa học, giúp mở rộng kiến thức và tư duy.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646399/Sapiens_A_Brief_History_of_Humankind_bk1ofr.jpg', 5),
       ('Cosmos', 'Carl Sagan', '9790000000039', 360000, 20,
        'Sách khoa học, giúp mở rộng kiến thức và tư duy.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646398/Cosmos_tm100p.jpg', 5),
       ('The Selfish Gene', 'Richard Dawkins', '9790000000040', 390000, 24,
        'Sách khoa học, giúp mở rộng kiến thức và tư duy.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646403/The_Selfish_Gene_ifqcmu.jpg', 5),
       ('Homo Deus', 'Yuval Noah Harari', '9790000000041', 300000, 12,
        'Sách khoa học, giúp mở rộng kiến thức và tư duy.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646401/Homo_Deus_o6y7ho.png', 5),
       ('The Gene: An Intimate History', 'Siddhartha Mukherjee', '9790000000042', 330000, 16,
        'Sách khoa học, giúp mở rộng kiến thức và tư duy.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646401/The_Gene_An_Intimate_History_w4tszb.jpg', 5),
       ('Astrophysics for People in a Hurry', 'Neil deGrasse Tyson', '9790000000043', 360000, 20,
        'Sách khoa học, giúp mở rộng kiến thức và tư duy.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646396/Astrophysics_for_People_in_a_Hurry_u7lezb.jpg', 5),

       -- 6️⃣ Thêm 2 sách business cho đủ 45 cuốn mới
       ('Deep Work', 'Cal Newport', '9790000000044', 320000, 35,
        'Sách về kinh doanh, quản trị và phát triển bản thân.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646398/Deep_Work_vlisi0.jpg', 2),
       ('Hooked', 'Nir Eyal', '9790000000045', 260000, 20,
        'Sách về kinh doanh, quản trị và phát triển bản thân.',
        'https://res.cloudinary.com/dnxgjpunr/image/upload/v1763646395/Hooked_s5xuaq.jpg', 2);

-- ========================
-- 5️⃣ CART ITEMS
-- ========================
CREATE TABLE cart_items
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id  BIGINT NOT NULL,
    book_id  BIGINT NOT NULL,
    quantity INT    NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (book_id) REFERENCES books (id),
    UNIQUE (user_id, book_id)
);

INSERT INTO cart_items (user_id, book_id, quantity)
VALUES (2, 1, 1),
       (2, 2, 2);

-- ========================
-- 6️⃣ PROMOTIONS
-- ========================
CREATE TABLE promotions
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 VARCHAR(255) NOT NULL,
    code                 VARCHAR(50)  NOT NULL UNIQUE,
    discount_percent     DOUBLE       NOT NULL,
    start_date           DATE         NOT NULL,
    end_date             DATE         NOT NULL,
    quantity             INT          NOT NULL,
    minimum_order_amount DOUBLE       NULL,
    max_discount_amount  DOUBLE       NULL,
    is_active            BOOLEAN DEFAULT TRUE,
    created_by_user_id   BIGINT,
    approved_by_user_id  BIGINT,
    FOREIGN KEY (created_by_user_id) REFERENCES users (id),
    FOREIGN KEY (approved_by_user_id) REFERENCES users (id)
);

INSERT INTO promotions (name, code, discount_percent, start_date, end_date, quantity, minimum_order_amount,
                        max_discount_amount, is_active, created_by_user_id)
VALUES ('Giảm 20% tháng 12', 'SALE20', 20, '2025-12-01', '2025-12-31', 50, 99000, 50000, TRUE, 1),
       ('Tặng 10% khách hàng mới', 'NEW10', 10, '2025-01-01', '2025-12-31', 100, NULL, NULL, TRUE, 1);

-- ========================
-- 7️⃣ PROMOTION LOGS
-- ========================
CREATE TABLE promotion_logs
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    promotion_id  BIGINT      NOT NULL,
    actor_user_id BIGINT      NOT NULL,
    action        VARCHAR(50) NOT NULL,
    log_time      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (promotion_id) REFERENCES promotions (id),
    FOREIGN KEY (actor_user_id) REFERENCES users (id)
);

INSERT INTO promotion_logs (promotion_id, actor_user_id, action)
VALUES (1, 1, 'CREATE'),
       (2, 1, 'CREATE');

-- ========================
-- 8️⃣ ORDERS
-- ========================
CREATE TABLE orders
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT NOT NULL,
    order_date           DATETIME                                                         DEFAULT CURRENT_TIMESTAMP,
    total                DOUBLE NOT NULL,
    applied_promotion_id BIGINT NULL,
    status_order         ENUM ('PENDING','PROCESSING','SHIPPING','COMPLETED','CANCELLED') DEFAULT 'PENDING',
    address              VARCHAR(255),
    note                 VARCHAR(500),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (applied_promotion_id) REFERENCES promotions (id)
);

INSERT INTO orders (user_id, total, status_order, address, note)
VALUES (2, 770000, 'COMPLETED', 'TP. Hồ Chí Minh', 'Giao trong ngày');

-- Lấy ID đơn hàng vừa tạo
SET @last_order_id = LAST_INSERT_ID();

-- ========================
-- 9️⃣ ORDER ITEMS
-- ========================
CREATE TABLE order_items
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    book_id  BIGINT NOT NULL,
    quantity INT    NOT NULL,
    price    DOUBLE NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders (id),
    FOREIGN KEY (book_id) REFERENCES books (id)
);

INSERT INTO order_items (order_id, book_id, quantity, price)
VALUES (@last_order_id, 1, 1, 350000),
       (@last_order_id, 2, 1, 420000);

-- ========================
-- 🔟 PAYMENTS
-- ========================
CREATE TABLE payments
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id           BIGINT       NOT NULL UNIQUE,
    method             ENUM ('CASH', 'VNPAY')              DEFAULT 'CASH',
    amount             DOUBLE       NOT NULL,
    payment_date       DATETIME                            DEFAULT CURRENT_TIMESTAMP,
    status_payment     ENUM ('PENDING','SUCCESS','FAILED') DEFAULT 'PENDING',
    vnp_txn_ref        VARCHAR(100) NULL,
    vnp_transaction_no VARCHAR(100) NULL,
    vnp_response_code  VARCHAR(20)  NULL,
    vnp_bank_code      VARCHAR(50)  NULL,
    vnp_pay_date       VARCHAR(20)  NULL,
    raw_response       MEDIUMTEXT   NULL,
    FOREIGN KEY (order_id) REFERENCES orders (id)
);

INSERT INTO payments (order_id, method, amount, status_payment)
VALUES (@last_order_id, 'CASH', 770000, 'SUCCESS');

-- ========================
-- 1️⃣1️⃣ REVIEWS
-- ========================
CREATE TABLE reviews
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id    BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    rating     INT CHECK (rating BETWEEN 1 AND 5),
    comment    VARCHAR(1000),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

INSERT INTO reviews (book_id, user_id, rating, comment)
VALUES (1, 2, 5, 'Sách cực hay, đáng đọc!'),
       (2, 2, 4, 'Rất bổ ích cho lập trình viên.'),
       (4, 2, 5, 'Truyền cảm hứng tài chính.');

-- ========================
-- 1️⃣2️⃣ BOOK EMBEDDINGS
-- ========================
CREATE TABLE book_embeddings
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id        BIGINT NOT NULL UNIQUE,
    embedding_json MEDIUMTEXT,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books (id)
);

-- ========================
-- 1️⃣3️⃣ NOTIFICATIONS
-- ========================
CREATE TABLE notifications
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    content     TEXT,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_read     BOOLEAN  DEFAULT FALSE,
    sender_id   BIGINT       NULL,
    receiver_id BIGINT       NOT NULL,
    FOREIGN KEY (sender_id) REFERENCES users (id),
    FOREIGN KEY (receiver_id) REFERENCES users (id)
);

INSERT INTO notifications (title, content, sender_id, receiver_id, created_at, is_read)
VALUES ('Khuyến mãi tháng 12', 'Giảm giá toàn bộ sách CNTT đến 30%', 1, 2, NOW(), FALSE),
       ('Đặt hàng thành công', 'Đơn hàng #1 đã được tạo thành công!', NULL, 2, NOW(), FALSE),
       ('Đơn hàng đang xử lý', 'Đơn hàng #1 của bạn đang được xử lý.', 1, 2, NOW(), FALSE),
       ('Đơn hàng đang vận chuyển', 'Đơn hàng #1 đang được giao.', 1, 2, NOW(), FALSE),
       ('Đơn hàng đã hoàn tất', 'Hãy đánh giá sản phẩm nhé!', 1, 2, NOW(), TRUE),
       ('Cập nhật tài khoản', 'Thông tin tài khoản của bạn đã được cập nhật.', 1, 2, NOW(), FALSE);

-- ========================
-- 1️⃣4️⃣ FAVORITE BOOKS
-- ========================
CREATE TABLE favorite_books
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    book_id    BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_book (user_id, book_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);

INSERT INTO favorite_books (user_id, book_id, created_at)
VALUES (2, 1, NOW()),
       (2, 2, NOW()),
       (2, 3, NOW());

-- ========================
-- 1️⃣5️⃣ ĐƠN HÀNG THỨ 2 (VNPAY)
-- ========================
INSERT INTO orders (user_id, total, status_order, address, note)
VALUES (2, 350000, 'COMPLETED', 'TP. Hồ Chí Minh', 'Đơn hàng VNPAY');

SET @last_order_id_vnpay = LAST_INSERT_ID();

INSERT INTO order_items (order_id, book_id, quantity, price)
VALUES (@last_order_id_vnpay, 1, 1, 350000);

INSERT INTO payments (order_id, method, amount, status_payment, vnp_txn_ref, vnp_transaction_no, vnp_response_code,
                      vnp_bank_code, vnp_pay_date)
VALUES (@last_order_id_vnpay, 'VNPAY', 350000, 'SUCCESS',
        'VNP123456', '987654321', '00', 'NCB', '20250101123045');