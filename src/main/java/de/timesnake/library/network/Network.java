/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.network;

import de.timesnake.library.basic.util.ServerType;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface Network {

  String DEFAULT_DIRECTORY = "default";
  String BASIS_DIRECTORY = "basis";
  String DEFAULT_PLAYER_DIRECTORY = "player_default";
  String PUBLIC_DIRECTORY = "public";
  String TEMPLATE_DIR_NAME = "templates";
  String SERVERS_TEMPLATE_NAME = "servers";
  String WORLDS_TEMPLATE_NAME = "worlds";
  String PLAYERS_TEMPLATE_NAME = "players";
  String PLAYER_DATA = "playerdata";
  String SERVERS = "servers";

  SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

  String LOGS_DIR_NAME = "logs";

  String OWN_SERVER_INFO_FILE_NAME = "own_server_info.toml";
  String OWN_SERVER_OWNER_UUID = "owner_uuid";
  String OWN_SERVER_MEMBER_UUIDS = "member_uuids";

  ServerCreationResult createServer(NetworkServer server);

  void generateConfigurations(NetworkServer server) throws IOException, TemplateException;

  void copyServerFromTemplate(NetworkServerInfo info) throws IOException;

  void copyServerWorlds(NetworkServerInfo info) throws IOException;

  void syncServerWorlds(NetworkServerInfo info) throws IOException;

  void syncLogs(NetworkServerInfo info) throws IOException;

  void syncPlayerData(NetworkServerInfo info) throws IOException;

  WorldSyncResult syncWorld(NetworkServerInfo server, String worldName);

  WorldSyncResult exportAndSyncWorld(String serverName, String worldName, Path exportPath);

  List<String> getWorldNames(ServerType type, String task);

  List<File> getWorldFiles(ServerType type, String task);

  ServerCreationResult createPublicPlayerServer(NetworkServer server);

  ServerCreationResult createPlayerServer(UUID uuid, NetworkServer server);

  ServerInitResult initNewPublicPlayerServer(ServerType type, String task, String name);

  ServerInitResult initNewPlayerServer(UUID uuid, ServerType type, String task, String name);

  List<String> getPublicPlayerServerNames(ServerType type, String task);

  List<String> getOwnerServerNames(UUID uuid, ServerType type, String task);

  Map<UUID, List<String>> getMemberServerNames(UUID member, ServerType type, String task);

  List<UUID> getPlayerServerMembers(UUID uuid, ServerType type, String task, String name);

  boolean setPlayerServerMembers(UUID uuid, ServerType type, String task, String name, List<UUID> memberUuids);
}
