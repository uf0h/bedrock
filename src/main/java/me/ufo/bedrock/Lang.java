package me.ufo.bedrock;

import java.io.File;
import java.io.IOException;
import me.ufo.architect.util.Style;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Lang {

  public static String NO_PERMISSION;
  public static String COMMAND_USAGE;
  public static String PLAYER_NOT_FOUND;
  public static String INVALID_INPUT;

  public Lang(final Bedrock plugin) {
    final File langFile = new File(plugin.getDataFolder().getPath() + "/lang.yml");
    if (!langFile.exists()) {
      plugin.saveResource("lang.yml", false);
    }

    // local variable to allow gc to clean up
    final YamlConfiguration config = new YamlConfiguration();

    try {
      config.load(langFile);
    } catch (final IOException | InvalidConfigurationException e) {
      e.printStackTrace();
    }

    NO_PERMISSION = Style.translate(
      config.getString(
        "no-permission",
        "&cYou do not have the required permission"
      )
    );

    COMMAND_USAGE = Style.translate(
      config.getString(
        "command-usage",
        "&f/{command}"
      )
    );

    PLAYER_NOT_FOUND = Style.translate(
      config.getString(
        "player-not-found",
        "&cPlayer not found."
      )
    );

    INVALID_INPUT = Style.translate(
      config.getString(
        "invalid-input",
        "&cThat is not a valid input."
      )
    );
  }

}
