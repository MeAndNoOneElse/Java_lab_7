package com.lab7.common.models;

import com.lab7.common.utility.Element;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Класс, представляющий музыкальную группу.
 */
public class Ticket extends Element implements Serializable {
    @Serial
    private static final long serialVersionUID = 20L;
    private Long id; // Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; // Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; // Поле не может быть null
    private final LocalDateTime creationDate; // Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Long price; // Поле может быть null, Значение поля должно быть больше 0
    private String description; // Поле не может быть null
    private TicketType type; // Поле может быть null
    private Event event; //Поле не может быть null
    private final String user;

    protected Ticket(Long id, String name, Coordinates coordinates, LocalDateTime creationDate, Long price, String description, TicketType type, Event event, String user) {
        this.id = id; //Ставится значение по умолчанию, так как id присваивается в БД
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.price = price;
        this.description = description;
        this.type = type;
        this.event = event;
        this.user = user;
    }

    public void updateId(Long id) {
        this.id = id;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public void updatePrice(Long price) {
        this.price = price;
    }



    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateType(TicketType type) {
        this.type = type;
    }

    public void updateEvent(Event event) {
        this.event = event;
    }

    /**
     * Возвращает идентификатор элемента.
     *
     * @return идентификатор элемента
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Возвращает название группы.
     *
     * @return название группы
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает координаты группы.
     *
     * @return координаты группы
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * Возвращает дату создания группы.
     *
     * @return дата создания группы
     */
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Возвращает количество участников группы.
     *
     * @return количество участников группы
     */
    public Long getPrice() {
        return price;
    }


    /**
     * Возвращает описание группы.
     *
     * @return описание группы
     */
    public String getDescription() {
        return description;
    }

    /**
     * Возвращает жанр музыки группы.
     *
     * @return жанр музыки группы
     */
    public TicketType getType() {
        return type;
    }

    /**
     * Возвращает студию группы.
     *
     * @return студия группы
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Возвращает пользователя, которому принадлежит группа.
     *
     * @return пользователь группы
     */
    public String getUser() {
        return user;
    }

    /**
     * Проверяет равенство текущего объекта с другим объектом.
     *
     * @param object объект для сравнения
     * @return true, если объекты равны, иначе false
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Ticket ticket = (Ticket) object;
        return Objects.equals(id, ticket.id);
    }

    /**
     * Возвращает хэш-код объекта.
     *
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name, coordinates, creationDate, price, description, type, event, user);
    }

    /**
     * Возвращает строковое представление объекта.
     *
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", event=" + event +
                ", user='" + user + '\'' +
                '}';
    }

    /**
     * Проверяет валидность объекта.
     */
    public boolean validate() {
        if (id == null || id <= 0) return false;
        if (name == null || name.isEmpty()) return false;
        if (coordinates == null || !coordinates.validate()) return false;
        if (creationDate == null) return false;
        if (price <= 0) return false;
        if (description == null) return false;
        if (user == null) return false;
        return event != null && event.validate();
    }

    /**
     * Сравнивает текущий объект с другим объектом.
     *
     * @param o объект для сравнения
     * @return результат сравнения
     */
    @Override
    public int compareTo(Element o) {
        return (int) (this.id - o.getId());
    }
}