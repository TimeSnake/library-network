/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.network;

import de.timesnake.database.util.object.Type;
import freemarker.template.TemplateException;
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

    ServerCreationResult createServer(NetworkServer server, boolean copyWorlds, boolean syncPlayerData);

    void generateConfigurations(NetworkServer server) throws IOException, TemplateException;

    void copyServerBasis(String name, Type.Server<?> type, String task) throws IOException;

    void copyServerWorlds(String name, Type.Server<?> type, String task) throws IOException;

    void syncLogs(String name, Type.Server<?> type, String task) throws IOException;

    void syncPlayerData(String name, Type.Server<?> type, String task) throws IOException;

    WorldSyncResult syncWorld(NetworkServer server, String worldName);

    WorldSyncResult exportAndSyncWorld(String serverName, String worldName, Path exportPath);

    List<String> getWorldNames(Type.Server<?> type, String task);

    ServerCreationResult createPublicPlayerServer(NetworkServer server);

    ServerCreationResult createPlayerServer(UUID uuid, NetworkServer server);

    ServerInitResult initPublicPlayerServer(Type.Server<?> type, String task, String name);

    ServerInitResult initPlayerServer(UUID uuid, Type.Server<?> type, String task, String name);

    List<String> getPublicPlayerServerNames(Type.Server<?> type, String task);

    List<String> getOwnerServerNames(UUID uuid, Type.Server<?> type, String task);

    Map<UUID, List<String>> getMemberServerNames(UUID member, Type.Server<?> type, String task);

    List<UUID> getPlayerServerMembers(UUID uuid, Type.Server<?> type, String task, String name);

    boolean setPlayerServerMembers(UUID uuid, Type.Server<?> type, String task, String name, List<UUID> memberUuids);
}
