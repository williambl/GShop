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

import ca.stellardrift.colonel.api.ServerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.*
import com.williambl.gshop.configs.GShopConfig
import com.williambl.gshop.shop.gui
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.GunpowderModule
import io.github.gunpowder.api.builders.Command
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.command.argument.EntityArgumentType.getPlayer
import net.minecraft.command.argument.EntityArgumentType.player
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier

class GShop : GunpowderModule {
    override val name = "gshop"
    override val toggleable = true
    private val gunpowder: GunpowderMod
        get() = GunpowderMod.instance

    override fun registerCommands() = gunpowder.registry.registerCommand { dispatcher ->
        Command.builder(dispatcher) {
            command("shop") {
                requires(Permissions.require("gshop.viewshop", 1)::test)
                argument("shop", ShopArgumentType()) {
                    executes { ctx ->
                        val shop = getShop(ctx, "shop")
                        if (!Permissions.check(ctx.source, "gshop.viewshop.${shop.name.toLowerCase()}", 2)) {
                            ctx.source.sendError(LiteralText("No permission to view shop: ${shop.name}"))
                            return@executes 0
                        }
                        ctx.source.player.openHandledScreen(shop.gui(ctx.source.player))
                        0
                    }
                }
            }

            command("showshop") {
                requires(Permissions.require("gshop.showshop", 2)::test)
                argument("target", player()) {
                    argument("shop", ShopArgumentType()) {
                        executes { ctx ->
                            val player = getPlayer(ctx, "target")
                            val shop = getShop(ctx, "shop")
                            player.openHandledScreen(shop.gui(player))
                            0
                        }
                    }
                }
            }

            command("shopconfigurator") {
                requires(Permissions.require("gshop.config", 2)::test)
                literal("export-itemstack") {
                    executes { ctx ->
                        ctx.source.sendFeedback(LiteralText(CompoundTag().also { ctx.source.player.mainHandStack.toTag(it) }.toString()), false)
                        0
                    }
                }
            }

            command("shops") {
                requires(Permissions.require("gshop.listshops", 1)::test)
                executes { ctx ->
                    val shops = config.shops.filter { Permissions.check(ctx.source, "gshop.viewshop.${it.name.toLowerCase()}", true) }
                    if (shops.isNotEmpty()) {
                        ctx.source.sendFeedback(LiteralText("Available shops:"), false)
                    } else {
                        ctx.source.sendError(LiteralText("No shops are available to you"))
                    }
                    for (shop in shops) {
                        ctx.source.sendFeedback(LiteralText(shop.name), false)
                    }
                    shops.size
                }
            }
        }
    }

    override fun registerConfigs() {
        gunpowder.registry.registerConfig("gshop.yml", GShopConfig::class.java, "gshop.yml")
    }

    override fun onInitialize() {
        super.onInitialize()
        ServerArgumentType.builder<ShopArgumentType>(Identifier("gshop:shop"))
            .type(ShopArgumentType::class.java)
            .serializer(ShopArgumentType.serializer)
            .fallbackProvider { word() }
            .register()
    }
}