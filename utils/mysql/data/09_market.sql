-- markets
insert into market (currency_id, market_name, market_code, market_cap, country, timezone) values (143, 'New York Stock Exchange', 'NYSE', 25000000000.00, 'United States of America', 'EST');
insert into market (currency_id, market_name, market_code, market_cap, country, timezone) values (143, 'NASDAQ Stock Market', 'NASDAQ', 41990000000.00, 'United States of America', 'EST');
insert into market (currency_id, market_name, market_code, market_cap, country, timezone) values (25, 'Toronto Stock Exchange', 'TSX', 4158000000.00, 'Canada', 'EST');
insert into market (currency_id, market_name, market_code, market_cap, country, timezone) values (96, 'Bolsa Mexicana de Valores (Mexican Stock Exchange)', 'BMV', 17620000000.00, 'Mexico', 'CST');
insert into market (currency_id, market_name, market_code, market_cap, country, timezone) values (45, 'London Stock Exchange', 'LSE', 15000000000.00, 'United Kingdom', 'GMT');
insert into market (currency_id, market_name, market_code, market_cap, country, timezone) values (42, 'Euronext Stock Exchange', 'Euronext', 6600000000000.00, 'West Europe Countries', 'CEST'); -- GMT+2
insert into market (currency_id, market_name, market_code, market_cap, country, timezone) values (42, 'Deutsche Börse (Frankfurt Stock Exchange)', 'FWB', 1970000000000.00, 'Germany', 'CET'); -- GMT+1
insert into market (currency_id, market_name, market_code, market_cap, country, timezone) values (67, 'Tokyo Stock Exchange', 'TSE', 954795998176264.60, 'Japan', 'JST'); -- GMT+9
insert into market (currency_id, market_name, market_code, market_cap, country, timezone) values (73, 'Korea Exchange (KRX)', 'KRX', 3462810000000000.50, 'South Korea', 'KST'); -- GMT+9

-- trading hours
INSERT INTO trading_hours (market_id, open_time, close_time, timezone)
VALUES (1, '09:30:00', '16:00:00', 'EST');

INSERT INTO trading_hours (market_id, open_time, close_time, timezone)
VALUES (2, '08:00:00', '16:30:00', 'GMT');
