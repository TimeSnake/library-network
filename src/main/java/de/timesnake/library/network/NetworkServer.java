/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.network;

import de.timesnake.library.basic.util.ServerType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class NetworkServer extends NetworkServerInfo {

  protected final int port;
  protected String velocitySecret;
  protected String channelHostName;
  protected String channelListenHostName;
  protected String channelProxyHostName;
  protected String channelProxyServerName;
  protected int channelPortOffset;
  protected int channelProxyPort;

  protected int maxPlayers = 100;

  protected final HashMap<String, String> configProperties = new HashMap<>();

  private final Options options = new Options();

  public NetworkServer(String name, int port, ServerType type) {
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

  public int getMaxPlayers() {
    return maxPlayers;
  }

  public NetworkServer setMaxPlayers(int maxPlayers) {
    this.maxPlayers = maxPlayers;
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

  public NetworkServer configProperty(String key, String value) {
    this.configProperties.put(key, value);
    return this;
  }

  public HashMap<String, String> getConfigProperties() {
    return configProperties;
  }

  public NetworkServer applyServerOptions(Map<String, String> serverOptions) {
    this.configProperties.putAll(serverOptions);
    return this;
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
