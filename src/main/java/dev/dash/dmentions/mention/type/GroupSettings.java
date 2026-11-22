package dev.dash.dmentions.mention.type;

import dev.yoshi.xconfig.ConfigException;
import dev.yoshi.xconfig.TypeAdapter;
import dev.yoshi.xconfig.adapters.DurationAdapter;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public record GroupSettings(@Nullable String display, @Nullable String sound, @Nullable Duration cooldown) {

    public static final TypeAdapter<GroupSettings> ADAPTER = new TypeAdapter<>() {
        @Override
        public GroupSettings deserialize(Object raw, String path) {
            if (!(raw instanceof Map<?,?> map)) {
                throw new ConfigException("Expected a map at path: " + path);
            }

            try {
                String display = (String) map.get("display");
                String sound = (String) map.get("sound");
                Object cooldownObj = map.get("cooldown");
                Duration cooldown = cooldownObj != null ? DurationAdapter.INSTANCE.deserialize(cooldownObj, path + ".cooldown") : null;

                return new GroupSettings(display, sound, cooldown);
            } catch (ClassCastException e) {
                throw new ConfigException("Expected a map with string key at path: " + path, e);
            }
        }

        @Override
        public Object serialize(GroupSettings value) {
            Map<String, Object> map = new LinkedHashMap<>();
            if (value.display != null) {
                map.put("display", value.display);
            }
            if (value.sound != null) {
                map.put("sound", value.sound);
            }
            if (value.cooldown != null) {
                map.put("cooldown", DurationAdapter.INSTANCE.serialize(value.cooldown));
            }
            return map;
        }
    };
}