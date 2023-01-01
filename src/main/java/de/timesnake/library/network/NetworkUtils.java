/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.network;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import de.timesnake.database.util.object.Type;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FileUtils;

public class NetworkUtils implements Network {

    public static Network getInstance() {
        return instance;
    }

    private static Network instance;
    private final Path networkPath;
    private final Path serverTemplatePath;
    private final Path worldsTemplatePath;
    private final Path playersTemplatePath;
    private final Path logsPath;

    private final Configuration cfg;

    public NetworkUtils(Path networkPath) {
        instance = this;
        this.networkPath = networkPath;
        this.serverTemplatePath = this.networkPath.resolve(TEMPLATE_DIR_NAME)
                .resolve(SERVERS_TEMPLATE_NAME);
        this.worldsTemplatePath = this.networkPath.resolve(TEMPLATE_DIR_NAME)
                .resolve(WORLDS_TEMPLATE_NAME);
        this.playersTemplatePath = this.networkPath.resolve(TEMPLATE_DIR_NAME)
                .resolve(PLAYERS_TEMPLATE_NAME);
        this.logsPath = this.networkPath.resolve(LOGS_DIR_NAME);

        this.cfg = new Configuration(Configuration.VERSION_2_3_29);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setNumberFormat("0.######");
    }

    @Override
    public ServerCreationResult createServer(NetworkServer server, boolean copyWorlds,
            boolean syncPlayerData) {
        try {
            this.copyServerBasis(server.getName(), server.getType(), server.getTask());
        } catch (IOException e) {
            e.printStackTrace();
            return new ServerCreationResult.Fail("no server template found");
        }

        if (copyWorlds) {
            try {
                this.copyServerWorlds(server.getName(), server.getType(), server.getTask());
            } catch (IOException e) {
                e.printStackTrace();
                return new ServerCreationResult.Fail("no worlds found");
            }
        }

        try {
            this.generateConfigurations(server);
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
            return new ServerCreationResult.Fail("failed to generate config files");
        }

        if (syncPlayerData) {
            try {
                this.syncPlayerData(server.getName(), server.getType(), server.getTask());
            } catch (IOException e) {
                e.printStackTrace();
                return new ServerCreationResult.Fail("failed to sync player data");
            }
        }

        try {
            this.syncLogs(server.getName(), server.getType(), server.getTask());
        } catch (IOException e) {
            e.printStackTrace();
            return new ServerCreationResult.Fail("failed to sync logs");
        }

        return new ServerCreationResult.Successful(
                this.networkPath.resolve(SERVERS).resolve(server.getName()));
    }

    @Override
    public void generateConfigurations(NetworkServer server) throws IOException, TemplateException {
        Template serverProperties = cfg.getTemplate("server.properties.ftl");
        Template paperGlobal = cfg.getTemplate("paper-global.yml.ftl");
        Template spigot = cfg.getTemplate("spigot.yml.ftl");
        Template bukkit = cfg.getTemplate("bukkit.yml.ftl");

        Map<String, Object> rootServerProperties = new HashMap<>();
        Map<String, Object> rootPaperGlobal = new HashMap<>();
        Map<String, Object> rootSpigot = new HashMap<>();
        Map<String, Object> rootBukkit = new HashMap<>();

        rootServerProperties.put("server", server);
        rootPaperGlobal.put("server", server);
        rootSpigot.put("server", server);
        rootBukkit.put("server", server);

        Writer outServerProperties = new OutputStreamWriter(
                new FileOutputStream(this.networkPath.resolve(SERVERS)
                        .resolve(server.getName()).resolve("server.properties").toFile()));
        Files.createDirectories(
                this.networkPath.resolve(SERVERS).resolve(server.getName()).resolve("config"));
        Writer outPaperGlobal = new OutputStreamWriter(
                new FileOutputStream(this.networkPath.resolve(SERVERS)
                        .resolve(server.getName()).resolve("config").resolve("paper-global.yml")
                        .toFile()));
        Writer outSpigot = new OutputStreamWriter(
                new FileOutputStream(this.networkPath.resolve(SERVERS)
                        .resolve(server.getName()).resolve("spigot.yml").toFile()));
        Writer outBukkit = new OutputStreamWriter(
                new FileOutputStream(this.networkPath.resolve(SERVERS)
                        .resolve(server.getName()).resolve("bukkit.yml").toFile()));

        serverProperties.process(rootServerProperties, outServerProperties);
        paperGlobal.process(rootPaperGlobal, outPaperGlobal);
        spigot.process(rootSpigot, outSpigot);
        bukkit.process(rootBukkit, outBukkit);

        outServerProperties.close();
        outPaperGlobal.close();
        outSpigot.close();
        outBukkit.close();
    }

    @Override
    public void copyServerBasis(String serverName, Type.Server<?> type, String task)
            throws IOException {
        Path dest = this.networkPath.resolve(SERVERS).resolve(serverName);
        this.copyServerFromTemplate(type, task, dest);
    }

    @Override
    public void copyServerWorlds(String name, Type.Server<?> type, String task) throws IOException {
        Path src = this.worldsTemplatePath.resolve(type.getShortName());
        Path dest = this.networkPath.resolve(SERVERS).resolve(name);

        if (task != null) {
            src = src.resolve(task);
        }

        FileUtils.copyDirectory(src.toFile(), dest.toFile());
    }

    @Override
    public void syncLogs(String name, Type.Server<?> type, String task) throws IOException {
        Path src = this.logsPath.resolve(type.getShortName());

        if (task != null) {
            src = src.resolve(task);
        }

        src = src.resolve(name).resolve(DATE_FORMAT.format(new Date()));

        Path dest = this.networkPath.resolve(SERVERS).resolve(name).resolve("logs");

        if (dest.toFile().exists()) {
            dest.toFile().delete();
        }

        Files.createDirectories(src);
        FileUtils.createParentDirectories(dest.toFile());
        Files.createSymbolicLink(dest, src);
    }

    @Override
    public void syncPlayerData(String name, Type.Server<?> type, String task) throws IOException {
        Path src = this.resolveTemplatePath(this.playersTemplatePath, type, task,
                DEFAULT_DIRECTORY);

        src = src.resolve(PLAYER_DATA);
        Path dest = this.networkPath.resolve(SERVERS).resolve(name).resolve("world")
                .resolve("playerdata");

        if (dest.toFile().exists()) {
            dest.toFile().delete();
        }

        FileUtils.createParentDirectories(dest.toFile());
        Files.createSymbolicLink(dest, src);
    }

    @Override
    public WorldSyncResult syncWorld(NetworkServer server, String worldName) {

        String name = server.getName();
        Type.Server<?> type = server.getType();
        String task = server.getTask();

        Path src = this.resolveTemplatePath(this.worldsTemplatePath, type, task, DEFAULT_DIRECTORY);

        src = src.resolve(worldName);
        Path dest = this.networkPath.resolve(SERVERS).resolve(name).resolve(worldName);

        if (dest.toFile().exists()) {
            try {
                FileUtils.delete(dest.toFile());
            } catch (IOException e) {
                e.printStackTrace();
                return new WorldSyncResult.Fail("failed to delete old world file");
            }
        }

        try {
            Files.createSymbolicLink(dest, src);
        } catch (IOException e) {
            e.printStackTrace();
            return new WorldSyncResult.Fail("failed to create world link");
        }

        return new WorldSyncResult.Successful(dest);
    }

    @Override
    public WorldSyncResult exportAndSyncWorld(String serverName, String worldName,
            Path exportPath) {

        Path src = this.networkPath.resolve(SERVERS).resolve(serverName).resolve(worldName);
        Path dest = this.worldsTemplatePath.resolve(exportPath).resolve(worldName);

        try {
            FileUtils.copyDirectory(src.toFile(), dest.toFile());
            FileUtils.deleteDirectory(src.toFile());
        } catch (IOException e) {
            e.printStackTrace();
            return new WorldSyncResult.Fail("failed to export world");
        }

        try {
            Files.createSymbolicLink(src, dest);
        } catch (IOException e) {
            e.printStackTrace();
            return new WorldSyncResult.Fail("failed to create world link");
        }

        return new WorldSyncResult.Successful(dest);
    }

    @Override
    public List<String> getWorldNames(Type.Server<?> type, String task) {
        Path src = this.resolveTemplatePath(this.worldsTemplatePath, type, task, DEFAULT_DIRECTORY);
        return List.of(src.toFile().list());
    }

    @Override
    public ServerCreationResult createPublicPlayerServer(NetworkServer server) {
        return this.createPlayerServer(PUBLIC_DIRECTORY, server);
    }

    @Override
    public ServerCreationResult createPlayerServer(UUID uuid, NetworkServer server) {
        return this.createPlayerServer(uuid.toString(), server);
    }

    private ServerCreationResult createPlayerServer(String owner, NetworkServer server) {
        Path dest = this.networkPath.resolve(SERVERS).resolve(server.getName());

        try {
            Files.createDirectories(dest);
            Path src = this.serverTemplatePath.resolve(server.getType().getShortName())
                    .resolve(server.getTask())
                    .resolve(owner).resolve(server.getFolderName());
            this.createSymLinks(src, dest);
        } catch (IOException e) {
            e.printStackTrace();
            return new ServerCreationResult.Fail("failed to create player-file links");
        }

        try {
            this.copyServerBasis(server.getName(), server.getType(), server.getTask());
        } catch (IOException e) {
            e.printStackTrace();
            return new ServerCreationResult.Fail("no server template found");
        }

        try {
            this.generateConfigurations(server);
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
            return new ServerCreationResult.Fail("failed to generate config files");
        }

        try {
            this.syncPlayerData(owner + "_" + server.getName(), server.getType(), server.getTask());
        } catch (IOException e) {
            return new ServerCreationResult.Fail("failed to sync logs");
        }

        return new ServerCreationResult.Successful(dest);
    }

    @Override
    public ServerInitResult initPublicPlayerServer(Type.Server<?> type, String task, String name) {
        return this.initPlayerServer(DEFAULT_DIRECTORY, type, task, name);
    }

    @Override
    public ServerInitResult initPlayerServer(UUID uuid, Type.Server<?> type, String task,
            String name) {
        Path dest = this.serverTemplatePath.resolve(type.getShortName()).resolve(task)
                .resolve(uuid.toString()).resolve(name);

        ServerInitResult result = this.initPlayerServer(uuid.toString(), type, task, name);

        if (!result.isSuccessful()) {
            return result;
        }

        File infoFile = new File(dest.toFile(), OWN_SERVER_INFO_FILE_NAME);

        if (!infoFile.exists()) {
            try {
                infoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return new ServerInitResult.Fail("failed to create server info file");
            }
        }

        TomlWriter tomlWriter = new TomlWriter();

        try {
            tomlWriter.write(Map.of(OWN_SERVER_OWNER_UUID, uuid.toString()), infoFile);
        } catch (IOException e) {
            e.printStackTrace();
            return new ServerInitResult.Fail("failed to write into server info file");
        }

        return new ServerInitResult.Successful(dest);
    }

    private ServerInitResult initPlayerServer(String owner, Type.Server<?> type, String task,
            String name) {
        Path dest = this.serverTemplatePath.resolve(type.getShortName()).resolve(task)
                .resolve(owner).resolve(name);

        try {
            this.copyServerFromPlayerTemplate(type, task, dest);
        } catch (IOException e) {
            e.printStackTrace();
            return new ServerInitResult.Fail("no server template found");
        }
        return new ServerInitResult.Successful(dest);
    }

    @Override
    public List<String> getPublicPlayerServerNames(Type.Server<?> type, String task) {
        return this.getPlayerServerNames(PUBLIC_DIRECTORY, type, task);
    }

    @Override
    public List<String> getOwnerServerNames(UUID uuid, Type.Server<?> type, String task) {
        return this.getPlayerServerNames(uuid.toString(), type, task);
    }

    private List<String> getPlayerServerNames(String owner, Type.Server<?> type, String task) {
        try {
            Path src = this.serverTemplatePath.resolve(type.getShortName()).resolve(task)
                    .resolve(owner);
            String[] files = src.toFile().list();
            return files != null ? List.of(files) : List.of();
        } catch (InvalidPathException e) {
            return List.of();
        }
    }

    @Override
    public Map<UUID, List<String>> getMemberServerNames(UUID member, Type.Server<?> type,
            String task) {
        Path src = this.serverTemplatePath.resolve(type.getShortName()).resolve(task);

        HashMap<UUID, List<String>> serverNamesByOwnerUuid = new HashMap<>();

        for (Map.Entry<UUID, List<String>> entry : this.getAllPlayerServerNames(type, task)
                .entrySet()) {
            UUID uuid = entry.getKey();
            for (String serverName : entry.getValue()) {
                Toml toml;
                try {
                    toml = new Toml().read(src.resolve(uuid.toString()).resolve(serverName)
                            .resolve(OWN_SERVER_INFO_FILE_NAME).toFile());
                } catch (IllegalStateException e) {
                    continue;
                }

                List<String> memberUuidStrings = toml.getList(OWN_SERVER_MEMBER_UUIDS);

                if (memberUuidStrings != null && memberUuidStrings.contains(member.toString())) {
                    serverNamesByOwnerUuid.computeIfAbsent(uuid, uuid1 -> new LinkedList<>())
                            .add(serverName);
                }
            }
        }

        return serverNamesByOwnerUuid;
    }

    private Map<UUID, List<String>> getAllPlayerServerNames(Type.Server<?> type, String task) {
        Path src = this.serverTemplatePath.resolve(type.getShortName()).resolve(task);

        String[] playerFiles = src.toFile().list();
        if (playerFiles == null) {
            return Map.of();
        }

        HashMap<UUID, List<String>> serverNamesByOwnerUuid = new HashMap<>();

        for (String playerFile : playerFiles) {
            UUID uuid;
            try {
                uuid = UUID.fromString(playerFile);
            } catch (IllegalArgumentException e) {
                continue;
            }

            Path playerPath = src.resolve(playerFile);

            String[] serverNames = playerPath.toFile().list();
            if (serverNames == null) {
                continue;
            }

            serverNamesByOwnerUuid.put(uuid, List.of(serverNames));
        }

        return serverNamesByOwnerUuid;
    }


    @Override
    public List<UUID> getPlayerServerMembers(UUID uuid, Type.Server<?> type, String task,
            String exactName) {
        File config = this.serverTemplatePath.resolve(type.getShortName()).resolve(task)
                .resolve(uuid.toString()).resolve(exactName).resolve(OWN_SERVER_INFO_FILE_NAME)
                .toFile();

        if (!config.exists()) {
            return List.of();
        }

        Toml toml;
        try {
            toml = new Toml().read(config);
        } catch (IllegalStateException e) {
            return List.of();
        }

        List<String> memberUuidStrings = toml.getList(OWN_SERVER_MEMBER_UUIDS);
        return memberUuidStrings != null ? memberUuidStrings.stream().map(UUID::fromString).toList()
                : List.of();
    }

    @Override
    public boolean setPlayerServerMembers(UUID uuid, Type.Server<?> type, String task,
            String exactName, List<UUID> memberUuids) {
        Path path;
        try {
            path = this.serverTemplatePath.resolve(type.getShortName()).resolve(task)
                    .resolve(uuid.toString()).resolve(exactName);
        } catch (InvalidPathException e) {
            e.printStackTrace();
            return false;
        }
        File infoFile = new File(path.toFile(), OWN_SERVER_INFO_FILE_NAME);

        if (!infoFile.exists()) {
            try {
                infoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        TomlWriter tomlWriter = new TomlWriter();

        try {
            tomlWriter.write(Map.of(OWN_SERVER_MEMBER_UUIDS,
                    memberUuids.stream().map(UUID::toString).toList()), infoFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void copyServerFromTemplate(Type.Server<?> type, String task, Path dest)
            throws IOException {
        Path src = this.serverTemplatePath.toAbsolutePath();

        // from base
        FileUtils.copyDirectory(src.resolve(BASIS_DIRECTORY).toFile(), dest.toFile());

        if (src.resolve(type.getShortName()).toFile().exists()) {
            src = src.resolve(type.getShortName());
            // from type base
            if (src.resolve(BASIS_DIRECTORY).toFile().exists()) {
                FileUtils.copyDirectory(src.resolve(BASIS_DIRECTORY).toFile(), dest.toFile());
            }

            if (task != null && src.resolve(task).toFile().exists()) {
                src = src.resolve(task);
                if (src.resolve(DEFAULT_DIRECTORY).toFile().exists()) {
                    src = src.resolve(DEFAULT_DIRECTORY);
                }
            } else if (src.resolve(DEFAULT_DIRECTORY).toFile().exists()) {
                src = src.resolve(DEFAULT_DIRECTORY);
            } else {
                src = this.serverTemplatePath.resolve(DEFAULT_DIRECTORY);
            }
        } else {
            src = src.resolve(DEFAULT_DIRECTORY);
        }

        FileUtils.copyDirectory(src.toFile(), dest.toFile());
    }

    private void copyServerFromPlayerTemplate(Type.Server<?> type, String task, Path dest)
            throws IOException {
        Path src = this.resolveTemplatePath(this.serverTemplatePath, type, task,
                DEFAULT_PLAYER_DIRECTORY);
        FileUtils.copyDirectory(src.toFile(), dest.toFile());
    }

    private void createSymLinks(Path src, Path dest) throws IOException {
        String[] fileNames = src.toFile().list();
        this.createSymLinks(src, fileNames != null ? List.of(fileNames) : List.of(), dest);
    }

    private void createSymLinks(Path src, List<String> files, Path dest) throws IOException {
        for (String fileName : files) {
            Files.createSymbolicLink(dest.resolve(fileName), src.resolve(fileName));
        }
    }

    private Path resolveTemplatePath(Path base, Type.Server<?> type, String task,
            String defaultDir) {
        Path path = base.toAbsolutePath();
        if (path.resolve(type.getShortName()).toFile().exists()) {
            path = path.resolve(type.getShortName());
            if (task != null && path.resolve(task).toFile().exists()) {
                path = path.resolve(task);
                if (path.resolve(defaultDir).toFile().exists()) {
                    path = path.resolve(defaultDir);
                }
            } else if (path.resolve(defaultDir).toFile().exists()) {
                path = path.resolve(defaultDir);
            } else {
                path = base.resolve(defaultDir);
            }
        } else {
            path = path.resolve(defaultDir);
        }
        return path;
    }
}
