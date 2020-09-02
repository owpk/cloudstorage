package org.owpk.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerConfig extends Config {
    private static final String CONFIG_NAME = "server.properties";
    private static final String DEFAULT_FOLDER = "./" + "clients_folders";
    private Path root;
    private static ServerConfig config;

    public static ServerConfig getConfig() {
        if (config == null)
            config = new ServerConfig();
        return config;
    }

    private ServerConfig() {
        super(CONFIG_NAME);
    }

    @Override
    public void load() {
        port = checkPort(properties.getProperty(ConfigParameters.PORT.getDescription(), null));
        String rootDir = properties.getProperty(ConfigParameters.SERVER_ROOT.getDescription(), DEFAULT_FOLDER);
        File f = new File(rootDir);
        if (!f.exists()) {
            f = new File(DEFAULT_FOLDER);
            f.mkdirs();
            writeProperty(ConfigParameters.SERVER_ROOT, f.getAbsolutePath());
        }
        root = f.toPath();
    }

    public Path getRoot() {
        return root;
    }

    public int getPort() {
        return port;
    }
}
