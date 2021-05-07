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

package com.williambl.gshop.shop

import com.williambl.gshop.ChestGuiScreenHandlerFactory
import com.williambl.gshop.Screen
import com.williambl.gshop.logger
import io.github.gunpowder.api.builders.ChestGui
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText

data class Shop(val name: String, val categories: List<ShopCategory>)

private fun Shop.screen(): Screen = {
    clearButtons()
    categories.forEachIndexed { i, page ->
        if (i >= 53) {
            logger.warn("Shop ${this@screen.name} has too many categories! Category ${page.name} will not be shown.")
        } else {
            button(i % 9, i / 9, page.icon.stack.setCustomName(LiteralText(page.name))) { _, container -> page.screen(::screen)(container) }
        }
    }
}

fun Shop.gui(player: ServerPlayerEntity): ChestGuiScreenHandlerFactory {
    val gui = ChestGui.factory {
        player(player)

        if (categories.size != 1) {
            screen()()
        } else {
            var hasShown = false
            refreshInterval(1) { container ->
                if (!hasShown) {
                    categories.first().screen(::screen)(container)
                    hasShown = true
                }
            }
        }
    }

    return ChestGuiScreenHandlerFactory(gui, LiteralText(name))
}
