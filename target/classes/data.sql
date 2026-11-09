-- Safe to run on every start (INSERT IGNORE skips rows that already exist)
INSERT IGNORE INTO settings (setting_key, setting_value) VALUES
    ('starting_balance', '10000'),
    ('monthly_budget',   '15000');

INSERT IGNORE INTO merchant_rules (keyword, merchant, category) VALUES
    ('blinkit',   'Blinkit',   'Groceries'),
    ('instamart', 'Instamart', 'Groceries'),
    ('zepto',     'Zepto',     'Groceries'),
    ('flipkart',  'Flipkart',  'Shopping'),
    ('amazon',    'Amazon',    'Shopping'),
    ('myntra',    'Myntra',    'Shopping'),
    ('zomato',    'Zomato',    'Food'),
    ('swiggy',    'Swiggy',    'Food'),
    ('uber',      'Uber',      'Travel'),
    ('irctc',     'IRCTC',     'Travel'),
    ('airtel',    'Airtel',    'Bills');
