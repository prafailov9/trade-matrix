-- order types
insert into order_type(order_type_name) values ('MARKET');
insert into order_type(order_type_name) values ('LIMIT');
insert into order_type(order_type_name) values ('STOP');
insert into order_type(order_type_name) values ('FILL_OR_KILL');

-- orders
-- test scenario
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 1, 1, 5, 'BUY', 174, '2024-11-21 22:22:01', 0, 5);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 1, 2, 3, 'BUY', 173, '2024-11-21 22:22:01', 0, 3);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 1, 3, 2, 'BUY', 174.45, '2024-11-21 22:22:01', 0, 2);

insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 1, 20, 2, 'SELL', 177.3, '2024-11-21 22:22:01', 0, 2);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 1, 19, 1, 'SELL', 178, '2024-11-21 22:22:01', 0, 1);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 1, 18, 5, 'SELL', 181, '2024-11-21 22:22:01', 0, 5);

-- random data
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 2, 2, 3, 'BUY', 34, '2024-11-21 22:22:01', 0, 3);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 3, 3, 11, 'BUY', 12, '2024-11-21 22:22:01', 0, 11);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 4, 4, 28, 'BUY', 9, '2024-11-21 22:22:01', 0, 28);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 5, 5, 7, 'BUY', 39, '2024-11-21 22:22:01', 0, 7);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 6, 6, 14, 'BUY', 200, '2024-11-21 22:22:01', 0, 14);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 7, 7, 40, 'BUY', 120, '2024-11-21 22:22:01', 0, 40);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 8, 8, 9, 'BUY', 77, '2024-11-21 22:22:01', 0, 9);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 9, 9, 22, 'BUY', 15, '2024-11-21 22:22:01', 0, 22);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 10, 10, 40, 'BUY', 350, '2024-11-21 22:22:01', 0, 3);


insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 2, 2, 3, 'SELL', 34, '2024-11-21 22:22:01', 0, 3);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 3, 3, 11, 'SELL', 12, '2024-11-21 22:22:01', 0, 11);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 4, 4, 28, 'SELL', 9, '2024-11-21 22:22:01', 0, 28);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 5, 5, 7, 'SELL', 39, '2024-11-21 22:22:01', 0, 7);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 6, 6, 14, 'SELL', 200, '2024-11-21 22:22:01', 0, 14);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 7, 7, 40, 'SELL', 120, '2024-11-21 22:22:01', 0, 40);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 8, 8, 9, 'SELL', 77, '2024-11-21 22:22:01', 0, 9);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 9, 9, 22, 'SELL', 15, '2024-11-21 22:22:01', 0, 22);
insert into `order`(order_type_id, market_product_id, wallet_id, quantity, side, price, placed_at, filled_quantity, remaining_quantity)
values (1, 10, 10, 40, 'SELL', 350, '2024-11-21 22:22:01', 0, 3);

-- order status: 'OPEN', 'PARTIALLY_FILLED', 'FILLED', 'CANCELLED'
insert into order_status(order_id, current_status) values (1, 'OPEN');
insert into order_status(order_id, current_status) values (2, 'OPEN');
insert into order_status(order_id, current_status) values (3, 'OPEN');
insert into order_status(order_id, current_status) values (4, 'OPEN');
insert into order_status(order_id, current_status) values (5, 'OPEN');
insert into order_status(order_id, current_status) values (6, 'OPEN');
insert into order_status(order_id, current_status) values (7, 'OPEN');
insert into order_status(order_id, current_status) values (8, 'OPEN');
insert into order_status(order_id, current_status) values (9, 'OPEN');
insert into order_status(order_id, current_status) values (10, 'OPEN');
insert into order_status(order_id, current_status) values (11, 'OPEN');
insert into order_status(order_id, current_status) values (12, 'OPEN');
insert into order_status(order_id, current_status) values (13, 'OPEN');
insert into order_status(order_id, current_status) values (14, 'OPEN');
insert into order_status(order_id, current_status) values (15, 'OPEN');
insert into order_status(order_id, current_status) values (16, 'OPEN');
insert into order_status(order_id, current_status) values (17, 'OPEN');
insert into order_status(order_id, current_status) values (18, 'OPEN');
insert into order_status(order_id, current_status) values (19, 'OPEN');
insert into order_status(order_id, current_status) values (20, 'OPEN');
insert into order_status(order_id, current_status) values (21, 'OPEN');
insert into order_status(order_id, current_status) values (22, 'OPEN');
insert into order_status(order_id, current_status) values (23, 'OPEN');
insert into order_status(order_id, current_status) values (24, 'OPEN');