package com.teamacronymcoders.epos.json.jsonprovider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.ResourceLocation;

public interface IJsonProvider<T> {
    T provide(ResourceLocation key, JsonObject jsonObject) throws JsonParseException;
}
