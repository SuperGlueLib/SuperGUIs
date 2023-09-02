package me.superpenguin.superglue.guis.types

import org.bukkit.entity.Player

interface ForceKeepOpen {

    fun canClose(player: Player): Boolean

}