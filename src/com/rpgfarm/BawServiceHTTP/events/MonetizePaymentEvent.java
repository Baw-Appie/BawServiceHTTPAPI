package com.rpgfarm.BawServiceHTTP.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class MonetizePaymentEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String command;

    public MonetizePaymentEvent(String example) {
        command = example;
    }

    public String getCommand() {
        return command;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
