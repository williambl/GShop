package com.williambl.gshop

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class ChestGuiScreenHandlerFactory(val gui: (Int, ServerPlayerEntity) -> ScreenHandler, val name: Text) :
    NamedScreenHandlerFactory {
    override fun createMenu(
        syncId: Int,
        inv: PlayerInventory,
        player: PlayerEntity
    ): ScreenHandler {
        return gui(syncId, player as ServerPlayerEntity)
    }

    override fun getDisplayName(): Text {
        return name
    }
}