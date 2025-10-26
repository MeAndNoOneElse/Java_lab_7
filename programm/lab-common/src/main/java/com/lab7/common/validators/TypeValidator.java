package com.lab7.common.validators;

import com.lab7.common.models.TicketType;
import com.lab7.common.utility.ExecutionStatus;

import java.io.Serial;
import java.io.Serializable;

/**
 * Валидатор для проверки корректности жанра музыки.
 */
public class TypeValidator extends ArgumentValidator implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * Проверяет корректность аргумента команды.
     *
     * @param arg Аргумент команды.
     * @param name Имя команды.
     * @return Статус выполнения проверки.
     */
    @Override
    public ExecutionStatus validate(String arg, String name) {
        if (arg.isEmpty()) {
            return new ExecutionStatus(false, "У команды должен быть аргумент (type)!\nПример корректного ввода: " + name);
        }
        try {
            TicketType.valueOf(arg);
            return new ExecutionStatus(true, "Аргумент команды введен корректно.");
        } catch (IllegalArgumentException e) {
            return new ExecutionStatus(false, "Некорректное значение поля type!\nСписок возможных значений: " + TicketType.list());
        }
    }
}
