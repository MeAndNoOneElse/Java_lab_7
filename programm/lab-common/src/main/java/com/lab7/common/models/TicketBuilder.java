package com.lab7.common.models;

import java.time.LocalDateTime;

public class TicketBuilder {
    private Long id = 1L; // Значение по умолчанию, так как id присваивается в БД
    private String name;
    private com.lab7.common.models.Coordinates coordinates;
    private LocalDateTime creationDate;
    private Long price;
    private String description;
    private TicketType type;
    private Event event;
    private String user;

    public TicketBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public TicketBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public TicketBuilder setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    public TicketBuilder setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public TicketBuilder setPrice(Long price) {
        this.price = price;
        return this;
    }

    public TicketBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public TicketBuilder setType(TicketType type) {
        this.type = type;
        return this;
    }

    public TicketBuilder setEvent(Event event) {
        this.event = event;
        return this;
    }

    public TicketBuilder setUser(String user) {
        this.user = user;
        return this;
    }

    public Ticket build() {
        Ticket band = new Ticket(
                id,
                name,
                coordinates,
                creationDate, price,
                description, type, event,
                user
        );
        if (band.validate()){
            return band;
        }
        else {
            throw new IllegalArgumentException("Некорректные данные для создания объекта Ticket");
        }
    }
}