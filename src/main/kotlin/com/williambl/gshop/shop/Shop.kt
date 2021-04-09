package com.williambl.gshop.shop

import com.williambl.gshop.ChestGuiScreenHandlerFactory
import com.williambl.gshop.Screen
import io.github.gunpowder.api.builders.ChestGui
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText

data class Shop(val name: String, val pages: List<ShopPage>) {

    fun screen(): Screen = { player -> {
        clearButtons()
        pages.forEachIndexed { i, page ->
            button(i, 0, page.icon.stack.setCustomName(LiteralText(page.name))) { _, container -> page.screen(::screen)(player)(container) }
        }
    }}

    fun gui(player: ServerPlayerEntity): ChestGuiScreenHandlerFactory {
        val gui = ChestGui.factory {
            player(player)

            pages.forEachIndexed { i, page ->
                button(i, 0, page.icon.stack.setCustomName(LiteralText(page.name))) { _, container -> page.screen(::screen)(player)(container) }
            }
        }

        return ChestGuiScreenHandlerFactory(gui, LiteralText(name))
    }
}
