package de.timesnake.library.network;

import de.timesnake.database.util.object.Type;

public class NetworkServer {

    public static final int DEFAULT_MAX_PLAYERS = 100;
    public static final int DEFAULT_PLAYER_TRACKING_RANGE = 48;

    private final String name;
    private final int port;
    private final Type.Server<?> type;
    private final String velocitySecret;
    private String task;
    private int maxPlayers = DEFAULT_MAX_PLAYERS;
    private int playerTrackingRange = DEFAULT_PLAYER_TRACKING_RANGE;
    private boolean allowNether = false;

    public NetworkServer(String name, int port, Type.Server<?> type, String velocitySecret) {
        this.name = name;
        this.port = port;
        this.type = type;
        this.velocitySecret = velocitySecret;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public Type.Server<?> getType() {
        return type;
    }

    public String getTask() {
        return task;
    }

    public NetworkServer setTask(String task) {
        this.task = task;
        return this;
    }

    public String getVelocitySecret() {
        return velocitySecret;
    }

    public boolean isAllowNether() {
        return allowNether;
    }

    public String isAllowNetherString() {
        return allowNether ? "true" : "false";
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public NetworkServer setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        return this;
    }

    public NetworkServer allowNether(boolean allow) {
        this.allowNether = allow;
        return this;
    }

    public int getPlayerTrackingRange() {
        return playerTrackingRange;
    }

    public NetworkServer setPlayerTrackingRange(int playerTrackingRange) {
        this.playerTrackingRange = playerTrackingRange;
        return this;
    }
}
