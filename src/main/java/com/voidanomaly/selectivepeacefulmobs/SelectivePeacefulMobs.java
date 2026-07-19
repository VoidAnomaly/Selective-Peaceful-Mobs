package com.voidanomaly.selectivepeacefulmobs;

import com.voidanomaly.selectivepeacefulmobs.command.PeacefulMobsCommand;
import com.voidanomaly.selectivepeacefulmobs.config.SpmConfig;
import com.voidanomaly.selectivepeacefulmobs.event.MobBehaviorEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(SelectivePeacefulMobs.MOD_ID)
public class SelectivePeacefulMobs {
    public static final String MOD_ID = "selectivepeacefulmobs";

    public SelectivePeacefulMobs(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, SpmConfig.SPEC, "selective-peaceful-mobs-server.toml");

        NeoForge.EVENT_BUS.register(new MobBehaviorEvents());
        NeoForge.EVENT_BUS.register(new PeacefulMobsCommand());
    }
}
