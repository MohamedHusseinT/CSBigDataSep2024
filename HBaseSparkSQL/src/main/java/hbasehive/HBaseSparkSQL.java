package hbasehive;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

public class HBaseSparkSQL {

	public static void main(String[] args) {
		// Set up Spark
		SparkSession spark = SparkSession.builder().appName("ReadHBase")
				.config("spark.master", "local[*]") // Adjust this if running in
													// // a cluster
				.getOrCreate();

		// Configure HBase
		org.apache.hadoop.conf.Configuration hbaseConf = HBaseConfiguration
				.create();
		hbaseConf.set("hbase.zookeeper.quorum", "localhost:2181");

		List<Row> data = new ArrayList<>();
		StructType schema = new StructType().add("key", DataTypes.StringType)
				.add("price", DataTypes.StringType)
				.add("volume", DataTypes.StringType)
				.add("timestamp",DataTypes.StringType);

		try (Connection connection = ConnectionFactory
				.createConnection(hbaseConf);
				Table table = connection.getTable(TableName
						.valueOf("stock_statistics"))) {

			for (Result result : table
					.getScanner(new org.apache.hadoop.hbase.client.Scan())) {
				String key = Bytes.toString(result.getRow());
				String col1 = Bytes.toString(result.getValue(
						Bytes.toBytes("stats"), Bytes.toBytes("totalPrice")));
				String col2 = Bytes.toString(result.getValue(
						Bytes.toBytes("stats"), Bytes.toBytes("totalVolume")));
				String col3 = Bytes.toString(result.getValue(
						Bytes.toBytes("stats"), Bytes.toBytes("timeStamp")));
				data.add(RowFactory.create(key, col1, col2,col3));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Create DataFrame from the list of rows
		Dataset<Row> df = spark.createDataFrame(data, schema);
		df.createOrReplaceTempView("hbase_table");

		// Perform SQL queries
		Dataset<Row> resultDf = spark.sql("SELECT * FROM hbase_table");
		resultDf.show();

		spark.stop();
	}
}
