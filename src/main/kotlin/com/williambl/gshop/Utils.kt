package com.williambl.gshop

import com.williambl.gshop.configs.GShopConfig
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.builders.ChestGui
import net.minecraft.server.network.ServerPlayerEntity

val config: GShopConfig
    get() = GunpowderMod.instance.registry.getConfig(GShopConfig::class.java)

typealias Screen = (ServerPlayerEntity) -> ChestGui.Container.() -> Unit