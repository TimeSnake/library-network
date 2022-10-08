/*
 * library-network.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
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
