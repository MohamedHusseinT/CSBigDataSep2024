package kafka;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaFinnhubProducer {

	private static final String FINNHUB_API_KEY = ""; // get it from the website finnhub
	private static final String FINNHUB_WS_URL = "wss://ws.finnhub.io?token="
			+ FINNHUB_API_KEY;

	private static final String KAFKA_TOPIC = "stock_prices";
	private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";

	private static KafkaProducer<String, String> producer;
	private static ObjectMapper objectMapper = new ObjectMapper();

	public static void main(String[] args) throws URISyntaxException {

		// Configure Kafka Producer
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
				KAFKA_BOOTSTRAP_SERVERS);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaFinnhubProducer");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringSerializer");

		producer = new KafkaProducer<>(props);

		// Create WebSocket Client
		WebSocketClient webSocketClient = new WebSocketClient(new URI(
				FINNHUB_WS_URL)) {

			@Override
			public void onOpen(ServerHandshake handshakedata) {
				System.out.println("### WebSocket Opened ###");
				// Subscribe to symbols
				subscribeToSymbol("AAPL");
				subscribeToSymbol("AMZN");
				subscribeToSymbol("BINANCE:BTCUSDT");
				subscribeToSymbol("IC MARKETS:1");
			}

			@Override
			public void onMessage(String message) {
				System.out.println("Received message: " + message);
				// Send message to Kafka
				producer.send(new ProducerRecord<>(KAFKA_TOPIC, null, message));
				System.out.println("Sent to Kafka: " + message);
			}

			@Override
			public void onClose(int code, String reason, boolean remote) {
				System.out
						.println("### WebSocket Closed ### Reason: " + reason);
				producer.close();
			}

			@Override
			public void onError(Exception ex) {
				System.err.println("WebSocket Error: " + ex.getMessage());
				ex.printStackTrace();
			}

			private void subscribeToSymbol(String symbol) {
				try {
					String subscribeMessage = objectMapper
							.writeValueAsString(new SubscriptionMessage(
									"subscribe", symbol));
					send(subscribeMessage);
					System.out.println("Subscribed to " + symbol);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		// Connect to WebSocket
		webSocketClient.connect();

		// Keep the application running
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				webSocketClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
	}

	// Inner class for subscription messages
	static class SubscriptionMessage {
		public String type;
		public String symbol;

		public SubscriptionMessage(String type, String symbol) {
			this.type = type;
			this.symbol = symbol;
		}
	}
}