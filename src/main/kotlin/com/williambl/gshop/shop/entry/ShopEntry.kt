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

package com.williambl.gshop.shop.entry

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.williambl.gshop.*
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.StringNbtReader
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import java.math.BigDecimal
import kotlin.math.max
import kotlin.math.min

@JsonDeserialize(using = ShopEntry.Deserializer::class)
interface ShopEntry {
    val type: ShopEntryType
    val priceToBuy: BigDecimal
    val priceToSell: BigDecimal

    val icon: ItemStack

    fun screen(previous: (() -> Screen)? = null): Screen

    class Deserializer : StdDeserializer<ShopEntry>(ShopEntry::class.java) {
        override fun deserialize(p: JsonParser, ctx: DeserializationContext): ShopEntry {
            val tree = p.readValueAsTree<TreeNode>()
            ctx.setAttribute("ShopEntryTree", tree)
            return ctx.findRootValueDeserializer(ctx.constructType(ShopEntryType.valueOf((tree["type"] as TextNode).asText()).entryClass.java)).deserialize(p, ctx) as ShopEntry
        }
    }
}

@JsonSerialize(using = ItemStackShopEntry.Serializer::class)
@JsonDeserialize(using = ItemStackShopEntry.Deserializer::class)
data class ItemStackShopEntry(val stack: ItemStack, override val priceToBuy: BigDecimal = BigDecimal.ZERO, override val priceToSell: BigDecimal = BigDecimal.ZERO): ShopEntry {
    override val type: ShopEntryType = ShopEntryType.ITEM_STACK
    override val icon: ItemStack = stack.copy()

    override fun screen(previous: (() -> Screen)?): Screen = { player -> {
        clearButtons()

        val stackToSell = stack.copy()
        val sellButtonIcon = Items.PINK_DYE.defaultStack.setCustomName(LiteralText("SELL"))
        val buyButtonIcon = Items.LIME_DYE.defaultStack.setCustomName(LiteralText("BUY"))
        var amountToSell = BigDecimal.ZERO
        var amountToBuy = BigDecimal.ZERO

        fun update() {
            amountToSell = priceToSell * BigDecimal(stackToSell.count)
            amountToBuy = priceToBuy * BigDecimal(stackToSell.count)

            sellButtonIcon.setCustomName(if (player.canSell(stackToSell)) LiteralText("SELL") else LiteralText("Cannot Sell").formatted(Formatting.DARK_RED))
            buyButtonIcon.setCustomName(if (player.canBuy(amountToBuy)) LiteralText("BUY") else LiteralText("Cannot Buy").formatted(Formatting.DARK_RED))

            sellButtonIcon.setLore(listOf(LiteralText("$$amountToSell"), LiteralText("Shift-Click: Sell All ($${priceToSell*BigDecimal(player.inventory.count(stackToSell))})")))
            buyButtonIcon.setLore(listOf(LiteralText("$$amountToBuy")))
        }

        update()

        button(4, 1, stackToSell) { _, _ ->  }
        button(2, 2, Items.RED_STAINED_GLASS_PANE.defaultStack.setCustomName(LiteralText("-1"))) { actionType, container ->
            if (actionType == SlotActionType.PICKUP) stackToSell.count = max(stackToSell.count - 1, 1)
            update()
        }
        button(1, 2, Items.RED_STAINED_GLASS_PANE.defaultStack.setCustomName(LiteralText("-10"))) { actionType, container ->
            if (actionType == SlotActionType.PICKUP) stackToSell.count = max(stackToSell.count - 10, 1)
            update()
        }
        button(0, 2, Items.RED_STAINED_GLASS_PANE.defaultStack.setCustomName(LiteralText("-64"))) { actionType, container ->
            if (actionType == SlotActionType.PICKUP) stackToSell.count = 1
            update()
        }
        button(6, 2, Items.GREEN_STAINED_GLASS_PANE.defaultStack.setCustomName(LiteralText("+1"))) { actionType, container ->
            if (actionType == SlotActionType.PICKUP) stackToSell.count = min(stackToSell.count + 1, stackToSell.maxCount)
            update()
        }
        button(7, 2, Items.GREEN_STAINED_GLASS_PANE.defaultStack.setCustomName(LiteralText("+10"))) { actionType, container ->
            if (actionType == SlotActionType.PICKUP) stackToSell.count += min(stackToSell.count + 10, stackToSell.maxCount)
            update()
        }
        button(8, 2, Items.GREEN_STAINED_GLASS_PANE.defaultStack.setCustomName(LiteralText("+64"))) { actionType, container ->
            if (actionType == SlotActionType.PICKUP) stackToSell.count += min(stackToSell.count + 64, stackToSell.maxCount)
            update()
        }

        if (priceToSell >= BigDecimal.ZERO) {
            button(3, 4, sellButtonIcon) { actionType, container ->
                if (actionType == SlotActionType.QUICK_MOVE) {
                    val stacks = player.inventory.removeAll(stackToSell)
                    if (stacks > 0) {
                        player.sell(priceToSell * BigDecimal(stacks))
                    }
                } else {
                    if (player.canSell(stackToSell)) {
                        player.sell(stackToSell, amountToSell)
                        player.closeHandledScreen()
                    }
                }
            }
        }

        if (priceToBuy >= BigDecimal.ZERO) {
            button(5, 4, buyButtonIcon) { actionType, container ->
                if (player.canBuy(amountToBuy)) {
                    player.buy(stackToSell, amountToBuy)
                    player.closeHandledScreen()
                }
            }
        }

        if (previous != null) {
            button(8, 5, Items.FEATHER.defaultStack.setCustomName(LiteralText("Back"))) { _, container -> previous()(player)(container) }
        }
    }}

    class Serializer : StdSerializer<ItemStackShopEntry>(ItemStackShopEntry::class.java) {
        override fun serialize(value: ItemStackShopEntry, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeStartObject()
            gen.writeStringField("type", value.type.name)
            gen.writeStringField("stack", CompoundTag().also { value.stack.toTag(it) }.toString())
            if (value.priceToBuy != BigDecimal.ZERO) {
                gen.writeNumberField("priceToBuy", value.priceToBuy)
            }
            if (value.priceToSell != BigDecimal.ZERO) {
                gen.writeNumberField("priceToSell", value.priceToSell)
            }
            gen.writeEndObject()
        }
    }

    class Deserializer : StdDeserializer<ItemStackShopEntry>(ItemStackShopEntry::class.java) {
        override fun deserialize(p: JsonParser, ctx: DeserializationContext): ItemStackShopEntry {
            val tree = ctx.getAttribute("ShopEntryTree") as TreeNode? ?: p.readValueAsTree()
            ctx.setAttribute("ShopEntryTree", null)
            return ItemStackShopEntry(ItemStack.fromTag(StringNbtReader.parse((tree["stack"] as TextNode).asText())), (tree["priceToBuy"] as JsonNode?)?.decimalValue() ?: BigDecimal.ZERO, (tree["priceToSell"] as JsonNode?)?.decimalValue() ?: BigDecimal.ZERO)
        }
    }
}

@JsonSerialize(using = CommandShopEntry.Serializer::class)
@JsonDeserialize(using = CommandShopEntry.Deserializer::class)
data class CommandShopEntry(val command: String, override val icon: ItemStack, override val priceToBuy: BigDecimal = BigDecimal.ZERO): ShopEntry {
    override val type: ShopEntryType = ShopEntryType.COMMAND
    override val priceToSell: BigDecimal = BigDecimal.ZERO

    override fun screen(previous: (() -> Screen)?): Screen = { player -> {
        clearButtons()

        val buyButtonIcon = Items.LIME_DYE.defaultStack.setCustomName(LiteralText("BUY"))
        buyButtonIcon.setCustomName(if (player.canBuy(priceToBuy)) LiteralText("BUY") else LiteralText("Cannot Buy").formatted(Formatting.DARK_RED))
        buyButtonIcon.setLore(listOf(LiteralText("$$priceToBuy")))

        button(4, 1, icon) { _, _ ->  }

        button(4, 4, buyButtonIcon) { actionType, container ->
            if (player.canBuy(priceToBuy)) {
                player.buy(priceToBuy)
                player.server.commandManager.execute(player.server.commandSource, command.replace("--buyer--", player.gameProfile.name))
                player.closeHandledScreen()
            }
        }

        if (previous != null) {
            button(8, 5, Items.FEATHER.defaultStack.setCustomName(LiteralText("Back"))) { _, container -> previous()(player)(container) }
        }
    }}

    class Serializer : StdSerializer<CommandShopEntry>(CommandShopEntry::class.java) {
        override fun serialize(value: CommandShopEntry, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeStartObject()
            gen.writeStringField("type", value.type.name)
            gen.writeStringField("command", value.command)
            gen.writeStringField("icon", CompoundTag().also { value.icon.toTag(it) }.toString())
            if (value.priceToBuy != BigDecimal.ZERO) {
                gen.writeNumberField("priceToBuy", value.priceToBuy)
            }
            gen.writeEndObject()
        }
    }

    class Deserializer : StdDeserializer<CommandShopEntry>(CommandShopEntry::class.java) {
        override fun deserialize(p: JsonParser, ctx: DeserializationContext): CommandShopEntry {
            val tree = ctx.getAttribute("ShopEntryTree") as TreeNode? ?: p.readValueAsTree()
            ctx.setAttribute("ShopEntryTree", null)
            return CommandShopEntry((tree["command"] as TextNode).asText(), ItemStack.fromTag(StringNbtReader.parse((tree["icon"] as TextNode).asText())), (tree["priceToBuy"] as JsonNode?)?.decimalValue() ?: BigDecimal.ZERO)
        }
    }
}
