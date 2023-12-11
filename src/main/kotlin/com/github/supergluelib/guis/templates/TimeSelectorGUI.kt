package com.github.supergluelib.guis.templates

import com.github.supergluelib.foundation.input.Input
import com.github.supergluelib.foundation.isInt
import com.github.supergluelib.foundation.send
import com.github.supergluelib.foundation.util.ItemBuilder
import com.github.supergluelib.guis.GUI
import com.github.supergluelib.guis.GUIManager
import com.github.supergluelib.guis.Panes
import com.github.supergluelib.guis.fillEmpty
import org.bukkit.Material
import java.util.concurrent.TimeUnit

class TimeSelectorGUI(val withTime: (Pair<Int, TimeUnit>) -> Unit): GUI() {
    var timeunit: TimeUnit? = null
    var amount: Int? = null
    override fun generateInventory() = createInventory("Time Selection", 27) {
        setButton(12, ItemBuilder(Material.OAK_SIGN,"Choose Amount").addLore("&7Currently: ${amount ?: "Unspecified"}").build()) {
            GUIManager.closeGui(player)
            player.send("&6Type the number of units in the chat (example: 15)")
            Input.Chat.take(player) {
                if (it.isInt()) this@TimeSelectorGUI.amount = it.toInt()
                else player.send("&cYou have specified an invalid number '$it'")
                this@TimeSelectorGUI.open(player)
            }
        }

        setButton(16, ItemBuilder(Material.CLOCK, "Choose Unit").addLore("&7Currently: ${timeunit ?: "Unspecified"}").build()){
            TimeUnitSelection(this@TimeSelectorGUI).open(player)
        }

        setButton(14, ItemBuilder(Material.LIME_STAINED_GLASS_PANE, "&aDone").build()) {
            if (timeunit == null || amount == null) player.send("&cInvalid time unit specified")
            else withTime.invoke(amount!! to timeunit!!)
        }
    }

    class TimeUnitSelection(val selector: TimeSelectorGUI): GUI() {
        override fun generateInventory() = createInventory("Time Unit Selection", 27) {
            setItem(10, SECONDS)
            setItem(12, MINUTES)
            setItem(14, HOURS)
            setItem(16, DAYS)
            fillEmpty(Panes.BLACK)
        }
        private companion object {
            val SECONDS = ItemBuilder(Material.CLOCK, "&6Seconds").locname("SECONDS").build()
            val MINUTES = ItemBuilder(Material.CLOCK, "&6Minutes").locname("MINUTES").build()
            val HOURS = ItemBuilder(Material.CLOCK, "&6Hours").locname("HOURS").build()
            val DAYS = ItemBuilder(Material.CLOCK, "&6Days").locname("DAYS").build()
        }

        override fun onClick(click: ClickData) {
            val unit = click.locname?.let(TimeUnit::valueOf) ?: return
            selector.timeunit = unit
            selector.open(click.player)
        }
    }
}