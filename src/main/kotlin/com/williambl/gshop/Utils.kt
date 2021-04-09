package com.williambl.gshop

import com.williambl.gshop.configs.GShopConfig
import io.github.gunpowder.api.GunpowderMod
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import java.util.*

val config: GShopConfig
    get() = GunpowderMod.instance.registry.getConfig(GShopConfig::class.java)
