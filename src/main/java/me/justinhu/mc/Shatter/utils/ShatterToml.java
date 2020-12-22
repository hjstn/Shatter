package me.justinhu.mc.Shatter.utils;

import com.moandjiezana.toml.Toml;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ShatterToml {
    private final Path pluginFolder;
    private final Logger logger;

    public ShatterToml(Path pluginFolder, Logger logger) {
        this.pluginFolder = pluginFolder;
        this.logger = logger;
    }

    public Toml loadConfig() {
        try {
            logger.info("Loading config file.");
            return loadToml("config.toml");
        } catch (IOException e) {
            logger.error("An exception occurred while loading config file.");
            e.printStackTrace();
        }

        return new Toml();
    }

    public Toml loadToml(String tomlFile) throws IOException {
        File file = new File(pluginFolder.toFile(), tomlFile);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            InputStream input = getClass().getResourceAsStream("/" + tomlFile);

            if (input != null) Files.copy(input, file.toPath());
            else file.createNewFile();
        }

        return new Toml().read(file);
    }
}
