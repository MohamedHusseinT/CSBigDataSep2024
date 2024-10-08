# BigData Course Sep-2024

**AWS_EMR:** AWS EMR + S3

**HBaseSparkSQL:** Spark-sql + HBase

**KafkaConsumer:**  Kafka Consumer + Spark Streaming API + HBase + 

**KafkaProducer:** Integrate with Finnhub API + listen topic + add stock object to topic

**Tableau:** Visualization using Tableau for data from Hive using view

**Presentation url:**
https://mum0-my.sharepoint.com/:p:/g/personal/mmostafa_miu_edu/EY2ie682y4lBtGOVIPl2T2MBB6uJ8tC-NBITsC4uJtfZEA?email=mmukadam%40miu.edu&e=ZXSCZ1



**Steps:**
### 1- Kafka
    **Download Kafka**: 

        - Go to the [Apache Kafka downloads page](https://kafka.apache.org/downloads) and copy the download link for the latest binary. 

 

    **Extract Kafka**: 

        - Download and extract Kafka in your home directory or a preferred location: 

  

     on bash (Terminal)

     wget https://archive.apache.org/dist/kafka/3.8.0/kafka_2.12-3.8.0.tgz --no-check-certificate 

     tar -xzf kafka_2.12-3.8.0.tgz 

     cd kafka_2.12-3.8.0 
### 2 - Zookeeper: if needed (skip as it should be already running)

   **Start Zookeeper**: 

     - Kafka requires Zookeeper to run. Start Zookeeper using the provided script: 

        on bash (Terminal)

        bin/zookeeper-server-start.sh config/zookeeper.properties  



### 3 - Create Topic

 

  #### Create a Topic and Test 

  

    **Create a Topic**: 

     - In a new terminal, create a Kafka topic: 

  

     on bash (Terminal)

       bin/kafka-topics.sh --create --topic stock_prices --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 




### 4 - Start Kafka service
     bin/kafka-server-start.sh config/server.properties



### 5 - Start Producer project as jar
     java -jar target/KafkaFinnhubProducer-1.0-SNAPSHOT.jar

### 6 - Submit Consumer project as jar or run from the eclipse
	
 	Sample of received message in Consumer which already coming from used API (API documentation: https://finnhub.io/docs/api/websocket-trades)
 	{"data":[{"c":["1","12"],"p":191.9788,"s":"AMZN","t":1727107422666,"v":1},{"c":["1","12"],"p":191.9788,"s":"AMZN","t":1727107422666,"v":1},{"c":["1","12"],"p":191.9788,"s":"AMZN","t":1727107422666,"v":1},{"c":["1","12"],"p":191.9788,"s":"AMZN","t":1727107422666,"v":1}],"type":"trade"}

	Sample of database stored data:
 	<img width="1492" alt="image" src="https://github.com/user-attachments/assets/da24e925-e8d3-4164-91ad-f0a89320abee">

	Tables
	<img width="375" alt="image" src="https://github.com/user-attachments/assets/d52094b4-2016-4405-9f24-b34c520f8bea">


### 7 - Create hive table 

      Drop table if exists hive_stock_table ;​

		create external table hive_stock_table ​
		    (key string,
			totalePrice string, 
			totalVolume string,​
			timeStamp string) ​
		stored by 'org.apache.hadoop.hive.hbase.HBaseStorageHandler' ​
		with serdeproperties ("hbase.columns.mapping"=":key,stats:totalPrice,stats:totalVolume,stats:timeStamp") tblproperties ​
		("hbase.table.name"="stock_statistics", "hbase.columns.mapping"=":key,stats:totalPrice,stats:totalVolume,stats:timeStamp");


### 8 - Create hive view is created in Hive foor Tableau

      create view vm_stock_statistics2 AS
        select rowkey, cast(totalPrice as double), cast(totalVolume as double), timeStamp from hbase_stock_statistics ;



### 9 - Refresh in Tableau project based on the connected IP (VM IP) to get latest view data using project "TableauBin.twb"
      <img width="503" alt="Edit Connection" src="https://github.com/user-attachments/assets/6334a789-5c2a-41bc-abe8-08bf8b8c3b4a">
      ![Edit connection image](/blob/main/Resources/Edit-Connection.png)
      ![Edit connection image](./blob/main/Resources/Edit-Connection.png)
      ![Edit connection image](./Resources/Edit-Connection.png)
      ![Edit connection image](Resources/Edit-Connection.png)

      
     
### 10 - Check results in Tableau






### Additional Configuration 

- **Edit Config Files**: 

  - You might need to edit `config/server.properties` and `config/zookeeper.properties` to adjust settings like `broker.id`, `log.dirs`, and `zookeeper.connect`. 

  














     
