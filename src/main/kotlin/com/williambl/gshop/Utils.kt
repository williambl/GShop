package com.williambl.gshop

import com.williambl.gshop.configs.GShopConfig
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.builders.ChestGui
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.item.ItemStack
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

val config: GShopConfig
    get() = GunpowderMod.instance.registry.getConfig(GShopConfig::class.java)

typealias Screen = (ServerPlayerEntity) -> ChestGui.Container.() -> Unit

fun ItemStack.setLore(text: Collection<Text>) {
    getOrCreateSubTag("display").put("Lore", ListTag().also { tag -> tag.addAll(text.map { StringTag.of(Text.Serializer.toJson(it)) })})
}
