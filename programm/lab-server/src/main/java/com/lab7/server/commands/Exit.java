package com.lab7.server.commands;

import com.lab7.common.utility.Pair;
import com.lab7.server.utility.Command;
import com.lab7.server.utility.CommandNames;
import com.lab7.common.validators.EmptyValidator;
import com.lab7.common.utility.ExecutionStatus;

/**
 * Класс команды для завершения программы (без сохранения в файл).
 */
public class Exit extends Command<EmptyValidator> {

    /**
     * Конструктор команды exit.
     */
    public Exit() {
        super(CommandNames.EXIT.getName(), CommandNames.EXIT.getDescription(), new EmptyValidator());
    }

    /**
     * Выполняет команду завершения программы.
     * @param argument Аргумент команды (не используется).
     * @return Статус выполнения команды.
     */
    @Override
    protected ExecutionStatus runInternal(String argument, Pair<String, String> user) {
        return new ExecutionStatus(true, "Данную команду нельзя отправить на сервер");
    }
}