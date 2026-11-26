package dev.dash.dmentions.config;

import dev.yoshi.xconfig.XConfig;
import dev.yoshi.xconfig.XConfigBuilder;

import java.nio.file.Path;

public class LangConfigFactory {

    public static final int VERSION = 1;

    public static XConfigBuilder create(Path langFile) {
        return XConfig.builder(langFile).version(VERSION).keys(LangKeys.class);
    }
}
