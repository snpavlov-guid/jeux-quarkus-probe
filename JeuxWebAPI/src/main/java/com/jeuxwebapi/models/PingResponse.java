package com.jeuxwebapi.models;

import java.time.OffsetDateTime;

public class PingResponse {
    private String echo;
    private OffsetDateTime timestamp;

    public PingResponse() {
    }

    public PingResponse(String echo, OffsetDateTime timestamp) {
        this.echo = echo;
        this.timestamp = timestamp;
    }

    public String getEcho() {
        return echo;
    }

    public void setEcho(String echo) {
        this.echo = echo;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
