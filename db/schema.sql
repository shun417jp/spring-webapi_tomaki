-- 新しい環境で、moneylogデータベースへ接続してから実行します。
-- 既存テーブルを削除・変更するSQLではありません。
CREATE TABLE expenses (
    id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    amount INTEGER NOT NULL CHECK (amount > 0),
    category VARCHAR(50) NOT NULL DEFAULT 'OTHER',
    payment_method VARCHAR(50),
    expense_date DATE NOT NULL,
    memo VARCHAR(255)
);
