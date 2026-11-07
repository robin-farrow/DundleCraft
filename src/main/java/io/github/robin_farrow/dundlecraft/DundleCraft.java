package io.github.robin_farrow.dundlecraft;

import com.mojang.logging.LogUtils;
import io.github.robin_farrow.dundlecraft.core.DundleModule;
import io.github.robin_farrow.dundlecraft.modules.dundlehardcore.DundleHardcoreModule;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.PersistentBlocksModule;
import io.github.robin_farrow.dundlecraft.modules.playersoul.PlayerSoulModule;
import io.github.robin_farrow.dundlecraft.modules.rarehealing.RareHealingModule;
import io.github.robin_farrow.dundlecraft.modules.resourceless.ResourceLessModule;
import io.github.robin_farrow.dundlecraft.modules.signalflare.SignalFlareModule;
import io.github.robin_farrow.dundlecraft.modules.truedark.TrueDarkModule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;

import java.util.List;

@Mod(DundleCraft.MODID)
public class DundleCraft
{
    public static final String MODID = "dundlecraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    private final List<DundleModule> dundleModules = List.of(
            new DundleHardcoreModule(),
            new PersistentBlocksModule(),
            new PlayerSoulModule(),
            new TrueDarkModule(),
            new RareHealingModule(),
            new ResourceLessModule(),
            new SignalFlareModule()
    );

    public DundleCraft(IEventBus modEventBus, ModContainer modContainer)
    {
        dundleModules.forEach(module -> module.register(modEventBus));
    }
}
