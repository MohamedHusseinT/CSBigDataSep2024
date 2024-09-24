package customconsumer;

import java.io.Serializable;


public class AggregatedStats implements Serializable{
    public double totalPrice;
    public double totalVolume;
    public long latestTimestamp;
    public double highPrice;
    public double lowPrice;
    public double openPrice;
    public double closePrice;
    public long tradeCount;

    public AggregatedStats() {
        this.totalPrice = 0.0;
        this.totalVolume = 0.0;
        this.latestTimestamp = 0L;
        this.highPrice = Double.MIN_VALUE;
        this.lowPrice = Double.MAX_VALUE;
        this.openPrice = 0.0;
        this.closePrice = 0.0;
        this.tradeCount = 0L;
    }
    
    
    @Override
    public String toString() {
        return "AggregatedStats [totalPrice=" + totalPrice + ", totalVolume=" + totalVolume 
            + ", latestTimestamp=" + latestTimestamp + ", highPrice=" + highPrice 
            + ", lowPrice=" + lowPrice + ", openPrice=" + openPrice 
            + ", closePrice=" + closePrice + ", tradeCount=" + tradeCount + "]";
    }
}
