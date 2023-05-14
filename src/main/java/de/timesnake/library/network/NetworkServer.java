/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.network;

import de.timesnake.database.util.object.Type;

public class NetworkServer {

    public static final int DEFAULT_MAX_HEALTH = 2048;
    public static final int DEFAULT_MAX_PLAYERS = 100;
    public static final int DEFAULT_PLAYER_TRACKING_RANGE = 48;
    public static final int DEFAULT_VIEW_DISTANCE = 10;
    public static final int DEFAULT_SIMULATION_DISTANCE = 10;

    private final String name;
    private final int port;
    private final Type.Server<?> type;
    private final String velocitySecret;
    private String folderName;
    private String task;
    private int maxPlayers = DEFAULT_MAX_PLAYERS;
    private int playerTrackingRange = DEFAULT_PLAYER_TRACKING_RANGE;
    private int maxHealth = DEFAULT_MAX_HEALTH;
    private boolean allowNether = false;
    private boolean allowEnd = false;
    private int viewDistance = DEFAULT_VIEW_DISTANCE;
    private int simulationDistance = DEFAULT_SIMULATION_DISTANCE;

    public NetworkServer(String name, int port, Type.Server<?> type, String velocitySecret) {
        this.name = name;
        this.folderName = name;
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

    public String getFolderName() {
        return folderName;
    }

    public NetworkServer setFolderName(String name) {
        this.folderName = name;
        return this;
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

    public boolean isAllowEnd() {
        return allowEnd;
    }

    public String isAllowEndString() {
        return allowEnd ? "true" : "false";
    }

    public NetworkServer allowEnd(boolean allow) {
        this.allowEnd = allow;
        return this;
    }

    public int getPlayerTrackingRange() {
        return playerTrackingRange;
    }

    public NetworkServer setPlayerTrackingRange(int playerTrackingRange) {
        this.playerTrackingRange = playerTrackingRange;
        return this;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public NetworkServer setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        return this;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public NetworkServer setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
        return this;
    }

    public int getSimulationDistance() {
        return this.simulationDistance;
    }

    public NetworkServer setSimulationDistance(int simulationDistance) {
        this.simulationDistance = simulationDistance;
        return this;
    }

    public enum CopyType {
        NONE, COPY, SYNC
    }
}
