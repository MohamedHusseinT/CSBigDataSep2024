package customconsumer;

public class StockRecord {
    private String symbol;
    private double price;
    private double volume;
    private long timestamp;

    public StockRecord(String symbol, double price, double volume, long timestamp) {
        this.symbol = symbol;
        this.price = price;
        this.volume = volume;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public double getVolume() {
        return volume;
    }

    public long getTimestamp() {
        return timestamp;
    }

	@Override
	public String toString() {
		return "StockRecord [symbol=" + symbol + ", price=" + price
				+ ", volume=" + volume + ", timestamp=" + timestamp + "]";
	}
    
}




