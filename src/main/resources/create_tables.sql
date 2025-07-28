CREATE TABLE IF NOT EXISTS users
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email    VARCHAR(100) NOT NULL UNIQUE,
    role     VARCHAR(20)  NOT NULL
);


CREATE TABLE IF NOT EXISTS families
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS family_members
(
    id        SERIAL PRIMARY KEY,
    family_id INTEGER     NOT NULL REFERENCES families (id) ON DELETE CASCADE,
    user_id   INTEGER     NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role      VARCHAR(20) NOT NULL,
    UNIQUE (family_id, user_id)
);

CREATE TABLE IF NOT EXISTS categories
(
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(50) NOT NULL,
    type      VARCHAR(10) NOT NULL CHECK (type IN ('income', 'expense')),
    family_id INTEGER REFERENCES families (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS transactions
(
    id          SERIAL PRIMARY KEY,
    family_id   INTEGER        NOT NULL REFERENCES families (id) ON DELETE CASCADE,
    category_id INTEGER        NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    amount      DECIMAL(10, 2) NOT NULL,
    type        VARCHAR(10)    NOT NULL CHECK (type IN ('income', 'expense')),
    description TEXT,
    date        DATE           NOT NULL,
    user_id     INTEGER        NOT NULL REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS budgets
(
    id           SERIAL PRIMARY KEY,
    family_id    INTEGER        NOT NULL REFERENCES families (id) ON DELETE CASCADE,
    category_id  INTEGER        NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    limit_amount DECIMAL(10, 2) NOT NULL,
    period       VARCHAR(10)    NOT NULL CHECK (period IN ('daily', 'weekly', 'monthly', 'yearly')),
    UNIQUE (family_id, category_id, period)
);

CREATE TABLE IF NOT EXISTS financial_goals
(
    id             SERIAL PRIMARY KEY,
    family_id      INTEGER        NOT NULL REFERENCES families (id) ON DELETE CASCADE,
    name           VARCHAR(100)   NOT NULL,
    description    TEXT,
    target_amount  DECIMAL(10, 2) NOT NULL,
    current_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    target_date    DATE           NOT NULL,
    priority       VARCHAR(10)    NOT NULL CHECK (priority IN ('low', 'medium', 'high'))
);

CREATE TABLE IF NOT EXISTS reminders
(
    id           SERIAL PRIMARY KEY,
    family_id    INTEGER      NOT NULL REFERENCES families (id) ON DELETE CASCADE,
    title        VARCHAR(100) NOT NULL,
    description  TEXT,
    due_date     DATE         NOT NULL,
    is_completed BOOLEAN      NOT NULL DEFAULT FALSE,
    type         VARCHAR(20)  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_transactions_family ON transactions (family_id);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions (date);
CREATE INDEX IF NOT EXISTS idx_budgets_family ON budgets (family_id);
CREATE INDEX IF NOT EXISTS idx_goals_family ON financial_goals (family_id);
CREATE INDEX IF NOT EXISTS idx_reminders_family ON reminders (family_id);
CREATE INDEX IF NOT EXISTS idx_reminders_due_date ON reminders (due_date);