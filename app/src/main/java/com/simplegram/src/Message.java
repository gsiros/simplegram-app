package com.simplegram.src;

import java.io.Serializable;

/**
 * A simple 'Message' class that contains all the required
 * information for a text message in a topic.
 */
public class Message extends Value implements Serializable {
    private String msg;

    public Message(String sentFrom, String msg) {
        super(sentFrom);
        this.msg = msg;

    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return this.msg;
    }
}
