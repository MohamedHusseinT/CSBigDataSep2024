package customconsumer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kafka.serializer.StringDecoder;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import scala.Tuple2;
import scala.Tuple3;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StockDataConsumer {

	public static void main(String[] args) throws Exception {

		// Configure Spark
		SparkConf conf = new SparkConf().setAppName("StockDataConsumer")
				.setMaster("local[*]"); // Use local mode for testing

		JavaStreamingContext jssc = new JavaStreamingContext(conf,
				Durations.seconds(10));

		// Kafka parameters
		Map<String, String> kafkaParams = new HashMap<>();
		kafkaParams.put("metadata.broker.list", "localhost:9092");
		kafkaParams.put("group.id", "stock-data-group");
		//kafkaParams.put("auto.offset.reset", "smallest");
		kafkaParams.put("auto.offset.reset", "largest");


		// List of topics to subscribe to
		Set<String> topics = new HashSet<>(Arrays.asList("stock_prices"));

		// Create direct Kafka stream with brokers and topics
		JavaPairInputDStream<String, String> stream = KafkaUtils
				.createDirectStream(jssc, String.class, String.class,
						StringDecoder.class, StringDecoder.class, kafkaParams,
						topics);

		stream.foreachRDD(rdd -> {
			if (!rdd.isEmpty()) {
				// Parse JSON messages and extract required fields
				JavaRDD<StockRecord> stockData = rdd.flatMap(record -> {
					System.out.println("record : " + record);
					List<StockRecord> records = new ArrayList<>();
					try {
						// Initialize Jackson ObjectMapper inside the flatMap
						// function
						ObjectMapper objectMapper = new ObjectMapper();

						JsonNode jsonNode = objectMapper.readTree(record._2());
						String type = jsonNode.get("type").asText();

						if ("trade".equals(type)) {
							JsonNode dataArray = jsonNode.get("data");
							for (JsonNode tradeNode : dataArray) {
								String symbol = tradeNode.get("s").asText();
								double price = tradeNode.get("p").asDouble();
								double volume = tradeNode.get("v").asDouble();
								long timestamp = tradeNode.get("t").asLong();

								records.add(new StockRecord(symbol, price,
										volume, timestamp));
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return records.iterator();
				});

				// Convert to PairRDD for aggregation
				JavaPairRDD<String, Tuple2<Double, Double>> pairRDD = stockData
						.mapToPair(stock -> new Tuple2<>(stock.getSymbol(),
								new Tuple2<>(stock.getPrice(), stock
										.getVolume())));

				// Compute statistics: total price and total volume per symbol
//				JavaPairRDD<String, Tuple2<Double, Double>> statsRDD = pairRDD
//						.aggregateByKey(
//								new Tuple2<>(0.0, 0.0),
//								(acc, value) -> new Tuple2<>(acc._1()  + value._1(), acc._2() + value._2()), 
//								(acc1, acc2) -> new Tuple2<>(acc1._1() + acc2._1(), acc1._2() + acc2._2()));
				
				// Compute averages: average price and average volume per symbol
				JavaPairRDD<String, Tuple3<Double, Double, Integer>> sumCountRDD = pairRDD
				    .aggregateByKey(
				        new Tuple3<>(0.0, 0.0, 0), // Initial accumulator: (sumPrice, sumVolume, count)
				        (acc, value) -> new Tuple3<>(
				            acc._1() + value._1(), // Sum prices
				            acc._2() + value._2(), // Sum volumes
				            acc._3() + 1            // Increment count
				        ),
				        (acc1, acc2) -> new Tuple3<>(
				            acc1._1() + acc2._1(), // Combine sumPrice
				            acc1._2() + acc2._2(), // Combine sumVolume
				            acc1._3() + acc2._3()  // Combine count
				        )
				    );

				JavaPairRDD<String, Tuple2<Double, Double>> avgRDD = sumCountRDD
				    .mapValues(tuple -> {
				        // Prevent division by zero
				        if (tuple._3() == 0) {
				            return new Tuple2<>(0.0, 0.0);
				        }
				        double avgPrice = tuple._1() / tuple._3();   // Average Price
				        double avgVolume = tuple._2() / tuple._3();  // Average Volume
				        return new Tuple2<>(avgPrice, avgVolume);
				    });
				

				Configuration config = HBaseConfiguration.create();
				try (Connection connection = ConnectionFactory
						.createConnection(config);
						Admin admin = connection.getAdmin()) {

					HTableDescriptor table = new HTableDescriptor(TableName
							.valueOf("stock_statistics"));
					table.addFamily(new HColumnDescriptor("stats"));
					System.out.print("Check for Table ... ");

					if (!admin.tableExists(table.getTableName())) {
						admin.createTable(table);
						
					}
//					else {
//						System.out.print("stock_statistics" + " exist");
//						admin.disableTable(table.getTableName());
//						admin.deleteTable(table.getTableName());
//					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				// Save the results to HBase
				avgRDD.foreachPartition(partitionOfRecords -> {
					// HBase configuration and connection setup inside the
					// partition
					Configuration hbaseConf = HBaseConfiguration.create();
					hbaseConf.set("hbase.zookeeper.quorum", "localhost"); // Adjust
																			// if
																			// necessary
					hbaseConf
							.set("hbase.zookeeper.property.clientPort", "2181");

					try (Connection connection = ConnectionFactory
							.createConnection(hbaseConf)) {

						Table stockStatisticsTable = connection
								.getTable(TableName.valueOf("stock_statistics"));

						while (partitionOfRecords.hasNext()) {
							Tuple2<String, Tuple2<Double, Double>> record = partitionOfRecords
									.next();
							String symbol = record._1();
							double totalPrice = record._2()._1();
							double totalVolume = record._2()._2();
							LocalDateTime timeStamp = LocalDateTime.now();

							// Create a new row in HBase with the symbol as row
							// key
							Put put = new Put(Bytes.toBytes(symbol + "|" + timeStamp));
							put.addColumn(Bytes.toBytes("stats"),
									Bytes.toBytes("totalPrice"),
									Bytes.toBytes(String.valueOf(totalPrice)));
							put.addColumn(Bytes.toBytes("stats"),
									Bytes.toBytes("totalVolume"),
									Bytes.toBytes(String.valueOf(totalVolume)));
							put.addColumn(Bytes.toBytes("stats"),
									Bytes.toBytes("timeStamp"),
									Bytes.toBytes(String.valueOf(timeStamp)));

							// Write to HBase
							stockStatisticsTable.put(put);
						}
						stockStatisticsTable.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
		});

		// Start the streaming context and await termination
		jssc.start();
		jssc.awaitTermination();
	}
}