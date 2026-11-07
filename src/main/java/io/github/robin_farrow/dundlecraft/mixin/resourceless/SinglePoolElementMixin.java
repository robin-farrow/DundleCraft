package io.github.robin_farrow.dundlecraft.mixin.resourceless;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.robin_farrow.dundlecraft.modules.resourceless.StructureGenerationRules;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SinglePoolElement.class)
public abstract class SinglePoolElementMixin
{
    @ModifyReturnValue(method = "getSettings", at = @At("RETURN"))
    private StructurePlaceSettings dundlecraft$structureRules(StructurePlaceSettings settings) {
        String path = ((SinglePoolElement) (Object) this).getTemplateLocation().getPath();
        if (path.startsWith("village/")) {
            settings.addProcessor(StructureGenerationRules.VILLAGE_REPLACE_BLOCKS);
        } else if (path.startsWith("trial_chambers/")) {
            settings.addProcessor(StructureGenerationRules.TRIAL_REPLACE_COPPER);
        }
        return settings;
    }
}
