package de.timesnake.library.network;

import de.timesnake.database.util.object.Type;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.io.FileUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkUtils implements Network {

    public static Network getInstance() {
        return instance;
    }

    private static Network instance;
    private final Path networkPath;
    private final Path serverTemplatePath;
    private final Path worldsTemplatePath;
    private final Path playersTemplatePath;

    private Configuration cfg;

    public NetworkUtils(Path networkPath) {
        instance = this;
        this.networkPath = networkPath;
        this.serverTemplatePath = this.networkPath.resolve(TEMPLATE_DIR_NAME).resolve(SERVERS_TEMPLATE_NAME);
        this.worldsTemplatePath = this.networkPath.resolve(TEMPLATE_DIR_NAME).resolve(WORLDS_TEMPLATE_NAME);
        this.playersTemplatePath = this.networkPath.resolve(TEMPLATE_DIR_NAME).resolve(PLAYERS_TEMPLATE_NAME);

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
    public ServerCreationResult createServer(NetworkServer server, boolean copyWorlds, boolean syncPlayerData) {
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
                return new ServerCreationResult.Fail("failed sync player data");
            }

        }

        return new ServerCreationResult.Successful(this.networkPath.resolve(SERVERS).resolve(server.getName()));
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

        Writer outServerProperties = new OutputStreamWriter(new FileOutputStream(this.networkPath.resolve(SERVERS)
                .resolve(server.getName()).resolve("server.properties").toFile()));
        Writer outPaperGlobal = new OutputStreamWriter(new FileOutputStream(this.networkPath.resolve(SERVERS)
                .resolve(server.getName()).resolve("config").resolve("paper-global.yml").toFile()));
        Writer outSpigot = new OutputStreamWriter(new FileOutputStream(this.networkPath.resolve(SERVERS)
                .resolve(server.getName()).resolve("spigot.yml").toFile()));
        Writer outBukkit = new OutputStreamWriter(new FileOutputStream(this.networkPath.resolve(SERVERS)
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
    public void copyServerBasis(String name, Type.Server<?> type, String task) throws IOException {
        Path src = this.serverTemplatePath.resolve(type.getDatabaseValue());

        if (src.toFile().exists()) {
            if (task != null && src.resolve(task).toFile().exists()) {
                src = src.resolve(task);
            } else if (src.resolve(DEFAULT_DIRECTORY).toFile().exists()) {
                src = src.resolve(DEFAULT_DIRECTORY);
            } else {
                src = this.serverTemplatePath.resolve(DEFAULT_DIRECTORY);
            }
        } else {
            src = this.serverTemplatePath.resolve(DEFAULT_DIRECTORY);
        }
        Path dest = this.networkPath.resolve(SERVERS).resolve(name);

        FileUtils.copyDirectory(src.toFile(), dest.toFile());
    }

    @Override
    public void copyServerWorlds(String name, Type.Server<?> type, String task) throws IOException {
        Path src = this.worldsTemplatePath.resolve(type.getDatabaseValue());
        Path dest = this.networkPath.resolve(SERVERS).resolve(name);

        if (task != null) {
            src = src.resolve(task);
        }

        FileUtils.copyDirectory(src.toFile(), dest.toFile());
    }

    @Override
    public void syncPlayerData(String name, Type.Server<?> type, String task) throws IOException {
        Path src = this.playersTemplatePath.resolve(type.getDatabaseValue());

        if (src.toFile().exists()) {
            if (task != null && src.resolve(task).toFile().exists()) {
                src = src.resolve(task);
            } else if (src.resolve(DEFAULT_DIRECTORY).toFile().exists()) {
                src = src.resolve(DEFAULT_DIRECTORY);
            } else {
                src = this.playersTemplatePath.resolve(DEFAULT_DIRECTORY);
            }
        } else {
            src = this.playersTemplatePath.resolve(DEFAULT_DIRECTORY);
        }
        src = src.resolve(PLAYER_DATA);
        Path dest = this.networkPath.resolve(SERVERS).resolve(name).resolve("world").resolve("playerdata");

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

        Path src = this.worldsTemplatePath.resolve(type.getDatabaseValue());

        if (src.toFile().exists()) {
            if (task != null && src.resolve(task).toFile().exists()) {
                src = src.resolve(task);
            } else if (src.resolve(DEFAULT_DIRECTORY).toFile().exists()) {
                src = src.resolve(DEFAULT_DIRECTORY);
            } else {
                src = this.worldsTemplatePath.resolve(DEFAULT_DIRECTORY);
            }
        } else {
            src = this.worldsTemplatePath.resolve(DEFAULT_DIRECTORY);
        }

        src = src.resolve(worldName);
        Path dest = this.networkPath.resolve(SERVERS).resolve(name).resolve(worldName);

        try {
            Files.createSymbolicLink(dest, src);
        } catch (IOException e) {
            e.printStackTrace();
            return new WorldSyncResult.Fail("failed to create world link");
        }

        return new WorldSyncResult.Successful(dest);
    }

    @Override
    public WorldSyncResult exportAndSyncWorld(String serverName, String worldName, Path exportPath) {

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
        Path src = this.worldsTemplatePath.resolve(type.getDatabaseValue());

        if (src.toFile().exists()) {
            if (task != null && src.resolve(task).toFile().exists()) {
                src = src.resolve(task);
            } else if (src.resolve(DEFAULT_DIRECTORY).toFile().exists()) {
                src = src.resolve(DEFAULT_DIRECTORY);
            } else {
                src = this.worldsTemplatePath.resolve(DEFAULT_DIRECTORY);
            }
        } else {
            src = this.worldsTemplatePath.resolve(DEFAULT_DIRECTORY);
        }

        return List.of(src.toFile().list());
    }
}
