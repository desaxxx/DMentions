# DMentions
DMentions is a versatile chat plugin that allows you to mention players effortlessly\
Compatible with various chat plugins, it enhances your in-game communication with ease, even when they colour every letter of the message.

## Download Links
- [Modrinth](https://modrinth.com/plugin/dmentions)
- [BuiltByBit](https://builtbybit.com/resources/dmentions.57545/)
- [Spigot](https://www.spigotmc.org/resources/dmentions-%E2%9C%A8-advanced-player-mention.121452/) *(legacy: 1.8.x only, 2.0+ requires Paper)*

## Requirements
- [Paper](https://papermc.io/) 1.21+ or a fork of it. [Folia](https://papermc.io/software/folia) is supported.
- Java 21+

## Supported Languages
- English
- Turkish

> [!NOTE]
> You can create your own language file in `DMentions/lang/` folder. You can also share it with me!

## Features
- **Toggle System**: Enable or disable mentions quickly.
- **Permissions**: Manage who can use mention commands.
- **Notification Sounds**: Play sounds for mentions.
- **Custom Keywords & Display Texts**: Personalize your mentions with [MiniMessage](https://docs.papermc.io/adventure/minimessage/format).
- **Custom Names**: Players can choose the name shown when they are mentioned.
- **Mention Limits**: Control the number of mentions.
- **Restrict Player Mentions**: Restrict who can be mentioned.
- **Cooldowns**: Prevent spam with cooldown settings.
- **Notification Messages**: Informative messages for both sender and target (chat, action bar or title).
- **Disabled Worlds**: Stop mentioning at specified worlds.
- **Vanish, AFK & Ignore**: Respect vanished players, AFK players and ignore lists.
- **Storage**: Keep player data in YAML, SQLite or MySQL/MariaDB.
- **In-Game Configuring**: Configure settings directly in-game.

## Mention Types
- **Player** (Individual)
- **Nearby**
- **Everyone**
- **Group** (Members)

## Commands
- **/dms toggle** - Toggle mentionable mode
- **/dms send <keyword>** - Send a mention via command
- **/dms reload** - Reloads the config and language files
- **/dms config** - Opens configuring menu
- **/dms customize <name>** - Personalizes your mention display
- **/dms user <player> (...)** - User management for admins
- **/dms version** - Shows the plugin version
- **/dms help** - Command list of plugin

## Permissions
- **dmentions.command.<command>** - Use a command, e.g. `dmentions.command.toggle`
- **dmentions.admin** - Use every command\
~
- **dmentions.restricted** - Restrict your mentions
- **dmentions.restricted.bypass** - Mention restricted players
- **dmentions.cooldown.bypass** - Ignore mention cooldowns
- **dmentions.update.notify** - Get notified about new versions\
~
- **dmentions.mention.player** - Mention a player
- **dmentions.mention.nearby** - Mention nearby players
- **dmentions.mention.everyone** - Mention everyone
- **dmentions.mention.group.<group>** - Mention groups\
*(!) Mention permissions are editable in config file.*

## Plugin Dependence
No required dependence. Optionally:
- [LuckPerms](https://luckperms.net/) for group mentioning.
- [EssentialsX](https://essentialsx.net/) to respect AFK players and ignore lists.

Vanished players are detected automatically with EssentialsX, Staff++ and other vanish plugins.

## Installation
1- **Download** the <ins>latest</ins> version of the plugin from one of the links above.\
2- **Place** the plugin in the plugins folder of your server.\
3- **Restart** your server.\
4- **Enjoy** the enhanced chat experience! :>

> [!TIP]
> Upgrading from 1.8.4 or 1.8.5? Your config, language files and player data are converted on the first start. The old files are kept in `DMentions/backup-1.8/`. Coming from an older version? Update to 1.8.5 first.

## Documentation & Contact
**Wiki**: Check out the [Wiki](https://github.com/desaxxx/DMentions/wiki) for help configuring the plugin.\
**Contact**: Found a bug or have a suggestion? Contact me on Discord: **desaxx** or open an [issue](https://github.com/desaxxx/DMentions/issues).\
**Discord**: Join [Discord Server](https://discord.gg/dN6RUzZGgJ) to get support configuring the plugin.

## License
Copyright (c) 2024-2026 Desa

DMentions 2.0.0 and later is licensed under version 3 only of the [GNU Lesser General Public License](COPYING.LESSER) (`LGPL-3.0-only`), which builds on the [GNU General Public License v3.0](COPYING).
- Forks and modified versions of DMentions that are shared must keep their source code open under the same license.
- Plugins that only use DMentions' API do not have to be open source and can use any license.

Versions 1.8.4 and earlier were released under the MIT License.

## Contributing
Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) first: every pull request needs agreement to the [Contributor License Agreement](CLA.md).

## Images
![](https://i.imgur.com/820NYD9.png)
![](https://i.imgur.com/Mfs5lTp.png)
![](https://i.imgur.com/o32GVnW.png)
![](https://i.imgur.com/9nPvblf.png)
![](https://i.imgur.com/HJNJ1qz.png)
![](https://i.imgur.com/ZWb7ne8.png)
![](https://i.imgur.com/EiYUPji.png)

## Thanks to Our Users!
![](https://bstats.org/signatures/bukkit/DMentions.svg)
