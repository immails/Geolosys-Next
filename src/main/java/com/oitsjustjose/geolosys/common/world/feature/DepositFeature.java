package com.oitsjustjose.geolosys.common.world.feature;

import java.util.Map.Entry;
import java.util.Random;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.serialization.Codec;
import com.oitsjustjose.geolosys.Geolosys;
import com.oitsjustjose.geolosys.api.BlockPosDim;
import com.oitsjustjose.geolosys.api.ChunkPosDim;
import com.oitsjustjose.geolosys.api.GeolosysAPI;
import com.oitsjustjose.geolosys.api.world.IDeposit;
import com.oitsjustjose.geolosys.common.config.CommonConfig;
import com.oitsjustjose.geolosys.common.utils.Utils;
import com.oitsjustjose.geolosys.common.world.capability.IDepositCapability;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

public class DepositFeature extends Feature<NoFeatureConfig> {
    public DepositFeature(Codec<NoFeatureConfig> p_i231976_1_) {
        super(p_i231976_1_);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean generate(ISeedReader reader, ChunkGenerator generator, Random rand, BlockPos pos,
            NoFeatureConfig config) {
        if (generator instanceof FlatChunkGenerator) {
            return false;
        }

        IDepositCapability cap = reader.getWorld().getCapability(GeolosysAPI.GEOLOSYS_WORLD_CAPABILITY).orElse(null);
        if (cap == null) {
            Geolosys.getInstance().LOGGER.error("NULL PLUTON CAPABILITY!!!");
            return false;
        }

        boolean placedPending = placePendingBlocks(reader, cap, pos);

        String dimName = Utils.dimensionToString(reader);
        ChunkPosDim chunkPosDim = new ChunkPosDim(pos, dimName);
        if (cap.hasOrePlutonGenerated(chunkPosDim)) {
            return false;
        }

        IDeposit pluton = GeolosysAPI.plutonRegistry.pick(reader, pos, rand);
        if (rand.nextInt(CommonConfig.CHUNK_SKIP_CHANCE.get()) > pluton.getGenWt()) {
            return false;
        }

        boolean anyGenerated = pluton.generate(reader, pos, cap, dimName) > 0;
        pluton.afterGen(reader, pos);

        if (anyGenerated) {
            // TODO: for top layer plutons repeat about 10-ish times, see "algo" below.
            // if (pluton.getPlutonType() == PlutonType.TOP_LAYER && rand.nextInt(10) == 0)
            // {
            // return generate(reader, generator, rand, pos, config);
            // }
            cap.setOrePlutonGenerated(new ChunkPosDim(pos.getX(), pos.getZ(), dimName));
            return true;
        }

        return placedPending;
    }

    private boolean placePendingBlocks(ISeedReader reader, IDepositCapability cap, BlockPos pos) {
        boolean placedAny = false;

        for (Entry<BlockPosDim, BlockState> e : cap.getPendingBlocks().entrySet()) {
            if (FeatureUtils.isInChunk(new ChunkPos(pos), e.getKey())) {
                if (reader.setBlockState(e.getKey().getPos(), e.getValue(), 2 | 16)) {
                    placedAny = true;
                    cap.getPendingBlocks().remove(e.getKey());
                    if (CommonConfig.DEBUG_WORLD_GEN.get()) {
                        Geolosys.getInstance().LOGGER.info("Generated pending block "
                                + e.getValue().getBlock().getRegistryName().toString() + " at " + e.getKey());
                    }
                } else {
                    if (CommonConfig.DEBUG_WORLD_GEN.get()) {
                        Geolosys.getInstance().LOGGER.error("FAILED to generate pending block "
                                + e.getValue().getBlock().getRegistryName().toString() + " at " + e.getKey());
                    }
                }
            }
        }

        return placedAny;
    }
}