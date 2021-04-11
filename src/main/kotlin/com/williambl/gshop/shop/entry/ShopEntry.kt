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

            sellButtonIcon.setCustomName(if (player.canSell(stackToSell)) LiteralText("SELL") else LiteralText("X Cannot Sell X").formatted(Formatting.DARK_RED))
            buyButtonIcon.setCustomName(if (player.canBuy(amountToBuy)) LiteralText("BUY") else LiteralText("X Cannot Buy X").formatted(Formatting.DARK_RED))

            sellButtonIcon.setLore(listOf(LiteralText("$$amountToSell")))
            buyButtonIcon.setLore(listOf(LiteralText("$$amountToBuy")))
        }

        update()

        button(4, 1, stackToSell) { _, _ ->  }
        button(2, 2, Items.RED_STAINED_GLASS_PANE.defaultStack.setCustomName(LiteralText("-1"))) { actionType, container ->
            if (actionType == SlotActionType.PICKUP) stackToSell.count = max(stackToSell.count - 1, 1)
            update()
        }
        button(1, 2, Items.RED_STAINED_GLASS_PANE.defaultStack.setCustomName(LiteralText("-5"))) { actionType, container ->
            if (actionType == SlotActionType.PICKUP) stackToSell.count = max(stackToSell.count - 5, 1)
            update()
        }
        button(6, 2, Items.GREEN_STAINED_GLASS_PANE.defaultStack.setCustomName(LiteralText("+1"))) { actionType, container ->
            if (actionType == SlotActionType.PICKUP) stackToSell.count++
            update()
        }
        button(7, 2, Items.GREEN_STAINED_GLASS_PANE.defaultStack.setCustomName(LiteralText("+5"))) { actionType, container ->
            if (actionType == SlotActionType.PICKUP) stackToSell.count += 5
            update()
        }

        button(3, 4, sellButtonIcon) { actionType, container ->
            if (player.canSell(stackToSell)) {
                player.sell(stackToSell, amountToSell)
                player.closeHandledScreen()
            }
        }

        button(5, 4, buyButtonIcon) { actionType, container ->
            if (player.canBuy(amountToBuy)) {
                player.buy(stackToSell, amountToBuy)
                player.closeHandledScreen()
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
            return ItemStackShopEntry(ItemStack.fromTag(StringNbtReader.parse((tree["stack"] as TextNode).asText())), (tree["priceToBuy"] as JsonNode?)?.decimalValue() ?: BigDecimal.ZERO, (tree["priceToSell"] as JsonNode?)?.decimalValue() ?: BigDecimal.ZERO)
        }
    }
}
