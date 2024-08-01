package de.littleprogrammer.marketplace.database;

import java.util.List;
import java.util.UUID;

public class DatabasePlayer {

    private UUID uuid;
    private List<String> history;

    public DatabasePlayer(UUID uuid, List<String> history) {
        this.uuid = uuid;
        this.history = history;
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<String> getHistory() {
        return history;
    }
}
