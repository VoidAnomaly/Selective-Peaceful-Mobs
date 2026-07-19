package com.voidanomaly.selectivepeacefulmobs.state;

import com.voidanomaly.selectivepeacefulmobs.SelectivePeacefulMobs;
import com.voidanomaly.selectivepeacefulmobs.config.SpmConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerPeacefulState extends SavedData {
    private static final String DATA_NAME = SelectivePeacefulMobs.MOD_ID + "_players";
    private final Map<UUID, Boolean> playerChoices = new HashMap<>();

    public static PlayerPeacefulState get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PlayerPeacefulState::new, PlayerPeacefulState::load),
                DATA_NAME
        );
    }

    public static PlayerPeacefulState get(ServerPlayer player) {
        return get(player.serverLevel());
    }

    public boolean isPeaceful(ServerPlayer player) {
        if (!SpmConfig.REMEMBER_PLAYER_CHOICE.get()) {
            return SpmConfig.DEFAULT_PEACEFUL.get();
        }
        return playerChoices.getOrDefault(player.getUUID(), SpmConfig.DEFAULT_PEACEFUL.get());
    }

    public Optional<Boolean> getExplicitChoice(ServerPlayer player) {
        if (!SpmConfig.REMEMBER_PLAYER_CHOICE.get()) {
            return Optional.empty();
        }
        return Optional.ofNullable(playerChoices.get(player.getUUID()));
    }

    public void setPeaceful(ServerPlayer player, boolean peaceful) {
        if (!SpmConfig.REMEMBER_PLAYER_CHOICE.get()) {
            return;
        }
        playerChoices.put(player.getUUID(), peaceful);
        setDirty();
    }

    public void clearChoice(ServerPlayer player) {
        playerChoices.remove(player.getUUID());
        setDirty();
    }

    public static PlayerPeacefulState load(CompoundTag tag, HolderLookup.Provider registries) {
        PlayerPeacefulState state = new PlayerPeacefulState();
        ListTag list = tag.getList("players", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            state.playerChoices.put(entry.getUUID("uuid"), entry.getBoolean("peaceful"));
        }

        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();

        for (Map.Entry<UUID, Boolean> entry : playerChoices.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("uuid", entry.getKey());
            playerTag.putBoolean("peaceful", entry.getValue());
            list.add(playerTag);
        }

        tag.put("players", list);
        return tag;
    }
}
