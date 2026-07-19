# Selective Peaceful Mobs

Selective Peaceful Mobs is a small NeoForge server-side mod for Minecraft 1.21.1 that lets each player choose whether hostile mobs should behave peacefully toward them unless provoked.

By default, mobs are peaceful unless provoked, but each player can turn that off for themselves. Admins can manage the setting for other players.

## Features

- Per-player mob hostility.
- New players default to peaceful mobs, unless changed in the config.
- Players can toggle only themselves.
- Admins/op players can toggle anyone.
- Hostile mobs ignore peaceful players.
- If a peaceful player attacks a hostile mob, that mob becomes hostile toward that player temporarily.
- Optional group aggro so nearby mobs of the same type also retaliate.
- Player choices are saved in the world.
- Works automatically with most vanilla and modded hostile mobs by checking `Enemy` and `MobCategory.MONSTER`.
- Server-side only in concept. Clients should not need the mod, but test this with your exact modpack before publishing.

## Commands

### Player commands

```mcfunction
/peacefulmobs on
/peacefulmobs off
/peacefulmobs status
/peacefulmobs reset
```

`/peacefulmobs on` means hostile mobs ignore you unless you attack them.

`/peacefulmobs off` means hostile mobs treat you normally, like vanilla Minecraft.

`/peacefulmobs reset` removes your personal choice and returns you to the server default.

### Admin commands

Requires permission level 2 or higher.

```mcfunction
/peacefulmobs set <player> <true|false>
/peacefulmobs check <player>
/peacefulmobs reset <player>
```

Examples:

```mcfunction
/peacefulmobs set Steve false
/peacefulmobs set Alex true
/peacefulmobs check Steve
/peacefulmobs reset Alex
```

## Config

The generated server config appears at `config/selective-peaceful-mobs-server.toml`. In the development run it is under `runs/server/config/`; a world can also provide overrides from its `serverconfig/` folder.

When upgrading from a pre-1.0.0 build, copy any customized values from `selectivepeacefulmobs-server.toml` into the new file while the server is stopped.

```toml
[general]
# If true, players who have not chosen yet start with peaceful mobs enabled.
defaultPeaceful = true

# If true, player choices are saved in the world.
# If false, the defaultPeaceful value is always used and set/on/off commands are rejected.
rememberPlayerChoice = true

# How long mobs remain angry at a peaceful player after that player attacks them.
provokedTimeSeconds = 30

# If true, nearby hostile mobs of the same type also become provoked.
groupAggro = true

# Radius in blocks used for group aggro.
groupAggroRadius = 16

# If true, creative and spectator players are always ignored by hostile mobs.
ignoreCreativeAndSpectator = true

[alwayshostilemobs]
# Entity ids for mobs that can always target peaceful players.
# Add vanilla or modded entity ids here, for example "minecraft:wither".
mobs = ["minecraft:ender_dragon", "minecraft:warden", "minecraft:wither"]
```

## Intended behavior

| Player setting | Mob behavior |
| --- | --- |
| Peaceful ON | Hostile mobs ignore the player unless provoked. |
| Peaceful OFF | Hostile mobs attack normally. |
| No saved choice | Uses `defaultPeaceful` from the config. |

## Provocation behavior

A mob becomes provoked when a peaceful player attacks it.

For example:

1. Nick has peaceful mobs ON.
2. A zombie ignores Nick.
3. Nick attacks the zombie.
4. The zombie targets Nick for `provokedTimeSeconds`.
5. After the timer expires, the zombie can go back to ignoring Nick.

If `groupAggro` is enabled, nearby mobs of the same entity type also become provoked.

## Project setup

This project targets:

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21
- Gradle through the NeoForge MDK/userdev setup

NeoForge recommends Java 21 for modern Minecraft modding. The mod filters protected players during normal combat target selection, uses NeoForge target events as a backstop, and clears existing targets when protection becomes active again.

## Build

From the project folder:

```bash
./gradlew build
```

On Windows:

```bat
gradlew.bat build
```

The built jar should appear in:

```text
build/libs/
```

To launch a development client with the mod loaded:

```bat
gradlew.bat runClient
```

To launch a dedicated development server:

```bat
gradlew.bat runServer
```

The development server files are written under `runs/server/`. A normal dedicated-server installation still requires accepting Mojang's EULA in the usual way.

## In-game verification checklist

Use two players for the mixed-setting check where possible.

1. Join with a client that does not have the mod and run `/peacefulmobs status`.
2. Leave one player ON, set another player OFF, and confirm a nearby hostile mob skips the ON player but can target the OFF player.
3. While ON, attack a hostile mob directly and with a projectile. Confirm it retaliates, then stops targeting after `provokedTimeSeconds` even if the player stays nearby.
4. With `groupAggro` enabled, attack one mob in a same-type group and confirm only same-type mobs inside `groupAggroRadius` retaliate.
5. Run each player command, then confirm a non-op cannot use `set`, `check`, or `reset <player>`. Confirm an op can use all three admin forms.
6. Set a choice in the Overworld, change dimensions, restart the server, and confirm the choice remains unchanged.
7. Set `rememberPlayerChoice=false`; confirm status uses the server default and the on/off/set commands explain that player choices are disabled.
8. Confirm configured `alwayshostilemobs` still target peaceful players, and test at least one representative hostile mob from the intended modpack.

## Compatibility and testing notes

The jar is built for Minecraft 1.21.1 and NeoForge 21.1.231 or newer within the 21.1 line. It contains no client-only code or custom network payloads, so clients are not expected to need the mod.

Test these areas with the exact modpack before publishing:

- Whether modded mobs use normal `Mob#setTarget` targeting.
- Whether special mobs use custom attack logic that bypasses normal targeting.
- Which mobs should be listed in `alwayshostilemobs` for your pack.
- Whether group aggro feels fair.
- Whether clients can connect without installing the mod. It should be server-side in concept because it adds no blocks/items/network packets, but every pack should be tested.

## Suggested future additions

- LuckPerms permission support.
- More detailed mob allow/deny list rules.
- Per-dimension rules.
- Per-team rules.
- A cooldown before players can toggle again.
- A message/actionbar warning when a mob becomes provoked.
- Optional retaliation from nearby mobs of the same category instead of exact same type.

## License

MIT, unless you choose another license.
