CREATE SCHEMA IF NOT EXISTS tmatrix;
use tmatrix;

-- address Table
CREATE TABLE IF NOT EXISTS address (
    address_id INT AUTO_INCREMENT PRIMARY KEY,
    address_hash CHAR(64) NOT NULL,
    country VARCHAR(50),
    city VARCHAR(50),
    street_name VARCHAR(100),
    street_number VARCHAR(20),
    postal_code VARCHAR(20)
);

CREATE UNIQUE INDEX idx_address_hash ON address(address_hash);

CREATE TABLE IF NOT EXISTS role (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(16) NOT NULL,

    UNIQUE(role_name)
);

-- Contains general user data. Multiple users can have the same address.
CREATE TABLE IF NOT EXISTS `user` (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    -- address_id INT NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,

    password_hash VARCHAR(256) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(32),
    date_of_birth DATE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

);

CREATE INDEX idx_first_last_name ON `user`(first_name, last_name);
CREATE UNIQUE INDEX idx_username_email ON `user`(username, email);

-- join table defining many-to-many relationship
CREATE TABLE IF NOT EXISTS user_role (
    user_id INT NOT NULL,
    role_id INT NOT NULL,

    FOREIGN KEY (user_id) REFERENCES `user`(user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(role_id)
);
CREATE UNIQUE INDEX idx_user_role ON user_role(user_id, role_id);

CREATE TABLE IF NOT EXISTS user_address (
    user_id INT NOT NULL,
    address_id INT NOT NULL,

    FOREIGN KEY (user_id) REFERENCES `user`(user_id) ON DELETE CASCADE,
    FOREIGN KEY (address_id) REFERENCES address(address_id)
);
CREATE UNIQUE INDEX idx_user_address ON user_address(user_id, address_id);


CREATE TABLE IF NOT EXISTS account (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(12) NOT NULL,

    total_balance DECIMAL(34, 16) NOT NULL, -- 34: total # digits that can be stored, 16: # digits after decimal point.

    risk_tolerance ENUM('AGGRESSIVE', 'MODERATE', 'CONSERVATIVE') DEFAULT 'MODERATE',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES `user`(user_id) ON DELETE CASCADE
);
CREATE INDEX idx_account_name ON account(account_name);
CREATE INDEX idx_account_total_balance ON account(total_balance);
CREATE UNIQUE INDEX idx_user_account ON account(user_id); -- creates 1-1 relationship for user-account tables.
CREATE UNIQUE INDEX idx_account_number ON account(account_number);


-- currency table
CREATE TABLE IF NOT EXISTS currency (
    currency_id INT AUTO_INCREMENT PRIMARY KEY,
    currency_code VARCHAR(6) NOT NULL,
    currency_name VARCHAR(50) NOT NULL,

    is_active BOOLEAN DEFAULT TRUE
);

CREATE UNIQUE INDEX idx_currency_code ON currency(currency_code);
CREATE INDEX idx_currency_name ON currency(currency_name);

-- currency exchange rates
CREATE TABLE IF NOT EXISTS currency_exchange_rate (
    currency_exchange_rate_id INT AUTO_INCREMENT PRIMARY KEY,
    source_currency_id INT NOT NULL,
    target_currency_id INT NOT NULL,

    exchange_rate DECIMAL(24, 10) NOT NULL, -- 40 total digits, 17 after decimal point
    updated_date DATETIME ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (source_currency_id) REFERENCES currency(currency_id) ON DELETE CASCADE,
    FOREIGN KEY (target_currency_id) REFERENCES currency(currency_id) ON DELETE CASCADE,

    UNIQUE (source_currency_id, target_currency_id)
);


-- wallet Table. An account can have multiple wallets with a unique currency.
-- There can be only 1 usd wallet, eur wallet, bgn wallet, etc.
CREATE TABLE IF NOT EXISTS wallet (
    wallet_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    currency_id INT NOT NULL,
    balance DECIMAL(20, 6) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_main BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    FOREIGN KEY (currency_id) REFERENCES currency(currency_id),

    UNIQUE (account_id, currency_id)  -- an account can't have multiple wallets for the same currency
);

-- financial market table
CREATE TABLE IF NOT EXISTS market (
    market_id INT PRIMARY KEY AUTO_INCREMENT,
    currency_id INT NOT NULL,  -- the default currency of the market
    market_name VARCHAR(100) NOT NULL,
    market_code VARCHAR(10) NOT NULL,
    market_cap DECIMAL(30, 2),
    country VARCHAR(50),
    timezone VARCHAR(10),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (currency_id) REFERENCES currency(currency_id)
);
CREATE UNIQUE INDEX idx_market_code ON market(market_code);
CREATE UNIQUE INDEX idx_market_name ON market(market_name);

-- trading hours information for different markets
CREATE TABLE IF NOT EXISTS trading_hours (
    market_id INT PRIMARY KEY,
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    timezone VARCHAR(50) NOT NULL,
    FOREIGN KEY (market_id) REFERENCES market(market_id)
);

CREATE TABLE IF NOT EXISTS sector (
    sector_id INT AUTO_INCREMENT PRIMARY KEY,
    sector_name VARCHAR(50) NOT NULL,

    UNIQUE(sector_name)
);

CREATE TABLE IF NOT EXISTS region (
    region_id INT AUTO_INCREMENT PRIMARY KEY,
    region_name VARCHAR(50) NOT NULL,

    UNIQUE(region_name)
);

-- product_type Table (stocks, bonds, mutual funds, ETFs, etc)
-- This data model does not support options contracts
CREATE TABLE IF NOT EXISTS product_type (
    product_type_id INT PRIMARY KEY AUTO_INCREMENT,
    product_type_name VARCHAR(32) NOT NULL
);
CREATE UNIQUE INDEX idx_type_name ON product_type(product_type_name);

-- storing financial products on a given market(ex: APPL stock on NYSE, MSFT bonds on NASDAQ, etc.)
CREATE TABLE IF NOT EXISTS product (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_type_id INT NOT NULL,
    sector_id INT NOT NULL, -- to track asset distribution across diff sectors.
    region_id INT NOT NULL, --  to geographic assets distribution(US stocks vs international stocks)

    product_name VARCHAR(100) NOT NULL,
    isin VARCHAR(12) NOT NULL,

    -- volatility
    standard_deviation DECIMAL(10, 6), -- measure an asset's(product) volatility
    volatile_coefficient DECIMAL(5, 2), -- AKA beta coefficient. Measures the volatility of a security or portfolio compared to the market.

    description VARCHAR(255),

    FOREIGN KEY (product_type_id) REFERENCES product_type(product_type_id),
    FOREIGN KEY (sector_id) REFERENCES sector(sector_id),
    FOREIGN KEY (region_id) REFERENCES region(region_id),

    UNIQUE(isin)
);
CREATE INDEX idx_product_name ON product(product_name);
CREATE INDEX idx_product_standard_deviation ON product(standard_deviation);
CREATE INDEX idx_product_volatile_coefficient ON product(volatile_coefficient);


-- junction table for market and product relationship.
CREATE TABLE IF NOT EXISTS market_product (
    market_product_id INT PRIMARY KEY AUTO_INCREMENT,
    market_id INT NOT NULL,
    product_id INT NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    current_price DECIMAL(20, 10) NOT NULL,
    listing_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Optional: Tracks when the product was listed on this market

    -- liquidity info
    avg_daily_volume DECIMAL(20, 4) DEFAULT 0.00,  -- track the average daily trading volume of each asset to assess liquidity risk.
    bid_ask_spread DECIMAL(10, 4) DEFAULT 0.00,  -- Bid-ask spread as a measure of liquidity

    FOREIGN KEY (market_id) REFERENCES market(market_id),
    FOREIGN KEY (product_id) REFERENCES product(product_id),

    UNIQUE (market_id, product_id) -- ensures product is listed only once on a unique market
);
-- identifying the ticker symbol for a unique exchange.
-- name is not used for identification since the same product can be on different exchanges.
CREATE UNIQUE INDEX idx_symbol_market_product ON market_product(symbol, market_id);
CREATE INDEX idx_market_product_avg_daily_volume ON market_product(avg_daily_volume);
CREATE INDEX idx_market_product_bid_ask_spread ON market_product(bid_ask_spread);

CREATE TABLE IF NOT EXISTS product_price_history (
    price_history_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    open_price DECIMAL(20, 4),
    close_price DECIMAL(20, 4),
    high_price DECIMAL(20, 4),
    low_price DECIMAL(20, 4),
    volume DECIMAL(20, 4),
    price_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (product_id) REFERENCES product(product_id)
);
--  enhance querying of price history
CREATE INDEX idx_price_history_date ON product_price_history(price_date);
-- fast lookups for price history for a product over time
CREATE INDEX idx_product_price_history_product_date ON product_price_history(product_id, price_date);

-- portfolio Table
CREATE TABLE IF NOT EXISTS portfolio (
    portfolio_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    portfolio_name VARCHAR(50) NOT NULL,
    total_value DECIMAL(20, 2),
    risk_level ENUM('High', 'Moderate', 'Low') DEFAULT 'Moderate',
    -- leverage data
    using_margin BOOLEAN DEFAULT FALSE, -- to track if portfolio uses borrowed funds(leverage) to increase exposure to certain assets.
    margin_ratio DECIMAL(5, 2) DEFAULT 1.00, -- 1.00 indicates no leverage. The proportion of borrowed funds in the portfolio. Higher leverage ratios imply higher risk.

    max_drawdown DECIMAL(10, 6), -- The max loss observed  from a peak to a trough in the portfolio's value - higher max drawdown suggests higher risk.
    sharpe_ratio DECIMAL(10, 6), -- measure of risk-adjusted return - higher Sharpe ratio indicates a more favorable risk-return profile.

    investment_goal VARCHAR(50), -- "Retirement", "Short-Term Speculation". short-term gains are riskier
    investment_horizon VARCHAR(50), --  "Short-Term", "Long-Term"

    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (account_id) REFERENCES account(account_id)
);
CREATE INDEX idx_portfolio_account_name ON portfolio(account_id, portfolio_name);
CREATE INDEX idx_portfolio_account_id ON portfolio(account_id);
CREATE INDEX idx_portfolio_risk_level ON portfolio(risk_level);
CREATE INDEX idx_portfolio_total_value ON portfolio(total_value);

-- A position refers to the state of ownership of particular securities.
-- For instance, if you own 100 shares of a particular stock, your "position" in that stock is 100 shares.
-- Positions are altered by transactions and indicate your current holdings.
CREATE TABLE IF NOT EXISTS `position` (
    position_id INT PRIMARY KEY AUTO_INCREMENT,
    portfolio_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,

    FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id),
    FOREIGN KEY (product_id) REFERENCES product(product_id)
);

-- only one unique product can exist in a given portfolio(ex: ins=AAPL stock, port=Retirement Portfolio)
CREATE UNIQUE INDEX idx_position_portfolio_id_product_id ON `position`(portfolio_id, product_id);

-- table for different order types
CREATE TABLE IF NOT EXISTS order_type (
    order_type_id INT PRIMARY KEY AUTO_INCREMENT,
    order_type_name VARCHAR(32) NOT NULL, -- 'MARKET', 'LIMIT', 'STOP', 'FILL_OR_KILL', etc.

    UNIQUE(order_type_name)
);

-- An order is a directive to buy or sell a financial product.
-- An order may or may not result in a transaction depending on market conditions.
-- The order could be partially fulfilled across multiple transactions.
CREATE TABLE IF NOT EXISTS `order` (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    order_type_id INT NOT NULL,
    wallet_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    side ENUM('BUY', 'SELL'),
    price DECIMAL(10, 2) NOT NULL,
    placed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    filled_quantity INT NOT NULL,
    remaining_quantity INT NOT NULL,
    version INT DEFAULT 0,

    FOREIGN KEY (order_type_id) REFERENCES order_type(order_type_id),
    FOREIGN KEY (wallet_id) REFERENCES wallet(wallet_id),
    FOREIGN KEY (product_id) REFERENCES product(product_id)
);
CREATE INDEX idx_order_wallet_id ON `order`(wallet_id);
CREATE INDEX idx_order_product_id ON `order`(product_id);
CREATE INDEX idx_order_placed_at ON `order`(placed_at);



-- order_status will keep track of the current status for a unique order.
-- On FILLED status, the system will trigger the tranaction against the order
CREATE TABLE IF NOT EXISTS order_status (
    order_status_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    current_status VARCHAR(32) NOT NULL, -- 'OPEN', 'PARTIALLY_FILLED', 'FILLED', 'CANCELLED'
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY(order_id) REFERENCES `order`(order_id)
);
CREATE INDEX idx_current_status ON order_status(current_status);
CREATE INDEX idx_order_status_order_id ON order_status(order_id); -- additional index to speed up join and lookups by order_id


CREATE TABLE IF NOT EXISTS transaction_type (
    transaction_type_id INT PRIMARY KEY AUTO_INCREMENT,
    transaction_type_name VARCHAR(32) NOT NULL, -- BUY, SELL

    UNIQUE(transaction_type_name)
);
-- A transaction is the record of a completed trade involving the transfer of a
-- financial product from a seller to a buyer.
-- This is an actual event that has taken place and cannot be altered or removed.
CREATE TABLE IF NOT EXISTS `transaction` (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    transaction_type_id INT NOT NULL,
    wallet_id INT,
    portfolio_id INT,
    product_id INT,
    order_id INT,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (transaction_type_id) REFERENCES transaction_type(transaction_type_id),
    FOREIGN KEY (wallet_id) REFERENCES wallet(wallet_id),
    FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id),
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    FOREIGN KEY (order_id) REFERENCES `order`(order_id)
);

-- Indices
CREATE INDEX idx_transaction_type ON `transaction`(transaction_type_id);
CREATE INDEX idx_transaction_wallet_id ON `transaction`(wallet_id);
CREATE INDEX idx_transaction_portfolio_id ON `transaction`(portfolio_id);
CREATE INDEX idx_transaction_product_id ON `transaction`(product_id);
CREATE INDEX idx_transaction_order_id ON `transaction`(order_id);
CREATE INDEX idx_transaction_date ON `transaction`(transaction_date); -- for faster time-based lookups


-- user service (address, role, user)
-- account service (account, wallet, portfolio, position)
-- market service (market, product, product_type, currency, product_price_history)
-- order service (order, order_type, order_status, transaction)