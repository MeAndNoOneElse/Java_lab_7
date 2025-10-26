package com.lab7.common.models;

import com.lab7.common.utility.Validatable;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Класс, представляющий студию.
 */
public class Event implements Validatable, Serializable {
    @Serial
    private static final long serialVersionUID = 22L;

    private String name; // Поле не может быть null
    private String time; //Поле не может быть null

    /**
     * Конструктор для создания объекта Studio.
     * @param name название студии, не может быть null
     * @param time адрес студии, может быть null
     */
    public Event(String name, String time) {
        this.name = name;
        this.time = time;
    }

    /**
     * Возвращает название студии.
     * @return название студии
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает адрес студии.
     * @return адрес студии
     */
    public String getTime() {
        return time;
    }

    /**
     * Проверяет равенство текущего объекта с другим объектом.
     * @param object объект для сравнения
     * @return true, если объекты равны, иначе false
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Event event = (Event) object;
        return Objects.equals(name, event.name) && Objects.equals(time, event.time);
    }

    /**
     * Возвращает хэш-код объекта.
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, time);
    }

    /**
     * Возвращает строковое представление объекта.
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return '{' +
                "name='" + name + '\'' +
                ", address='" + time + '\'' +
                '}';
    }

    /**
     * Проверяет валидность объекта.
     * @return true, если объект валиден, иначе false
     */
    @Override
    public boolean validate() {
        return name != null;
    }
}