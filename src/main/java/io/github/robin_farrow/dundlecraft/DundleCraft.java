package io.github.robin_farrow.dundlecraft;

import io.github.robin_farrow.dundlecraft.core.DundleModule;
import io.github.robin_farrow.dundlecraft.modules.truedark.TrueDarkModule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

import java.util.List;

@Mod(DundleCraft.MODID)
public class DundleCraft {
    public static final String MODID = "dundlecraft";

    private final List<DundleModule> dundleModules = List.of(
            new TrueDarkModule()
    );

    public DundleCraft(IEventBus modEventBus, ModContainer modContainer) {
    }
}
