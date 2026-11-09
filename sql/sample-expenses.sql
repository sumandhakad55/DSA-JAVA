-- OPTIONAL: run manually in MySQL Workbench to see the dashboard with demo data.
-- (Not loaded automatically, so it never duplicates.)
USE expense_tracker;
INSERT INTO expenses (amount, spent_at, merchant, category, payment_method, note) VALUES
    (234.00,  NOW(),                          'Blinkit',   'Groceries', 'PhonePe',    'Milk and bread'),
    (1299.00, NOW() - INTERVAL 1 DAY,         'Flipkart',  'Shopping',  'Google Pay', 'Phone cover'),
    (456.50,  NOW() - INTERVAL 2 DAY,         'Instamart', 'Groceries', 'PhonePe',    'Weekly vegetables'),
    (349.00,  NOW() - INTERVAL 3 DAY,         'Zomato',    'Food',      'Google Pay', 'Dinner'),
    (2199.00, NOW() - INTERVAL 5 DAY,         'Amazon',    'Shopping',  'Card',       'Headphones'),
    (180.00,  NOW() - INTERVAL 40 DAY,        'Blinkit',   'Groceries', 'PhonePe',    NULL);
INSERT INTO income (amount, received_on, source) VALUES (5000.00, CURDATE() - INTERVAL 4 DAY, 'Pocket money');
