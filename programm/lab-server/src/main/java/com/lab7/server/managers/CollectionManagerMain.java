package com.lab7.server.managers;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.concurrent.locks.ReadWriteLock;

import com.lab7.common.models.Ticket;
import com.lab7.common.models.TicketType;
import com.lab7.common.utility.ExecutionStatus;
import com.lab7.common.utility.Pair;

import com.lab7.server.Server;

/**
 * Класс, управляющий коллекцией музыкальных групп.
 */
class CollectionManagerMain implements CollectionManager {
    private static volatile CollectionManagerMain instance;
    private final DBManagerInterface dbManager = DBManager.getInstance();
    private final Map<Long, Ticket> ticketsMap = new HashMap<>();
    private Stack<Ticket> collection = new Stack<>();
    private LocalDateTime initializationDate;
    private LocalDateTime lastSaveDate;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Конструктор класса CollectionManagerMain.
     */
    private CollectionManagerMain() {
    }

    /**
     * Возвращает единственный экземпляр CollectionManagerMain.
     *
     * @return Экземпляр CollectionManagerMain.
     */
    public static CollectionManagerMain getInstance() {
        if (instance == null) {
            synchronized (CollectionManagerMain.class) {
                if (instance == null) {
                    instance = new CollectionManagerMain();
                }
            }
        }
        return instance;
    }

    /**
     * Возвращает коллекцию музыкальных групп.
     *
     * @return Коллекция музыкальных групп.
     */
    @Override
    public Stack<Ticket> getTickets() {
        lock.readLock().lock(); // Блокировка чтения
        try {
            return collection;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Сортирует коллекцию музыкальных групп.
     */
    @Override
    public void sort() {
        // Коллекция в бд хранятся уже в отсортированном виде, поэтому там ничего не делаем
        lock.writeLock().lock();
        try {
            collection.sort(Comparator.comparing(Ticket::getId));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Удаляет первую музыкальную группу из коллекции.
     */
    @Override
    public ExecutionStatus removeFirst(Pair<String, String> user) {
        lock.writeLock().lock();
        try {
            ExecutionStatus removeStatus = dbManager.removeById(collection.pop().getId(), user);
            if (removeStatus.isSuccess()) {
                lastSaveDate = LocalDateTime.now();
                collection.stream().findFirst().ifPresent(band -> {
                    ticketsMap.remove(band.getId());
                    collection = collection.stream().skip(1).collect(Collectors.toCollection(Stack::new));
                });
            }
            return removeStatus;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Возвращает дату инициализации коллекции.
     *
     * @return Дата инициализации коллекции.
     */
    @Override
    public LocalDateTime getInitializationDate() {
        return initializationDate;
    }

    /**
     * Возвращает дату последнего сохранения коллекции.
     *
     * @return Дата последнего сохранения коллекции.
     */
    @Override
    public LocalDateTime getLastSaveDate() {
        lock.readLock().lock(); // Блокировка чтения
        try {
            return lastSaveDate;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Возвращает коллекцию музыкальных групп.
     *
     * @return Коллекция музыкальных групп.
     */
    @Override
    public Stack<Ticket> getCollection() {
        lock.readLock().lock(); // Блокировка чтения
        try {
            return collection;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Возвращает музыкальную группу по идентификатору.
     *
     * @param id Идентификатор музыкальной группы.
     * @return Музыкальная группа с указанным идентификатором.
     */
    @Override
    public Ticket getById(Long id) {
        lock.readLock().lock(); // Блокировка чтения
        try {
            return ticketsMap.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Удаляет все музыкальные группы с указанным жанром.
     *
     * @param genre Жанр музыкальных групп для удаления.
     * @return Количество удалённых групп.
     */
    @Override
    public ExecutionStatus removeAllByGenre(TicketType genre, Pair<String, String> user) {
        lock.writeLock().lock();
        try {
            ExecutionStatus accessStatus = dbManager.checkUserPermission(user);
            if (!accessStatus.isSuccess()) {
                return accessStatus;
            }
            ExecutionStatus removeStatus;
            if (accessStatus.getMessage().equals("USER")) {
                removeStatus = dbManager.removeAllByGenre(genre, user);
                if (removeStatus.isSuccess()) {
                    lastSaveDate = LocalDateTime.now();
                    collection = collection.stream().filter(band -> !band.getType().equals(genre) || !band.getUser().equals(user.getFirst()))
                            .collect(Collectors.toCollection(Stack::new));
                    ticketsMap.entrySet().removeIf(entry -> entry.getValue().getType().equals(genre) && entry.getValue().getUser().equals(user.getFirst()));
                }
            } else {
                removeStatus = dbManager.removeAllByGenre(genre);
                if (removeStatus.isSuccess()) {
                    lastSaveDate = LocalDateTime.now();
                    collection = collection.stream().filter(band -> !band.getType().equals(genre)).collect(Collectors.toCollection(Stack::new));
                    ticketsMap.entrySet().removeIf(entry -> entry.getValue().getType().equals(genre));
                }
            }
            return removeStatus;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Загружает коллекцию музыкальных групп.
     *
     * @return Статус выполнения загрузки коллекции.
     */
    @Override
    public ExecutionStatus loadCollection() {
        lock.writeLock().lock();
        try {
            collection.clear();
            ticketsMap.clear();
            ExecutionStatus loadStatus = dbManager.loadCollection(collection);
            if (loadStatus.isSuccess()) {
                initializationDate = LocalDateTime.now();
                lastSaveDate = LocalDateTime.now();
                boolean hasDuplicates = collection.stream().anyMatch(band -> ticketsMap.putIfAbsent(band.getId(), band) != null);
                if (hasDuplicates) {
                    return new ExecutionStatus(false, "Ошибка загрузки коллекции: обнаружены дубликаты id!");
                }
            }
            return loadStatus;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Очищает коллекцию музыкальных групп.
     */
    @Override
    public ExecutionStatus clear(Pair<String, String> user) {
        lock.writeLock().lock();
        try {
            ExecutionStatus accessStatus = dbManager.checkUserPermission(user);
            if (!accessStatus.isSuccess()) {
                return accessStatus;
            }
            ExecutionStatus clearStatus;
            if (accessStatus.getMessage().equals("USER")) {
                clearStatus = dbManager.clear(user);
                if (clearStatus.isSuccess()) {
                    collection = collection.stream()
                            .filter(band -> !band.getUser().equals(user.getFirst()))
                            .collect(Collectors.toCollection(Stack::new));
                    ticketsMap.entrySet().removeIf(entry -> entry.getValue().getUser().equals(user.getFirst()));
                }
            }
            else {
                clearStatus = dbManager.clearAll();
                if (clearStatus.isSuccess()) {
                    collection.clear();
                    ticketsMap.clear();
                }
            }
            return clearStatus;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public ExecutionStatus add(Ticket ticket, Pair<String, String> user) {
        lock.writeLock().lock();
        try {
            if ((ticket != null) && ticket.validate()) {
                ExecutionStatus addStatus = dbManager.addTicket(ticket, user);
                int n =0;
                if (addStatus.isSuccess()) {
                    ticket.updateId(Long.parseLong(addStatus.getMessage()));
                    lastSaveDate = LocalDateTime.now();
                    collection.push(ticket);
                    ticketsMap.put(ticket.getId(), ticket);
                    return new ExecutionStatus(true, "Элемент успешно добавлен в коллекцию! Присвоенный id = " + addStatus.getMessage());
                }
                return new ExecutionStatus(false, "Произошла ошибка при добавлении коллекции в базу данных!"+n);
            }
            return new ExecutionStatus(false, "Элемент коллекции введён неверно!");
        } catch (SQLException e) {
            return new ExecutionStatus(false, "Ошибка при сохранении элемента коллекции в базу данных: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public ExecutionStatus update(Ticket ticket, Pair<String, String> user) {
        lock.writeLock().lock();
        try {
            ExecutionStatus updStatus = dbManager.updateTicket(ticket, user);
            if (updStatus.isSuccess()) {
                lastSaveDate = LocalDateTime.now();
                collection.stream()
                        .filter(existingBand -> existingBand.getId().equals(ticket.getId()))
                        .forEach(existingBand -> {
                            ticketsMap.remove(existingBand.getId(), existingBand);
                            existingBand.updateName(ticket.getName());
                            existingBand.updateCoordinates(ticket.getCoordinates());
                            existingBand.updatePrice(ticket.getPrice());
                            existingBand.updateDescription(ticket.getDescription());
                            existingBand.updateType(ticket.getType());
                            existingBand.updateEvent(ticket.getEvent());
                            ticketsMap.put(ticket.getId(), ticket);
                        });

            } else {
                Server.logger.severe("Error updating ticket in database: " + ticket.getName());
            }
            return updStatus;
        } catch (SQLException e) {
            return new ExecutionStatus(false, "Ошибка при сохранении элемента коллекции в базу данных: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Удаляет музыкальную группу по идентификатору.
     *
     * @param elementId Идентификатор музыкальной группы для удаления.
     */
    @Override
    public ExecutionStatus removeById(Long elementId, Pair<String, String> user) {
        lock.writeLock().lock();
        try {
            ExecutionStatus removeStatus = dbManager.removeById(elementId, user);
            if (removeStatus.isSuccess()) {
                collection = collection.stream().filter(band -> !band.getId().equals(elementId)).collect(Collectors.toCollection(Stack::new));
                ticketsMap.remove(elementId);
            }
            return removeStatus;
        } finally {
            lock.writeLock().unlock();
        }
    }
}