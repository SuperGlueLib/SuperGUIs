package me.superpenguin.superglue.guis.types

import org.bukkit.entity.Player

interface OpenEvent {

    fun onOpen(player: Player)

}