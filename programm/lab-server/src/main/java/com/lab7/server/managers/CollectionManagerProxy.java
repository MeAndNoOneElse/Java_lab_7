package com.lab7.server.managers;

import com.lab7.common.models.Ticket;
import com.lab7.common.models.TicketType;
import com.lab7.common.utility.ExecutionStatus;
import com.lab7.common.utility.Pair;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CollectionManagerProxy implements CollectionManager {
    private static volatile CollectionManagerProxy instance;
    private final CollectionManagerMain collectionManagerMain = CollectionManagerMain.getInstance();
    private final Map<Long, Ticket> ticketMap = new HashMap<>();
    private Stack<Ticket> collection = new Stack<>();
    private final AtomicBoolean isCacheValid = new AtomicBoolean(false);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Конструктор класса CollectionManagerProxy.
     */
    private CollectionManagerProxy() {
    }

    /**
     * Возвращает единственный экземпляр CollectionManagerProxy.
     *
     * @return Экземпляр CollectionManagerProxy.
     */
    public static CollectionManagerProxy getInstance() {
        if (instance == null) {
            synchronized (CollectionManagerProxy.class) {
                if (instance == null) {
                    instance = new CollectionManagerProxy();
                }
            }
        }
        return instance;
    }

    private void refreshCache() {
        if (!isCacheValid.get()) {
            lock.writeLock().lock(); // Блокировка для обновления кэша
            collection = collectionManagerMain.getCollection();
            ticketMap.clear();
            collection.forEach(band -> ticketMap.put(band.getId(), band));
            isCacheValid.set(true);
            lock.writeLock().unlock();
        }
    }

    @Override
    public Stack<Ticket> getTickets() {
        refreshCache();
        return collection;
    }

    @Override
    public void sort() {
        collectionManagerMain.sort();
        isCacheValid.set(false);
    }

    @Override
    public ExecutionStatus removeFirst(Pair<String, String> user) {
        ExecutionStatus status = collectionManagerMain.removeFirst(user);
        if (status.isSuccess()) {
            isCacheValid.set(false);
        }
        return status;
    }

    @Override
    public LocalDateTime getInitializationDate() {
        return collectionManagerMain.getInitializationDate();
    }

    @Override
    public LocalDateTime getLastSaveDate() {
        return collectionManagerMain.getLastSaveDate();
    }

    @Override
    public Stack<Ticket> getCollection() {
        refreshCache();
        return collection;
    }

    @Override
    public Ticket getById(Long id) {
        refreshCache();
        return ticketMap.get(id);
    }

    @Override
    public ExecutionStatus removeAllByGenre(TicketType genre, Pair<String, String> user) {
        ExecutionStatus status = collectionManagerMain.removeAllByGenre(genre, user);
        if (status.isSuccess()) {
            isCacheValid.set(false);
        }
        return status;
    }

    @Override
    public ExecutionStatus loadCollection() {
        ExecutionStatus status = collectionManagerMain.loadCollection();
        if (status.isSuccess()) {
            isCacheValid.set(false);
        }
        return status;
    }

    @Override
    public ExecutionStatus clear(Pair<String, String> user) {
        ExecutionStatus status = collectionManagerMain.clear(user);
        if (status.isSuccess()) {
            isCacheValid.set(false);
        }
        return status;
    }

    @Override
    public ExecutionStatus add(Ticket band, Pair<String, String> user) {
        ExecutionStatus status = collectionManagerMain.add(band, user);
        if (status.isSuccess()) {
            isCacheValid.set(false);
        }
        return status;
    }

    @Override
    public ExecutionStatus update(Ticket band, Pair<String, String> user) {
        ExecutionStatus status = collectionManagerMain.update(band, user);
        if (status.isSuccess()) {
            isCacheValid.set(false);
        }
        return status;
    }

    @Override
    public ExecutionStatus removeById(Long elementId, Pair<String, String> user) {
        ExecutionStatus status = collectionManagerMain.removeById(elementId, user);
        if (status.isSuccess()) {
            isCacheValid.set(false);
        }
        return status;
    }
}