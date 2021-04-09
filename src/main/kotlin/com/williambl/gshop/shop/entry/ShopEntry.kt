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
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.StringNbtReader

@JsonDeserialize(using = ShopEntry.Deserializer::class)
interface ShopEntry {
    val type: ShopEntryType
    val priceToBuy: Int
    val priceToSell: Int

    val icon: ItemStack

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
data class ItemStackShopEntry(val stack: ItemStack, override val priceToBuy: Int = 0, override val priceToSell: Int = 0): ShopEntry {
    override val type: ShopEntryType = ShopEntryType.ITEM_STACK
    override val icon: ItemStack = stack

    class Serializer : StdSerializer<ItemStackShopEntry>(ItemStackShopEntry::class.java) {
        override fun serialize(value: ItemStackShopEntry, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeStartObject()
            gen.writeStringField("type", value.type.name)
            gen.writeStringField("stack", CompoundTag().also { value.stack.toTag(it) }.toString())
            if (value.priceToBuy != 0) {
                gen.writeNumberField("priceToBuy", value.priceToBuy)
            }
            if (value.priceToSell != 0) {
                gen.writeNumberField("priceToSell", value.priceToSell)
            }
            gen.writeEndObject()
        }
    }

    class Deserializer : StdDeserializer<ItemStackShopEntry>(ItemStackShopEntry::class.java) {
        override fun deserialize(p: JsonParser, ctx: DeserializationContext): ItemStackShopEntry {
            val tree = ctx.getAttribute("ShopEntryTree") as TreeNode? ?: p.readValueAsTree()
            return ItemStackShopEntry(ItemStack.fromTag(StringNbtReader.parse((tree["stack"] as TextNode).asText())), (tree["priceToBuy"] as JsonNode?)?.asInt() ?: 0, (tree["priceToSell"] as JsonNode?)?.asInt() ?: 0)
        }
    }
}
