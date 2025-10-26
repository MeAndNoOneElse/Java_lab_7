package com.lab7.client.managers;

import com.lab7.client.Client;
import com.lab7.client.utility.FileConsole;
import com.lab7.client.utility.Console;
import com.lab7.common.models.*;
import com.lab7.common.models.Event;

import java.time.LocalDateTime;

/**
 * Класс, запрашивающий у пользователя данные для создания объектов.
 */
public class Asker {
    /**
     * Исключение, выбрасываемое для прерывания ввода.
     */
    public static class Breaker extends Exception {
    }

    /**
     * Исключение, выбрасываемое при некорректном вводе.
     */
    public static class IllegalInputException extends IllegalArgumentException {
        /**
         * Конструктор исключения с сообщением.
         *
         * @param message Сообщение об ошибке.
         */
        public IllegalInputException(String message) {
            super(message);
        }
    }

    /**
     * Запрашивает у пользователя данные для создания объекта MusicBand.
     *
     * @param console Консоль для ввода/вывода.
     * @return Объект MusicBand с введенными данными.
     * @throws Breaker               Исключение, выбрасываемое при вводе команды "exit".
     * @throws IllegalInputException Исключение, выбрасываемое при некорректном вводе.
     */
    public static Ticket askBand(Console console) throws Breaker, IllegalInputException {
        TicketBuilder builder = new TicketBuilder();
        String name;
        do {
            console.println("Введите значение поля name:");
            name = console.readln();
            if (name.equals("exit")) {
                throw new Breaker();
            }
        } while (name.isEmpty());
        builder.setName(name);

        builder.setCoordinates(askCoordinates(console));

        Long price;
        do {
            console.println("Введите значение поля price:");
            try {
                String input = console.readln();
                if (input.equals("exit")) {
                    throw new Breaker();
                } else {
                    price = Long.valueOf(input);
                }
            } catch (NumberFormatException e) {
                price = -1L;
            }
            if (price <= 0) {
                if (console instanceof FileConsole) {
                    throw new IllegalInputException("Некорректное значение поля price!\nЗначение поля должно быть больше 0");
                }
                console.printError("Некорректное значение поля price!\nЗначение поля должно быть больше 0");
            }
        } while (price <= 0);
        builder.setPrice(price);


        String description;
        do {
            console.println("Введите значение поля description:");
            description = console.readln();
            if (description.equals("exit")) {
                throw new Breaker();
            }
        } while (description.isEmpty());
        builder.setDescription(description);

        TicketType genre = null;
        do {
            console.println("Введите значение поля type:");
            console.println("Список возможных значений: " + TicketType.list());
            String input = console.readln();
            if (input.equals("exit")) {
                throw new Breaker();
            } else {
                try {
                    genre = TicketType.valueOf(input);
                } catch (IllegalArgumentException e) {
                    if (console instanceof FileConsole) {
                        throw new IllegalArgumentException("Некорректное значение поля type!");
                    }
                    console.printError("Некорректное значение поля type!");
                }
            }
        } while (genre == null);
        builder.setType(genre);
        builder.setEvent(askStudio(console));

        return builder.setUser(Client.user.getFirst()).setCreationDate(LocalDateTime.now()).build();
    }

    /**
     * Запрашивает у пользователя данные для создания объекта Coordinates.
     *
     * @param console Консоль для ввода/вывода.
     * @return Объект Coordinates с введенными данными.
     * @throws Breaker               Исключение, выбрасываемое при вводе команды "exit".
     * @throws IllegalInputException Исключение, выбрасываемое при некорректном вводе.
     */
    private static Coordinates askCoordinates(Console console) throws Breaker, IllegalInputException {
        console.println("Ввод значений поля Coordinates...");
        double x;
        do {
            console.println("Введите значение поля x:");
            try {
                String input = console.readln();
                if (input.equals("exit")) {
                    throw new Breaker();
                } else {
                    x = Double.parseDouble(input);
                }
            } catch (NumberFormatException e) {
                x = -981;
            }
            if (x <= -980) {
                if (console instanceof FileConsole) {
                    throw new IllegalInputException("Некорректное значение поля x!\nЗначение поля должно быть больше -980");
                }
                console.printError("Некорректное значение поля x!\nЗначение поля должно быть больше -980");
            }
        } while (x <= -980);

        Float y;
        do {
            console.println("Введите значение поля y:");
            try {
                String input = console.readln();
                if (input.equals("exit")) {
                    throw new Breaker();
                } else {
                    y = Float.valueOf(input);
                }
            } catch (NumberFormatException e) {
                y = 296F;
            }
            if (y > 295) {
                if (console instanceof FileConsole) {
                    throw new IllegalInputException("Некорректное значение поля y!\nМаксимальное значение поля: 295");
                }
                console.printError("Некорректное значение поля y!\nМаксимальное значение поля: 295");
            }
        } while (y > 295);

        console.println("Значения поля Coordinates записаны...");
        return new Coordinates(x, y);
    }

    /**
     * Запрашивает у пользователя данные для создания объекта Studio.
     *
     * @param console Консоль для ввода/вывода.
     * @return Объект Studio с введенными данными.
     * @throws Breaker Исключение, выбрасываемое при вводе команды "exit".
     */
    private static Event askStudio(Console console) throws Breaker {
        console.println("Ввод значений поля Event...");
        String name;
        do {
            console.println("Введите значение поля name:");
            name = console.readln();
            if (name.equals("exit")) {
                throw new Breaker();
            }
        } while (name.isEmpty());

        String time;

        do {
            console.println("Введите значение поля time:");
            time = console.readln();
            if (time.equals("exit")) {
                throw new Breaker();
            }
        } while (time.isEmpty());

        console.println("Значения поля Event записаны...");
        return new Event(name, time);
    }
}