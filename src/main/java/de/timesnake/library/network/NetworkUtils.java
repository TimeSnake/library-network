/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.network;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import de.timesnake.database.util.object.Type;
import de.timesnake.database.util.object.Type.Server;
import de.timesnake.library.network.NetworkServer.CopyType;
import de.timesnake.library.network.NetworkServer.Options;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;

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
  public ServerCreationResult createServer(NetworkServer server) {
    Options options = server.getOptions();

    try {
      this.copyServerFromTemplate(server);
    } catch (IOException e) {
      e.printStackTrace();
      return new ServerCreationResult.Fail("no server template found");
    }

    if (options.getWorldCopyType() == CopyType.COPY) {
      try {
        this.copyServerWorlds(server);
      } catch (IOException e) {
        e.printStackTrace();
        return new ServerCreationResult.Fail("failed to copy worlds");
      }
    } else if (options.getWorldCopyType() == CopyType.SYNC) {
      try {
        this.syncServerWorlds(server);
      } catch (IOException e) {
        e.printStackTrace();
        return new ServerCreationResult.Fail("failed to sync worlds");
      }
    }

    try {
      this.generateConfigurations(server);
    } catch (IOException | TemplateException e) {
      e.printStackTrace();
      return new ServerCreationResult.Fail("failed to generate config files");
    }

    if (options.isSyncPlayerData()) {
      // TODO fix for 1.20.1
      // try {
      //   this.syncPlayerData(server);
      // } catch (IOException e) {
      //   e.printStackTrace();
      //   return new ServerCreationResult.Fail("failed to sync player data");
      // }
    }

    if (options.isSyncLogs()) {
      try {
        this.syncLogs(server);
      } catch (IOException e) {
        e.printStackTrace();
        return new ServerCreationResult.Fail("failed to sync logs");
      }
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
    Template channel = cfg.getTemplate("channel_config.toml.ftl");

    Map<String, Object> rootServerProperties = new HashMap<>();
    Map<String, Object> rootPaperGlobal = new HashMap<>();
    Map<String, Object> rootSpigot = new HashMap<>();
    Map<String, Object> rootBukkit = new HashMap<>();
    Map<String, Object> rootChannel = new HashMap<>();

    rootServerProperties.put("server", server);
    rootPaperGlobal.put("server", server);
    rootSpigot.put("server", server);
    rootBukkit.put("server", server);
    rootChannel.put("server", server);

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

    Writer outChannel = new OutputStreamWriter(
        new FileOutputStream(this.networkPath.resolve(SERVERS)
            .resolve(server.getName()).resolve("plugins").resolve("channel").resolve("config.toml").toFile()));

    serverProperties.process(rootServerProperties, outServerProperties);
    paperGlobal.process(rootPaperGlobal, outPaperGlobal);
    spigot.process(rootSpigot, outSpigot);
    bukkit.process(rootBukkit, outBukkit);
    channel.process(rootChannel, outChannel);

    outServerProperties.close();
    outPaperGlobal.close();
    outSpigot.close();
    outBukkit.close();
    outChannel.close();
  }

  @Override
  public void copyServerWorlds(NetworkServerInfo info) throws IOException {
    Path src = this.worldsTemplatePath.resolve(info.getType().getShortName());
    Path dest = this.networkPath.resolve(SERVERS).resolve(info.getFolderName());

    if (info.getTask() != null) {
      src = src.resolve(info.getTask());
    }

    FileUtils.copyDirectory(src.toFile(), dest.toFile());
  }

  @Override
  public void syncServerWorlds(NetworkServerInfo info) throws IOException {
    for (String worldName : this.getWorldNames(info.getType(), info.getTask())) {
      this.syncWorld(info, worldName);
    }
  }

  @Override
  public void syncLogs(NetworkServerInfo info) throws IOException {
    Path src = this.logsPath.resolve(info.getType().getShortName());

    if (info.getTask() != null) {
      src = src.resolve(info.getTask());
    }

    src = src.resolve(info.getName()).resolve(DATE_FORMAT.format(new Date()));

    Path dest = this.networkPath.resolve(SERVERS).resolve(info.getFolderName()).resolve("logs");

    if (dest.toFile().exists()) {
      dest.toFile().delete();
    }

    Files.createDirectories(src);
    FileUtils.createParentDirectories(dest.toFile());
    Files.createSymbolicLink(dest, src);
  }

  @Override
  public void syncPlayerData(NetworkServerInfo info) throws IOException {
    Path src = NetworkFileUtils.resolveTemplatePath(this.playersTemplatePath, info.getType(),
        info.getTask(),
        DEFAULT_DIRECTORY);

    src = src.resolve(PLAYER_DATA);
    Path dest = this.networkPath.resolve(SERVERS).resolve(info.getFolderName()).resolve("world")
        .resolve("playerdata");

    if (dest.toFile().exists()) {
      dest.toFile().delete();
    }

    FileUtils.createParentDirectories(dest.toFile());
    Files.createSymbolicLink(dest, src);
  }

  @Override
  public WorldSyncResult syncWorld(NetworkServerInfo server, String worldName) {

    String name = server.getName();
    Type.Server<?> type = server.getType();
    String task = server.getTask();

    Path src = NetworkFileUtils.resolveWorldTemplatePath(this.worldsTemplatePath, type, task);

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
  public List<String> getWorldNames(Server<?> type, String task) {
    Path src = NetworkFileUtils.resolveWorldTemplatePath(this.worldsTemplatePath, type, task);
    String[] names = src.toFile().list();
    return names != null ? Arrays.asList(names) : List.of();
  }

  @Override
  public List<File> getWorldFiles(Server<?> type, String task) {
    Path src = NetworkFileUtils.resolveWorldTemplatePath(this.worldsTemplatePath, type, task);
    File[] files = src.toFile().listFiles();
    return files != null ? Arrays.asList(files) : List.of();
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
      Path src = this.serverTemplatePath
          .resolve(server.getType().getShortName())
          .resolve(server.getTask())
          .resolve(owner)
          .resolve(server.getFolderName());
      NetworkFileUtils.createSymLinks(src, dest);
    } catch (IOException e) {
      e.printStackTrace();
      return new ServerCreationResult.Fail("failed to link server files");
    }

    try {
      this.copyServerFromTemplate(server);
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
      this.syncLogs(server);
    } catch (IOException e) {
      e.printStackTrace();
      return new ServerCreationResult.Fail("failed to sync logs");
    }

    return new ServerCreationResult.Successful(dest);
  }

  @Override
  public ServerInitResult initNewPublicPlayerServer(Type.Server<?> type, String task,
                                                    String name) {
    return this.initNewPlayerServer(DEFAULT_DIRECTORY, type, task, name);
  }

  @Override
  public ServerInitResult initNewPlayerServer(UUID uuid, Type.Server<?> type, String task,
                                              String name) {
    Path dest = this.serverTemplatePath.resolve(type.getShortName()).resolve(task)
        .resolve(uuid.toString()).resolve(name);

    ServerInitResult result = this.initNewPlayerServer(uuid.toString(), type, task, name);

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

  private ServerInitResult initNewPlayerServer(String owner, Type.Server<?> type, String task,
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

  @Override
  public void copyServerFromTemplate(NetworkServerInfo info)
      throws IOException {
    Path dest = this.networkPath.resolve(SERVERS).resolve(info.getFolderName());
    this.copyServerFromTemplate(info.getType(), info.getTask(), dest);
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
    Path src = NetworkFileUtils.resolveTemplatePath(this.serverTemplatePath, type, task,
        DEFAULT_PLAYER_DIRECTORY);
    FileUtils.copyDirectory(src.toFile(), dest.toFile());
  }

}
