create view vm_stock_statistics2 AS
select rowkey, cast(totalPrice as double), cast(totalVolume as double), timeStamp from hbase_stock_statistics
where rowkey <> 'BINANCE:BTCUSDT-1727042569122'
and rowkey <> 'BINANCE:BTCUSDT|2024-09-22T15:03:18.171'
and rowkey <> 'BINANCE:BTCUSDT|2024-09-22T15:05:51'
and rowkey <>  'BINANCE:BTCUSDT|2024-09-22T15:05:51'
and rowkey <> 'BINANCE:BTCUSDT|2024-09-22T15:05:51.504'
and `timestamp` > '2024-09-22T15:25:00.211'
;

