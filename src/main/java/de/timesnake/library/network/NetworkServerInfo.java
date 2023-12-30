/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.network;

import de.timesnake.library.basic.util.ServerType;

public class NetworkServerInfo {

  protected final String name;
  protected final ServerType type;
  protected String folderName;
  protected String task;

  public NetworkServerInfo(String name, ServerType type) {
    this.name = name;
    this.type = type;
    this.folderName = name;
  }

  public String getName() {
    return name;
  }

  public ServerType getType() {
    return type;
  }

  public String getFolderName() {
    return folderName;
  }

  public NetworkServerInfo setFolderName(String name) {
    this.folderName = name;
    return this;
  }

  public String getTask() {
    return task;
  }

  public NetworkServerInfo setTask(String task) {
    this.task = task;
    return this;
  }
}
