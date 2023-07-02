/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.network;

import de.timesnake.database.util.object.Type;

import java.util.function.Consumer;

public class NetworkServer extends NetworkServerInfo {

  public static final int DEFAULT_MAX_HEALTH = 2048;
  public static final int DEFAULT_MAX_PLAYERS = 100;
  public static final int DEFAULT_PLAYER_TRACKING_RANGE = 48;
  public static final int DEFAULT_VIEW_DISTANCE = 10;
  public static final int DEFAULT_SIMULATION_DISTANCE = 10;

  public static int DEFAULT_CHANNEL_PORT_OFFSET = 10000;
  public static int DEFAULT_CHANNEL_PROXY_PORT = 35565;
  public static String DEFAULT_HOST_NAME = "127.0.0.1";
  public static String DEFAULT_PROXY_HOST_NAME = "127.0.0.1";
  public static String DEFAULT_PROXY_SERVER_NAME = "proxy";
  public static String DEFAULT_LISTEN_HOST_NAME = "0.0.0.0";

  protected final int port;
  protected String velocitySecret;
  protected String channelHostName = DEFAULT_HOST_NAME;
  protected String channelListenHostName = DEFAULT_LISTEN_HOST_NAME;
  protected String channelProxyHostName = DEFAULT_PROXY_HOST_NAME;
  protected String channelProxyServerName = DEFAULT_PROXY_SERVER_NAME;
  protected int channelPortOffset = DEFAULT_CHANNEL_PORT_OFFSET;
  protected int channelProxyPort = DEFAULT_CHANNEL_PROXY_PORT;

  protected int maxPlayers = DEFAULT_MAX_PLAYERS;
  protected int playerTrackingRange = DEFAULT_PLAYER_TRACKING_RANGE;
  protected int maxHealth = DEFAULT_MAX_HEALTH;
  protected boolean allowNether = false;
  protected boolean allowEnd = false;
  protected int viewDistance = DEFAULT_VIEW_DISTANCE;
  protected int simulationDistance = DEFAULT_SIMULATION_DISTANCE;

  private final Options options = new Options();

  public NetworkServer(String name, int port, Type.Server<?> type) {
    super(name, type);
    this.port = port;
  }

  public NetworkServer setFolderName(String name) {
    return ((NetworkServer) super.setFolderName(name));
  }

  public NetworkServer setTask(String task) {
    return ((NetworkServer) super.setTask(task));
  }

  public int getPort() {
    return port;
  }

  public NetworkServer setVelocitySecret(String velocitySecret) {
    this.velocitySecret = velocitySecret;
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

  public String getChannelHostName() {
    return channelHostName;
  }

  public NetworkServer setChannelHostName(String channelHostName) {
    this.channelHostName = channelHostName;
    return this;
  }

  public String getChannelProxyHostName() {
    return channelProxyHostName;
  }

  public NetworkServer setChannelProxyHostName(String channelProxyHostName) {
    this.channelProxyHostName = channelProxyHostName;
    return this;
  }

  public int getChannelPortOffset() {
    return channelPortOffset;
  }

  public NetworkServer setChannelPortOffset(int channelPortOffset) {
    this.channelPortOffset = channelPortOffset;
    return this;
  }

  public int getChannelProxyPort() {
    return channelProxyPort;
  }

  public NetworkServer setChannelProxyPort(int channelProxyPort) {
    this.channelProxyPort = channelProxyPort;
    return this;
  }

  public String getChannelListenHostName() {
    return channelListenHostName;
  }

  public NetworkServer setChannelListenHostName(String channelListenHostName) {
    this.channelListenHostName = channelListenHostName;
    return this;
  }

  public String getChannelProxyServerName() {
    return channelProxyServerName;
  }

  public NetworkServer setChannelProxyServerName(String channelProxyServerName) {
    this.channelProxyServerName = channelProxyServerName;
    return this;
  }

  public NetworkServer options(Consumer<Options> optionsConsumer) {
    optionsConsumer.accept(this.options);
    return this;
  }

  public Options getOptions() {
    return options;
  }

  public enum CopyType {
    NONE, COPY, SYNC
  }

  public static class Options {

    private CopyType worldCopyType = CopyType.NONE;
    private boolean syncPlayerData = false;
    private boolean syncLogs = true;

    public Options() {

    }

    public Options setWorldCopyType(CopyType copyType) {
      this.worldCopyType = copyType;
      return this;
    }

    public CopyType getWorldCopyType() {
      return worldCopyType;
    }

    @Deprecated
    public Options setSyncPlayerData(boolean syncPlayerData) {
      this.syncPlayerData = syncPlayerData;
      return this;
    }

    public boolean isSyncPlayerData() {
      return syncPlayerData;
    }

    public Options setSyncLogs(boolean syncLogs) {
      this.syncLogs = syncLogs;
      return this;
    }

    public boolean isSyncLogs() {
      return syncLogs;
    }
  }
}
