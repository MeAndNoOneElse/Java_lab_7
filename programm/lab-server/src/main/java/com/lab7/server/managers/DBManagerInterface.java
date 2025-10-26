package com.lab7.server.managers;

import com.lab7.common.models.Ticket;
import com.lab7.common.models.TicketType;
import com.lab7.common.utility.ExecutionStatus;
import com.lab7.common.utility.Pair;
import com.lab7.common.utility.PermissionType;
import com.lab7.server.utility.Transactional;

import java.sql.SQLException;
import java.util.Stack;

public interface DBManagerInterface {
    ExecutionStatus addUser(Pair<String, String> user);
    ExecutionStatus checkPassword(Pair<String, String> user);
    ExecutionStatus showUserList(Pair<String, String> user);
    ExecutionStatus updateUserPermissions(String user, PermissionType permission);
    ExecutionStatus checkUserPermission(Pair<String, String> user);
    ExecutionStatus clear(Pair<String, String> user);
    ExecutionStatus clearAll();
    ExecutionStatus removeById(Long id, Pair<String, String> user);
    ExecutionStatus removeAllByGenre(TicketType type, Pair<String, String> user);
    ExecutionStatus removeAllByGenre(TicketType type);

    @Transactional
    ExecutionStatus addTicket(Ticket ticket, Pair<String, String> user) throws SQLException;

    @Transactional
    ExecutionStatus updateTicket(Ticket ticket, Pair<String, String> user) throws SQLException;

    ExecutionStatus loadCollection(Stack<Ticket> collection);

}