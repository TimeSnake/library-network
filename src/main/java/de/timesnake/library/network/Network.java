package de.timesnake.library.network;

import de.timesnake.database.util.object.Type;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface Network {

    String DEFAULT_DIRECTORY = "default";
    String TEMPLATE_DIR_NAME = "templates";
    String SERVERS_TEMPLATE_NAME = "servers";
    String WORLDS_TEMPLATE_NAME = "worlds";
    String PLAYERS_TEMPLATE_NAME = "players";
    String PLAYER_DATA = "playerdata";
    String SERVERS = "servers";

    ServerCreationResult createServer(NetworkServer server, boolean copyWorlds, boolean syncPlayerData);

    void generateConfigurations(NetworkServer server) throws IOException, TemplateException;

    void copyServerBasis(String name, Type.Server<?> type, String task) throws IOException;

    void copyServerWorlds(String name, Type.Server<?> type, String task) throws IOException;

    void syncPlayerData(String name, Type.Server<?> type, String task) throws IOException;

    WorldSyncResult syncWorld(NetworkServer server, String worldName);

    WorldSyncResult exportAndSyncWorld(String serverName, String worldName, Path exportPath);

    List<String> getWorldNames(Type.Server<?> type, String task);
}
