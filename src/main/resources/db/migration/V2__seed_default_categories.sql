-- ==============================================================================
-- Flyway Migration V2: Seed Default System Categories
-- Predefined Expense and Income categories accessible to all users
-- ==============================================================================

-- Seed Expense Categories
INSERT INTO categories (user_id, name, type, icon, color_code, is_system_default) VALUES
(NULL, 'Groceries', 'EXPENSE', 'shopping-cart', '#2ECC71', TRUE),
(NULL, 'Utilities', 'EXPENSE', 'zap', '#F39C12', TRUE),
(NULL, 'Rent & Housing', 'EXPENSE', 'home', '#E74C3C', TRUE),
(NULL, 'Transportation', 'EXPENSE', 'truck', '#3498DB', TRUE),
(NULL, 'Entertainment', 'EXPENSE', 'film', '#9B59B6', TRUE),
(NULL, 'Health & Medical', 'EXPENSE', 'activity', '#E67E22', TRUE),
(NULL, 'Dining Out', 'EXPENSE', 'coffee', '#D35400', TRUE),
(NULL, 'Education', 'EXPENSE', 'book-open', '#16A085', TRUE),
(NULL, 'Personal Care', 'EXPENSE', 'smile', '#1ABC9C', TRUE),
(NULL, 'Miscellaneous', 'EXPENSE', 'more-horizontal', '#7F8C8D', TRUE);

-- Seed Income Categories
INSERT INTO categories (user_id, name, type, icon, color_code, is_system_default) VALUES
(NULL, 'Salary', 'INCOME', 'briefcase', '#27AE60', TRUE),
(NULL, 'Freelance', 'INCOME', 'laptop', '#2980B9', TRUE),
(NULL, 'Investments', 'INCOME', 'trending-up', '#8E44AD', TRUE),
(NULL, 'Gifts & Grants', 'INCOME', 'gift', '#D63031', TRUE),
(NULL, 'Rental Income', 'INCOME', 'key', '#00B894', TRUE),
(NULL, 'Other Income', 'INCOME', 'plus-circle', '#636E72', TRUE);
