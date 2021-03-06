package com.teamacronymcoders.epos.feats;

import com.teamacronymcoders.epos.api.EposAPI;
import com.teamacronymcoders.epos.api.feat.Feat;
import com.teamacronymcoders.epos.api.feat.FeatBuilder;
import com.teamacronymcoders.epos.utils.BlockBreakHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.world.BlockEvent;

public class TimberFeat {
    private static final ResourceLocation NAME = new ResourceLocation(EposAPI.ID, "timber");
    public static final Feat FEAT =
            FeatBuilder.start(NAME)
                    .withEventHandler(BlockEvent.BreakEvent.class,
                            (breakEvent, entity, iCharacterStats) -> {
                                if (entity.getActiveItemStack().getToolTypes().contains(ToolType.AXE)) {
                                    if (breakEvent.getState().getBlock().isIn(BlockTags.LOGS)) {
                                        BlockPos pos = breakEvent.getPos();
                                        World world = breakEvent.getWorld().getWorld();
                                        PlayerEntity player = breakEvent.getPlayer();

                                        // Runs through blocks, adding valid blocks to the schedueled list to check, and checked blocks to checked.
                                        BlockBreakHelper.handleHarvest(pos, world, player);
                                    }
                                }
                            })
                    .finish();
}