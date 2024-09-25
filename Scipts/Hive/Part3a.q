Drop table if exists hive_stock_table ;​

create external table hive_stock_table ​
    (key string,
	totalePrice string, 
	totalVolume string,​
	timeStamp string) ​
stored by 'org.apache.hadoop.hive.hbase.HBaseStorageHandler' ​
with serdeproperties ("hbase.columns.mapping"=":key,stats:totalPrice,stats:totalVolume,stats:timeStamp") tblproperties ​
("hbase.table.name"="stock_statistics", "hbase.columns.mapping"=":key,stats:totalPrice,stats:totalVolume,stats:timeStamp");
