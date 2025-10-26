package com.lab7.common.models;

/**
 * Перечисление, представляющее музыкальные жанры.
 */
public enum TicketType {
    VIP,
    USUAL,
    CHEAP,
    BUDGETARY;

    /**
     * Возвращает строку, содержащую все музыкальные жанры, разделенные запятыми.
     * @return строка с перечислением всех музыкальных жанров
     */
    public static String list() {
        StringBuilder list = new StringBuilder();
        for (TicketType genre : TicketType.values()) {
            list.append(genre).append(", ");
        }
        return list.substring(0, list.length() - 2);
    }
}