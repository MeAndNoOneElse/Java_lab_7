package com.lab7.server.commands.askingCommands;

import com.lab7.common.utility.Pair;
import com.lab7.server.utility.AskingCommand;
import com.lab7.server.utility.CommandNames;
import com.lab7.common.validators.EmptyValidator;
import com.lab7.common.models.Ticket;
import com.lab7.common.utility.ExecutionStatus;

import java.util.Comparator;
import java.util.Stack;

/**
 * Класс команды для добавления нового элемента в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции.
 */
public class AddIfMin extends AskingCommand<EmptyValidator> {
    /**
     * Конструктор команды addIfMin.
     */
    public AddIfMin() {
        super(CommandNames.ADD_IF_MIN.getName() + " {element}", CommandNames.ADD_IF_MIN.getDescription(), new EmptyValidator());
    }

    /**
     * Выполняет команду добавления нового элемента в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции.
     *
     * @param ticket Элемент коллекции.
     * @return Статус выполнения команды.
     */
    @Override
    protected ExecutionStatus runInternal(Ticket ticket, Pair<String, String> user) {
        if (collectionManager.getCollection().isEmpty()) {
            return collectionManager.add(ticket, user);
        }
        Stack<Ticket> bufCollection = collectionManager.getCollection();
        bufCollection.sort(Comparator.naturalOrder());
        if (ticket.compareTo(bufCollection.firstElement()) < 0) {
            return collectionManager.add(ticket, user);
        } else {
            return new ExecutionStatus(true, "Элемент не является наименьшим в коллекции!");
        }
    }
}