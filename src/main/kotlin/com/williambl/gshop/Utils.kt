package com.williambl.gshop

import com.williambl.gshop.configs.GShopConfig
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.builders.ChestGui
import net.minecraft.inventory.Inventory
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

fun ItemStack.isItemAndNbtEqual(other: ItemStack): Boolean =
    this.isItemEqual(other) && ItemStack.areTagsEqual(this, other)

fun Inventory.count(stack: ItemStack): Int {
    var result = 0
    for (i in 0 until this.size()) {
        val stackInSlot = this.getStack(i)
        if (stackInSlot.isItemAndNbtEqual(stack)) result += stackInSlot.count
    }
    return result
}

fun Inventory.removeAll(stack: ItemStack): Int {
    var result = 0
    for (i in 0 until this.size()) {
        val stackInSlot = this.getStack(i)
        if (stackInSlot.isItemAndNbtEqual(stack)) {
            result += stackInSlot.count
            stackInSlot.count = 0
        }
    }
    return result
}