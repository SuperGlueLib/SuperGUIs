package me.superpenguin.superglue.guis

import me.superpenguin.superglue.foundations.util.ItemBuilder
import org.bukkit.Material

object Panes {

    private fun pane(type: Material) = ItemBuilder(type).name("&7").build()

    val CLEAR by lazy { pane(Material.GLASS_PANE) }
    val WHITE by lazy { pane(Material.WHITE_STAINED_GLASS_PANE) }
    val YELLOW by lazy { pane(Material.YELLOW_STAINED_GLASS_PANE) }
    val ORANGE by lazy { pane(Material.ORANGE_STAINED_GLASS_PANE) }
    val RED by lazy { pane(Material.RED_STAINED_GLASS_PANE) }
    val LIME by lazy { pane(Material.LIME_STAINED_GLASS_PANE) }
    val GREEN by lazy { pane(Material.GREEN_STAINED_GLASS_PANE) }
    val CYAN by lazy { pane(Material.CYAN_STAINED_GLASS_PANE) }
    val BLUE by lazy { pane(Material.LIGHT_BLUE_STAINED_GLASS_PANE) }
    val LIGHT_BLUE by lazy { pane(Material.LIGHT_BLUE_STAINED_GLASS_PANE) }
    val MAGENTA by lazy { pane(Material.MAGENTA_STAINED_GLASS_PANE) }
    val PINK by lazy { pane(Material.PINK_STAINED_GLASS_PANE) }
    val PURPLE by lazy { pane(Material.PURPLE_STAINED_GLASS_PANE) }
    val LIGHT_GRAY by lazy { pane(Material.LIGHT_GRAY_STAINED_GLASS_PANE) }
    val GRAY by lazy { pane(Material.GRAY_STAINED_GLASS_PANE) }
    val BROWN by lazy { pane(Material.BROWN_STAINED_GLASS_PANE) }
    val BLACK by lazy { pane(Material.BLACK_STAINED_GLASS_PANE) }

    private val allPanes = listOf(
        CLEAR, WHITE, YELLOW, ORANGE, RED, LIME, GREEN, CYAN, BLUE, LIGHT_BLUE,
        MAGENTA, PINK, PURPLE , LIGHT_GRAY, GRAY, BROWN, BLACK
    )

    fun random() = allPanes.random()
}