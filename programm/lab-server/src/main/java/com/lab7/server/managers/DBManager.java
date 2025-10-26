package com.lab7.server.managers;

import com.lab7.common.models.Ticket;
import com.lab7.common.models.TicketType;
import com.lab7.common.models.Coordinates;
import com.lab7.common.models.Event;
import com.lab7.common.models.TicketBuilder;
import com.lab7.common.utility.ExecutionStatus;
import com.lab7.common.utility.Pair;
import com.lab7.common.utility.PermissionType;
import com.lab7.server.Server;
import com.lab7.server.utility.Transactional;
import com.lab7.server.utility.TransactionalProxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.Stack;

/**
 * Класс, управляющий сохранением и загрузкой коллекции музыкальных групп.
 */
public class DBManager implements DBManagerInterface {
    private static volatile DBManagerInterface instance;
    private static Connection connection;

    /**
     * Конструктор для создания объекта DBManager.
     */
    private DBManager() {
        try (FileInputStream input = new FileInputStream("dbconfig.properties")) {
            Properties properties = new Properties();
            properties.load(input);

            String url = properties.getProperty("db.url");
            String user = properties.getProperty("db.user"); // sXXXXXX
            String password = properties.getProperty("db.password"); // пароль из файла .pgpass

            connection = DriverManager.getConnection(url, user, password);
            Server.logger.info("Connected to database successfully");
        } catch (SQLException e) {
            Server.logger.severe("Failed to connect to database: " + e.getMessage());
        } catch (IOException e) {
            Server.logger.severe("Failed to load database properties: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static DBManagerInterface getInstance() {
        if (instance == null) {
            synchronized (DBManager.class) {
                if (instance == null) {
                    instance = new DBManager();
                    instance = TransactionalProxy.createProxy(instance, connection); // оборачиваем в прокси для поддержки транзакций
                }
            }
        }
        return instance;
    }

    public ExecutionStatus addUser(Pair<String, String> user) {
        String query = "INSERT INTO users (username, password) VALUES (?, ?);";
        try (PreparedStatement p = connection.prepareStatement(query)) {
            p.setString(1, user.getFirst());
            p.setString(2, user.getSecond());
            p.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            return new ExecutionStatus(false, "Ошибка при добавлении пользователя в базу данных: " + e.getMessage());
        }
        return new ExecutionStatus(true, "User registered successfully!");
    }

    private ExecutionStatus checkUser(Pair<String, String> user) {
        String query = "SELECT EXISTS(SELECT 1 FROM users WHERE username = ?);";
        try (PreparedStatement p = connection.prepareStatement(query)) {
            p.setString(1, user.getFirst());
            ResultSet res = p.executeQuery();
            if (res.next() && res.getBoolean(1)) {
                return new ExecutionStatus(true, "Пользователь успешно найден!");
            } else {
                return new ExecutionStatus(false, "Пользователь не найден!");
            }
        } catch (SQLException | NullPointerException e) {
            return new ExecutionStatus(false, "Ошибка при проверке пользователя в базе данных: " + e.getMessage());
        }
    }

    public ExecutionStatus showUserList(Pair<String, String> user) {
        String query = "SELECT username, permissions FROM users;";
        try (PreparedStatement p = connection.prepareStatement(query); ResultSet res = p.executeQuery()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Список пользователей:\n");
            while (res.next()) {
                stringBuilder.append(res.getString("username")).append(" - Permissions: ").append(res.getString("permissions")).append("\n");
            }
            return new ExecutionStatus(true, stringBuilder.toString());
        } catch (SQLException | NullPointerException e) {
            return new ExecutionStatus(false, "Ошибка при проверке пользователей в базе данных: " + e.getMessage());
        }
    }

    public ExecutionStatus checkPassword(Pair<String, String> user) {
        ExecutionStatus checkUserStatus = checkUser(user);
        if (checkUserStatus.isSuccess()) {
            String query = "SELECT password FROM users WHERE username = ?;";
            try (PreparedStatement p = connection.prepareStatement(query)) {
                p.setString(1, user.getFirst());
                ResultSet res = p.executeQuery();
                if (res.next()) {
                    String password = res.getString("password");
                    boolean match = password.equals(user.getSecond());
                    if (match) {
                        return new ExecutionStatus(true, "Login successful!");
                    } else {
                        return new ExecutionStatus(false, "Введён неверный пароль!");
                    }
                } else {
                    return new ExecutionStatus(false, "Пользователь не найден!");
                }
            } catch (SQLException | NullPointerException e) {
                return new ExecutionStatus(false, "Ошибка при проверке пользователя в базе данных: " + e.getMessage());
            }
        } else {
            return new ExecutionStatus(false, checkUserStatus.getMessage());
        }
    }

    public ExecutionStatus updateUserPermissions(String username, PermissionType permission) {
        String query = "UPDATE users SET permissions = ? WHERE username = ?;";
        try (PreparedStatement p = connection.prepareStatement(query)) {
            p.setString(1, permission.name());
            p.setString(2, username);
            int affectedRows = p.executeUpdate();
            if (affectedRows > 0) {
                return new ExecutionStatus(true, "Права пользователя " + username + " успешно обновлены!");
            } else {
                return new ExecutionStatus(false, "Пользователь " + username + " не найден!");
            }
        } catch (SQLException | NullPointerException e) {
            return new ExecutionStatus(false, "Ошибка при обновлении прав пользователя в базе данных: " + e.getMessage());
        }
    }

    public ExecutionStatus checkUserPermission(Pair<String, String> user) {
        String query = "SELECT permissions FROM users WHERE username = ?;";
        try (PreparedStatement p = connection.prepareStatement(query)) {
            p.setString(1, user.getFirst());
            ResultSet res = p.executeQuery();
            if (res.next()) {
                String permission = res.getString("permissions");
                return new ExecutionStatus(true, permission);
            } else {
                return new ExecutionStatus(false, "Пользователь не найден!");
            }
        } catch (SQLException | NullPointerException e) {
            return new ExecutionStatus(false, "Ошибка при проверке прав пользователя в базе данных: " + e.getMessage());
        }
    }

    public ExecutionStatus clear(Pair<String, String> user) {
        String query = "DELETE FROM tickets WHERE user_id IN (SELECT id FROM users WHERE username = ?);";
        try (PreparedStatement p = connection.prepareStatement(query)) {
            p.setString(1, user.getFirst());
            int affectedRows = p.executeUpdate();
            if (affectedRows > 0) {
                return new ExecutionStatus(true, "Успешно удалено " + affectedRows + " элементов пользователя " + user.getFirst() + "!");
            } else {
                return new ExecutionStatus(true, "У пользователя " + user.getFirst() + " нет элементов в коллекции!");
            }
        } catch (SQLException | NullPointerException e) {
            return new ExecutionStatus(false, "Ошибка при очистке коллекции в базе данных: " + e.getMessage());
        }
    }

    public ExecutionStatus clearAll() {
        String query = "TRUNCATE tickets CASCADE;";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
            return new ExecutionStatus(true, "Коллекция успешно очищена!");
        } catch (SQLException e) {
            return new ExecutionStatus(false, "Ошибка при очистке коллекции: " + e.getMessage());
        }
    }

    public ExecutionStatus removeById(Long id, Pair<String, String> user) {
        ExecutionStatus accessStatus = checkUserPermission(user);
        if (! accessStatus.isSuccess()) {
            return accessStatus;
        }
        String query;
        if (accessStatus.getMessage().equals("USER")) {
            query = "DELETE FROM tickets WHERE id = ? AND user_id IN (SELECT id FROM users WHERE username = ?);";
        } else {
            query = "DELETE FROM tickets WHERE id = ?;";
        }
        try (PreparedStatement p = connection.prepareStatement(query)) {
            p.setLong(1, id);
            if (accessStatus.getMessage().equals("USER")) {
                p.setString(2, user.getFirst());
            }
            int affectedRows = p.executeUpdate();
            if (affectedRows > 0) {
                return new ExecutionStatus(true, "Элемент успешно удалён!");
            } else {
                return new ExecutionStatus(false, "Элемент не может быть удалён, так как вы не являетесь его владельцем!");
            }
        } catch (SQLException | NullPointerException e) {
            return new ExecutionStatus(false, "Ошибка при удалении элемента коллекции из базы данных: " + e.getMessage());
        }
    }

    public ExecutionStatus removeAllByGenre(TicketType type, Pair<String, String> user) {
        String query = "DELETE FROM tickets WHERE genre_id = ? AND user_id IN (SELECT id FROM users WHERE username = ?);";
        try (PreparedStatement p = connection.prepareStatement(query)) {
            p.setLong(1, type.ordinal() + 1);
            p.setString(2, user.getFirst());
            int affectedRows = p.executeUpdate();
            if (affectedRows > 0) {
                return new ExecutionStatus(true, "Успешно удалено " + affectedRows + " элементов с жанром " + type + "!");
            } else {
                return new ExecutionStatus(false, "Доступные для удаления элементы с указанным genre не найдены!");
            }
        } catch (SQLException | NullPointerException e) {
            return new ExecutionStatus(false, "Ошибка при удалении элемента коллекции из базы данных: " + e.getMessage());
        }
    }

    public ExecutionStatus removeAllByGenre(TicketType type) {
        String query = "DELETE FROM tickets WHERE genre_id = ?;";
        try (PreparedStatement p = connection.prepareStatement(query)) {
            p.setLong(1, type.ordinal() + 1);
            int affectedRows = p.executeUpdate();
            if (affectedRows > 0) {
                return new ExecutionStatus(true, "Успешно удалено " + affectedRows + " элементов с жанром " + type + "!");
            } else {
                return new ExecutionStatus(false, "Доступные для удаления элементы с указанным genre не найдены!");
            }
        } catch (SQLException | NullPointerException e) {
            return new ExecutionStatus(false, "Ошибка при удалении элемента коллекции из базы данных: " + e.getMessage());
        }
    }

    @Transactional
    public ExecutionStatus addTicket(Ticket ticket, Pair<String, String> user) throws SQLException {
        // Запись координат
        int coordinatesId = - 1;
        String insertCoordinates = "INSERT INTO coordinates (x, y) VALUES (?, ?) RETURNING id";
        try (PreparedStatement coordinatesStmt = connection.prepareStatement(insertCoordinates)) {
            coordinatesStmt.setDouble(1, ticket.getCoordinates().getX());
            coordinatesStmt.setFloat(2, ticket.getCoordinates().getY());
            ResultSet rs = coordinatesStmt.executeQuery();
            if (rs.next()) {
                coordinatesId = rs.getInt("id");
            }
        }

        // Запись мероприятия
        int eventId = - 1;
        String insertEvent = "INSERT INTO event (name, time) VALUES (?, ?) RETURNING id";
        try (PreparedStatement EventStmt = connection.prepareStatement(insertEvent)) {
            EventStmt.setString(1, ticket.getEvent().getName());
            EventStmt.setString(2, ticket.getEvent().getTime());
            ResultSet rs = EventStmt.executeQuery();
            if (rs.next()) {
                eventId = rs.getInt("id");
            }
        }

        // Запись самой музыкальной группы
        String insertTicket = "INSERT INTO tickets (name, coordinates_id, creation_date, price, description, type_id, event_id, user_id) " + "VALUES (?, ?,  ?, ?, ?, ?, ?, (SELECT id FROM users WHERE username = ?)) RETURNING id";
        try (PreparedStatement ticketStmt = connection.prepareStatement(insertTicket)) {
            ticketStmt.setString(1, ticket.getName());
            ticketStmt.setInt(2, coordinatesId);
            ticketStmt.setTimestamp(3, Timestamp.valueOf(ticket.getCreationDate()));
            ticketStmt.setLong(4, ticket.getPrice());
            ticketStmt.setString(5, ticket.getDescription());
            ticketStmt.setInt(6, ticket.getType().ordinal() + 1);
            ticketStmt.setInt(7, eventId);
            ticketStmt.setString(8, user.getFirst());
            ResultSet rs = ticketStmt.executeQuery();
            if (rs.next()) {
                return new ExecutionStatus(true, rs.getString("id"));
            } else {
                throw new SQLException();
            }
        }
    }

    @Transactional
    public ExecutionStatus updateTicket(Ticket ticket, Pair<String, String> user) throws SQLException {
        int coordinatesId;
        int eventId;

        ExecutionStatus accessStatus = checkUserPermission(user);
        if (! accessStatus.isSuccess()) {
            return accessStatus;
        }
        String updateBandQuery;
        if (accessStatus.getMessage().equals("USER")) {
            updateBandQuery = "UPDATE tickets SET name = ?, price = ?,  description = ?, type_id = ? WHERE id = ? AND user_id IN (SELECT id FROM users WHERE username = ?) RETURNING coordinates_id, event_id";
        } else {
            updateBandQuery = "UPDATE tickets SET name = ?, price = ?, description = ?, type_id = ? WHERE id = ? RETURNING coordinates_id, event_id";
        }

        String checkQuery = "SELECT COUNT(*) FROM tickets WHERE id = ?;";
        // Обновление самой музыкальной группы
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery); PreparedStatement ticketStmt = connection.prepareStatement(updateBandQuery)) {
            checkStmt.setLong(1, ticket.getId());
            ResultSet checkResult = checkStmt.executeQuery();
            if (checkResult.next() && checkResult.getInt(1) == 0) {
                return new ExecutionStatus(false, "Элемент с указанным id не найден!");
            }

            ticketStmt.setString(1, ticket.getName());
            ticketStmt.setLong(2, ticket.getPrice());
            ticketStmt.setString(3, ticket.getDescription());
            ticketStmt.setInt(4, ticket.getType().ordinal() + 1);
            ticketStmt.setLong(5, ticket.getId());
            if (accessStatus.getMessage().equals("USER")) {
                ticketStmt.setString(6, user.getFirst());
            }
            ResultSet rs = ticketStmt.executeQuery();
            if (rs.next()) {
                coordinatesId = rs.getInt("coordinates_id");
                eventId = rs.getInt("event_id");
            } else {
                return new ExecutionStatus(false, "Пользователь не является владельцем элемента коллекции!");
            }
        }

        // Обновление координат
        String updateCoordinates = "UPDATE coordinates SET x = ?, y = ? WHERE id = ?";
        try (PreparedStatement coordinatesStmt = connection.prepareStatement(updateCoordinates)) {
            coordinatesStmt.setDouble(1, ticket.getCoordinates().getX());
            coordinatesStmt.setFloat(2, ticket.getCoordinates().getY());
            coordinatesStmt.setInt(3, coordinatesId);
            coordinatesStmt.executeUpdate();
        }

        // Обновление студии
        String insertEvent = "UPDATE event SET name = ?, time = ? WHERE id = ?";
        try (PreparedStatement eventStmt = connection.prepareStatement(insertEvent)) {
            eventStmt.setString(1, ticket.getEvent().getName());
            eventStmt.setString(2, ticket.getEvent().getTime());
            eventStmt.setInt(3, eventId);
            eventStmt.executeUpdate();
        }
        return new ExecutionStatus(true, "Элемент успешно обновлён!");
    }

    /**
     * Загружает коллекцию музыкальных групп из базы данных.
     *
     * @param collection коллекция музыкальных групп
     */
    public ExecutionStatus loadCollection(Stack<Ticket> collection) {
        String query = "SELECT tickets.id       AS id, " +
                "tickets.name                   AS ticket_name, " +
                "coordinates.x                      AS coordinates_x, " +
                "coordinates.y                      AS coordinates_y, " +
                "tickets.creation_date          AS creation_date, " +
                "tickets.price AS price, " +
                "tickets.description            AS description, " +
                "type.type_name             AS type_name, " +
                "event.name                        AS event_name, " +
                "event.time                     AS event_time, " +
                "users.username                     AS username " +
                "FROM tickets " +
                "JOIN coordinates ON tickets.coordinates_id = coordinates.id " +
                "JOIN event ON tickets.event_id = event.id " +
                "JOIN type ON tickets.type_id = type.id " +
                "JOIN users ON tickets.user_id = users.id;";
        try (PreparedStatement p = connection.prepareStatement(query); ResultSet res = p.executeQuery()) {
            while (res.next()) {
                Ticket ticket = new TicketBuilder().setId(res.getLong("id")).setName(res.getString("ticket_name")).setCoordinates(new Coordinates(res.getDouble("coordinates_x"), res.getFloat("coordinates_y"))).setCreationDate(res.getTimestamp("creation_date").toLocalDateTime()).setPrice(res.getLong("price")).setDescription(res.getString("description")).setType(TicketType.valueOf(res.getString("type_name"))).setEvent(new Event(res.getString("event_name"), res.getString("event_time"))).setUser(res.getString("username")).build();
                collection.push(ticket);
            }
        } catch (IllegalArgumentException e) {
            return new ExecutionStatus(false, "Введены некорректные данные элемента коллекции!"+e);
        } catch (SQLException e) {
            return new ExecutionStatus(false, "Произошла ошибка при чтении коллекции из базы данных!"+e);
        }
        return new ExecutionStatus(true, "Коллекция успешно загружена!");
    }
}