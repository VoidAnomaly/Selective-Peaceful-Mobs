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

1. VoidAnomaly has peaceful mobs ON.
2. A zombie ignores VoidAnomaly.
3. VoidAnomaly attacks the zombie.
4. The zombie targets VoidAnomaly for `provokedTimeSeconds`.
5. After the timer expires, the zombie can go back to ignoring VoidAnomaly.

If `groupAggro` is enabled, nearby mobs of the same entity type also become provoked.
