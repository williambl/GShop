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

import com.williambl.gshop.Screen
import com.williambl.gshop.logger
import com.williambl.gshop.shop.entry.ItemStackShopEntry
import com.williambl.gshop.shop.entry.ShopEntry
import net.minecraft.item.Items
import net.minecraft.text.LiteralText

data class ShopCategory(val name: String, val icon: ItemStackShopEntry, val entries: List<ShopEntry>)

fun ShopCategory.screen(previous: (() -> Screen)? = null): Screen = { player -> {
    clearButtons()
    entries.forEachIndexed { i, entry ->
        if (i >= 53) {
            logger.warn("Category ${this@screen.name} has too many entries! Entry $i will not be shown.")
        } else {
            button(i % 9, i / 9, entry.icon) { actionType, container -> entry.screen {screen(previous)}(player)(container) }
        }
    }

    if (previous != null) {
        button(8, 5, Items.FEATHER.defaultStack.setCustomName(LiteralText("Back"))) { _, container -> previous()(player)(container) }
    }
}}
