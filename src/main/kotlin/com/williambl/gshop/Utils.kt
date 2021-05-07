/*
 * MIT License
 *
 * Copyright (c) 2021 Will BL
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.williambl.gshop

import com.williambl.gshop.configs.GShopConfig
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.builders.ChestGui
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.text.Text
import org.apache.logging.log4j.Logger

val config: GShopConfig
    get() = GunpowderMod.instance.registry.getConfig(GShopConfig::class.java)

val logger: Logger
    get() = GunpowderMod.instance.logger

typealias Screen = ChestGui.Container.() -> Unit

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
