package com.lab7.common.utility;

import com.lab7.common.models.Ticket;
import java.io.Serial;
import java.io.Serializable;

public class Request implements Serializable {
    @Serial
    private static final long serialVersionUID = 11L;
    private final String string;
    private Ticket ticket = null;
    private final Pair<String, String> user;

    public Request(String string, Pair<String, String> user) {
        this.string = string;
        this.user = user;
    }

    public Request(String string, Ticket ticket, Pair<String, String> user) {
        this.string = string;
        this.ticket = ticket;
        this.user = user;
    }

    public Pair<String, String> getUser() { return user; }

    public String[] getCommand() {
        String[] inputCommand = (string.trim() + " ").split(" ", 2);
        inputCommand[1] = inputCommand[1].trim();
        return inputCommand;
    }

    public Ticket getTicket() {
        return ticket;
    }

    @Override
    public String toString() {
        return "Request{" +
                "string='" + string + '\'' +
                ", ticket=" + ticket +
                ", user=" + user +
                '}';
    }
}