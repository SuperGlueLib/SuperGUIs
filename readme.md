# SuperGlue - SuperGUIs
## Kotlin library for creating and handling spigot GUIs
<br>
Set it up before use (often in the onEnable) using 
`GUIManager.setup(JavaPlugin)`



to get started, make a class for your new gui by extending the 
GUI class, then implement the generateInventory() method.
The generateInventory method is most often followed by the `createInventory(...)` 
method which allows you to easily create and customise your menu.

Here is an example GUI which creates an inventory with 3 rows
and sets the border to white panes, and fills the rest of the
empty slots with black stained glass panes.
```kt
class Example: GUI() {
    override fun generateInventory() {
        val diamond = ItemBuilder(Material.DIAMOND, "&bShiny!").build()
        return createInventory("Inventory Name", 27) {
            setItem(13, diamond)
            setBorder(Panes.WHITE)
            fillEmpty(Panes.BLACK)            
        }
    } 
}
```

For support or suggestions, join the 
[discord sever](https://discord.gg/cAtj5Ue2mC)
