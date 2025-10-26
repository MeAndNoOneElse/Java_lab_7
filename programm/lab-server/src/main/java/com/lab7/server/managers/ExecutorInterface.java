package com.lab7.server.managers;

import com.lab7.common.models.Ticket;
import com.lab7.common.utility.ExecutionStatus;
import com.lab7.common.utility.Pair;

public interface ExecutorInterface {
    ExecutionStatus runCommand(String[] userCommand, Ticket ticket, Pair<String, String> user);
}