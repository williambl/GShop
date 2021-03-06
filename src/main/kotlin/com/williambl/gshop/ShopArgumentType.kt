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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.williambl.gshop.shop.Shop
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.serialize.ArgumentSerializer
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import java.util.concurrent.CompletableFuture

class ShopArgumentType(val shops: List<Shop> = config.shops): ArgumentType<Shop> {
    override fun parse(reader: StringReader): Shop {
        val shopName = reader.readString()
        return shops.find { it.name == shopName } ?: throw unknownShop.create(shopName)
    }

    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return if (context.source is CommandSource)
            CommandSource.suggestMatching(shops.map { it.name }.filter { Permissions.check(context.source as CommandSource, "gshop.shop.$it", 2) }, builder)
        else Suggestions.empty()
    }

    companion object {
        val unknownShop = DynamicCommandExceptionType {
            LiteralText("Unknown Shop: $it")
        }

        val serializer = object : ArgumentSerializer<ShopArgumentType> {
            override fun toPacket(argumentType: ShopArgumentType, packetByteBuf: PacketByteBuf) {
                packetByteBuf.writeVarInt(argumentType.shops.size)
                for (shop in argumentType.shops) {
                    packetByteBuf.writeString(shop.name)
                }
            }

            override fun fromPacket(packetByteBuf: PacketByteBuf): ShopArgumentType {
                return ShopArgumentType(List(packetByteBuf.readVarInt()) { Shop(packetByteBuf.readString(), listOf()) })
            }

            override fun toJson(argumentType: ShopArgumentType, jsonObject: JsonObject) {
                jsonObject.add("shops", JsonArray().also { array -> argumentType.shops.forEach { array.add(it.name) } } )
            }
        }
    }
}

fun getShop(commandContext: CommandContext<ServerCommandSource>, name: String): Shop =
    commandContext.getArgument(name, Shop::class.java) as Shop