package me.superpenguin.superglue.guis

import me.superpenguin.superglue.foundations.isValid
import me.superpenguin.superglue.foundations.toColor
import me.superpenguin.superglue.foundations.util.ItemBuilder
import me.superpenguin.superglue.guis.guiparts.Button
import me.superpenguin.superglue.guis.guiparts.Button.Companion.getId
import me.superpenguin.superglue.guis.guiparts.Button.Companion.isButton
import me.superpenguin.superglue.guis.guiparts.DynamicButton
import me.superpenguin.superglue.guis.types.CloseEvent
import me.superpenguin.superglue.guis.types.ForceKeepOpen
import me.superpenguin.superglue.guis.types.OpenEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.scheduler.BukkitTask
import java.util.*

/*

Apparently Dynamic buttons dont update on click properly
add a re-open method which refreshes and re-opens
add a close-for-all method which closes the inventory for all viewers, safely.
add ability to set buttons to multiple slots, put emphasis on button constructor by having button.setIn(inv, slot) and inv.setButton(slot, () -> Button)
ability to set dynamic button to multiple slots, ties in with above ^
add item.setToInv and ItemBuilder.setToInv both returning the item so it can be chained.
make most guimanager methods private
add player.closeGUI extension method.
Can onClick be used with a scoping inside a data class containing locname etc.?

---- PAGE SUPPORT ----
 */

abstract class GUI {
    private var inventory: Inventory? = null

    private var backslot: Int? = null
    private var backgui: GUI? = null
    private var requireTopInventory = true

    fun requiresClickTopInventory() = requireTopInventory

    // Manager methods
    fun open(player: Player) = Bukkit.getScheduler().runTaskLater(GUIManager.getPlugin(), Runnable {
        navigating.add(player.uniqueId) // Allows player to use navigation buttons while being forced into an inventory.
        if (inventory == null) inventory = getInventory()
        player.openInventory(inventory!!)
        navigating.remove(player.uniqueId)
        GUIManager.openGUIs[player.uniqueId] = this
        if (this is OpenEvent) this.onOpen(player)
    }, 1)

    fun onClose(player: Player) {
        // Usually only run if the player closes inventory manually.
        if (this is ForceKeepOpen && !canClose(player) && !navigating.contains(player.uniqueId)) open(player).also { return }  // Re-opens gui if not allowed to close
//        runOnClose(player)
        GUIManager.openGUIs.remove(player.uniqueId)
        if (inventory?.viewers?.size == 1) {
            inventory = null
            temporaryTasks.forEach { it.cancel() }
            temporaryTasks.clear()
        }
        if (this is CloseEvent) this.whenClosed(player)
    }

    private fun getInventory(): Inventory {
        buttons.clear()
        val inv = generateInventory()
        if (backslot != null) inv.setItem(backslot!!, RETURN)
        return inv
    }

    // Builder methods
    fun setBackButton(slot: Int, gui: GUI) = apply {
        this.backslot = slot
        this.backgui = gui
    }

    fun requireTopInventory(require: Boolean = true) = apply { this.requireTopInventory = require }

    // manager methods

    internal fun runClick(player: Player, item: ItemStack, event: InventoryClickEvent) {
        val clickdata = ClickData(player, item, event)
        if (isBackButton(item) && this.backgui != null) backgui!!.open(player).also { return }
        if (item.isButton()) {
            val button = item.getButton() ?: throw NullPointerException("Found an untracked button in a gui, this is usually a bug, join the discord for support")
            button.onClick?.invoke(clickdata)
            if (button is DynamicButton && button.updateOnClick) event.clickedInventory?.setItem(event.slot, button.getItem())
            return
        }
        onClick(clickdata)
    }

    // Public methods

    /**
     * Refreshes the inventory value by invalidating the inventory of all current viewers by overriding the existing inventory.
     * Will only take effect on re-open, for an instant effect, use #refreshContents
     *
     * @see refreshContents
     */
    fun refresh() = apply { inventory = getInventory() }

    /**
     * - Regenerates and broadcasts an inventory update to all current viewers.
     * - Is optimised to reduce flickering on high ping by only broadcasting changes to modified items
     * - If using a dynamically sized inventory use #refresh() and then re-open the inventory.
     * - You should also use #refresh() and re-open if the inventory contains buttons that are supplied outside of the generateInventory method, such as in the onOpen
     *
     * @see refresh
     */
    fun refreshContents() {
        if (inventory == null) return
        val newinv = getInventory()
        for (i in 0 until inventory!!.size) {
            val existingitem = inventory!!.getItem(i)
            val newitem = newinv.getItem(i)
            if ((existingitem == null && newitem != null) || (existingitem != null && newitem == null)) {
                inventory!!.setItem(i, newitem)
            } else if (newitem != null && existingitem != null) {
                if (existingitem != newitem) inventory!!.setItem(i, newitem)
            }
        }
    }

    // Abstract methods
    protected abstract fun generateInventory(): Inventory

    /**
     * - Run when a player clicks in the inventory
     * - This will not run when a player clicks on a button
     * - Overriding this method is optional
     */
    protected open fun onClick(click: ClickData) {}

    data class ClickData(
        val player: Player,
        val item: ItemStack,
        val event: InventoryClickEvent,
        val clickType: ClickType = event.click,
        val meta: ItemMeta? = item.itemMeta,
        val locname: String? = meta?.localizedName?.ifEmpty { null },
        val pdc: PersistentDataContainer? = meta?.persistentDataContainer
    )

    // Utility methods
    fun createInventory(name: String, size: Int, display: Inventory.() -> Unit) = Bukkit.createInventory(null, size, name.toColor()).apply(display)
    fun easyGUI(name: String, size: Int, inventory: Inventory.() -> Unit) = object: GUI() {
        override fun generateInventory(): Inventory {
            return createInventory(name, size, inventory)
        }
    }


    private fun isBackButton(item: ItemStack) = item.itemMeta?.localizedName?.equals("return") == true

    // Inventory creation utilities
    private val buttons = HashMap<Int, Button>()

    private fun ItemStack.getButton() = buttons[getId()]

    /**
     * @param action The action to execute **when this button is clicked**
     */
    protected fun Inventory.setButton(slot: Int, item: ItemStack, action: ClickData.() -> Unit): Button {
        if (!item.isValid()) throw NullPointerException("Invalid Item found in a GUI at slot $slot")
        val key = buttons.size
        val button = Button(key, item, action)
        buttons[key] = button
        setItem(slot, button.getItem())
        return button
    }

    protected fun Inventory.setDynamicButton(slot: Int, item: () -> ItemStack): DynamicButton {
        val button = DynamicButton(buttons.size, item)
        buttons[button.id] = button
        val stack = button.getItem()
        if (!stack.isValid()) throw NullPointerException("Invalid Item found in a GUI at slot $slot")
        setItem(slot, button.getItem())
        return button
    }

    // Set next for adding sequential buttons
    protected fun Inventory.setGUIButton(slot: Int, item: ItemBuilder, gui: () -> GUI) = setGUIButton(slot, item.build(), gui)
    protected fun Inventory.setGUIButton(slot: Int, item: ItemStack, gui: () -> GUI) = setButton(slot, item) { gui.invoke().open(player) }
    /**
     * Create a new GUI class on the spot!
     * @param slot the slot to place the button in
     * @param item the item display of the button
     * @param name the name of the new GUI's inventory
     * @param size the size of the new GUI's inventory
     */
    protected fun Inventory.setGUIButton(slot: Int, item: ItemStack, name: String, size: Int, display: Inventory.() -> Unit) =
        setGUIButton(slot, item) { easyGUI(name, size, display) }

    private val temporaryTasks = ArrayList<BukkitTask>()
    protected fun Inventory.setTemporaryButton(slot: Int, ticks: Int, item: ItemStack) {
        val old = getItem(slot)
        setItem(slot, item)
        temporaryTasks.add(Bukkit.getScheduler().runTaskLater(GUIManager.getPlugin(), Runnable {
            if( this.viewers.size == 0 ) return@Runnable
            setItem(slot, old)
        }, ticks.toLong()))
    }

    // Cached objects
    companion object {


        val AIR = ItemStack(Material.AIR)
        val RETURN = ItemBuilder(Material.BARRIER).name("&c&lBACK").locname("return").build()

        private var navigating = HashSet<UUID>()
    }
}
