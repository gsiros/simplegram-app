package gr.aueb.simplegram.common;

import java.net.InetAddress;

/**
 * This class is used to contain all necessary information
 * regarding a broker.
 */
public class BrokerConnection {
    private final InetAddress broker_addr;
    private int brokerID;
    private boolean isActive;
    private long lastTimeActive;

    public BrokerConnection(InetAddress addr){
        this.broker_addr = addr;
        this.isActive = false;
        this.lastTimeActive = -1;
    }

    public BrokerConnection(int brokerID, InetAddress addr){
        this.brokerID = brokerID;
        this.broker_addr = addr;
        this.isActive = false;
        this.lastTimeActive = -1;
    }

    public int getBrokerID() {
        return brokerID;
    }

    public InetAddress getBrokerAddress() {
        return this.broker_addr;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void setActive() {
        this.isActive = true;
        this.lastTimeActive = System.currentTimeMillis();
    }

    public void setDead() {
        this.isActive = false;
    }

    public long getLastTimeActive() {
        return this.lastTimeActive;
    }

}

