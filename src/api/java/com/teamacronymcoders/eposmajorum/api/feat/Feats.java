package com.teamacronymcoders.eposmajorum.api.feat;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Feats implements INBTSerializable<NBTTagCompound> {
    private final Map<IFeat, FeatSource> feats;
    private final LoadingCache<Class, List<FeatEventHandler>> featCache;

    private int maxPoints;
    private int usedPoints;

    public Feats() {
        this.feats = Maps.newHashMap();
        this.featCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<Class, List<FeatEventHandler>>() {
                    @Override
                    public List<FeatEventHandler> load(@Nonnull Class key) {
                        return feats.keySet()
                                .parallelStream()
                                .map(IFeat::getEventHandlers)
                                .flatMap(List::parallelStream)
                                .filter(checkKey(key))
                                .collect(Collectors.toList());
                    }
                });

        this.maxPoints = 1;
        this.usedPoints = 0;
    }

    public Set<IFeat> getAll() {
        return this.feats.keySet();
    }

    public boolean addFeat(@Nonnull IFeat feat, @Nonnull FeatSource featSource) {
        if ((!featSource.countsTowardsPoints || this.usedPoints < this.maxPoints)) {
            this.feats.put(feat, featSource);
            for (FeatEventHandler featEventHandler: feat.getEventHandlers()) {
                featCache.invalidate(featEventHandler.getClass());
            }
            return true;
        }
        return false;
    }

    public void removeFeat(@Nonnull IFeat feat) {
        FeatSource featSource = this.feats.remove(feat);
        if (featSource != null && featSource.countsTowardsPoints) {
            this.usedPoints--;
        }
        for (FeatEventHandler featEventHandler: feat.getEventHandlers()) {
            featCache.invalidate(featEventHandler.getClass());
        }
    }

    //This will be a mess regardless
    @SuppressWarnings("unchecked")
    public <T extends Event> void handleEvent(T event) {
        featCache.getUnchecked(event.getClass())
                .forEach(list -> list.eventHandler.accept(event));
    }

    public int getAvailablePoints() {
        return this.maxPoints - this.usedPoints;
    }

    public int getMaxPoints() {
        return this.maxPoints;
    }

    public int getUsedPoints() {
        return this.usedPoints;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        feats.forEach((feat, source) -> nbtTagCompound.putString(feat.getRegistryName().toString(), source.id.toString()));
        return nbtTagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }

    private static Predicate<FeatEventHandler> checkKey(Class eventClass) {
        return handler -> handler.getClass().isAssignableFrom(eventClass);
    }
}
