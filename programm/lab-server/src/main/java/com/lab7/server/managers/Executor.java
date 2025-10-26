package com.lab7.server.managers;

import com.lab7.common.models.Ticket;
import com.lab7.common.utility.Pair;
import com.lab7.server.Server;
import com.lab7.server.utility.Command;
import com.lab7.common.utility.ExecutionStatus;
import com.lab7.common.validators.ArgumentValidator;
import com.lab7.server.utility.AskingCommand;

/**
 * Класс, выполняющий команды и скрипты.
 */
public class Executor implements ExecutorInterface {
    private final CommandManager commandManager;

    /**
     * Конструктор для создания объекта Executer.
     */
    public Executor(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    private ExecutionStatus validateCommand(String[] userCommand) {
        try {
            Command<?> command = commandManager.getCommand(userCommand[0]);
            if (command == null) {
                return new ExecutionStatus(false, "Команда '" + userCommand[0] + "' не найдена! Для показа списка команд введите 'help'.");
            } else {
                ArgumentValidator argumentValidator = command.getArgumentValidator();
                return argumentValidator.validate(userCommand[1].trim(), command.getName());
            }
        } catch (NullPointerException e) {
            return new ExecutionStatus(false, "Введено недостаточно аргументов для выполнения последней команды!");
        }
    }

    /**
     * Выполняет команду.
     * @param userCommand массив строк, представляющий команду
     * @param ticket объект MusicBand, если команда требует его
     * @return статус выполнения команды
     */
    @Override
    public ExecutionStatus runCommand(String[] userCommand, Ticket ticket, Pair<String, String> user) {
        ExecutionStatus validateStatus = validateCommand(userCommand);
        if (validateStatus.isSuccess()) {
            var command = commandManager.getCommand(userCommand[0]);
            Server.logger.info("Command '" + userCommand[0] + "' is running...");
            if (AskingCommand.class.isAssignableFrom(command.getClass())) {
                return ((AskingCommand<?>) command).run(userCommand[1], ticket, user);
            } else {
                return command.run(userCommand[1], user);
            }
        } else {
            return new ExecutionStatus(false, validateStatus.getMessage());
        }
    }
}