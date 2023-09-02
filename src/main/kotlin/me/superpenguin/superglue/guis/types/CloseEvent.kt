package me.superpenguin.superglue.guis.types

import org.bukkit.entity.Player

interface CloseEvent {

    fun whenClosed(player: Player)

}