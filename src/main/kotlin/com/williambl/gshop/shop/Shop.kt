package com.williambl.gshop.shop

import com.williambl.gshop.ChestGuiScreenHandlerFactory
import com.williambl.gshop.Screen
import io.github.gunpowder.api.builders.ChestGui
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText

data class Shop(val name: String, val categories: List<ShopCategory>) {

    fun screen(): Screen = { player -> {
        clearButtons()
        categories.forEachIndexed { i, page ->
            button(i, 0, page.icon.stack.setCustomName(LiteralText(page.name))) { _, container -> page.screen(::screen)(player)(container) }
        }
    }}

    fun gui(player: ServerPlayerEntity): ChestGuiScreenHandlerFactory {
        val gui = ChestGui.factory {
            player(player)

            if (categories.size != 1) {
                categories.forEachIndexed { i, category ->
                    button(
                        i,
                        0,
                        category.icon.stack.setCustomName(LiteralText(category.name))
                    ) { _, container -> category.screen(::screen)(player)(container) }
                }
            } else {
                var hasShown = false
                refreshInterval(1) { container ->
                    if (!hasShown) {
                        categories.first().screen(::screen)(player)(container)
                        hasShown = true
                    }
                }
            }
        }

        return ChestGuiScreenHandlerFactory(gui, LiteralText(name))
    }
}
