# GShop

A Gunpowder add-on which adds shops.

## Commands

| Command                              | Permission node             | Action                                                                    |
|--------------------------------------|-----------------------------|---------------------------------------------------------------------------|
| `/shop <shopname>`                   | `gshop.viewshop.<shopname>` | Opens shop `<shopname>`.                                                  |
| `/showshop <target> <shopname>`      | `gshop.showshop`            | Opens shop `<shopname>` for `<target>`                                    |
| `/shopconfigurator export-itemstack` | `gshop.config`              | Returns the String NBT representation of the itemstack in your main hand. |

## Config

```yaml
shops:                                                      # a list of sops
- name: "default"                                           # each shop has a name
  categories:                                               # each shop is divided into categories
  - name: "items"                                           # categories have an icon and a list of entries
    icon:
      type: "ITEM_STACK"                                    # type of icons is always ITEM_STACK.
      stack: "{id:\"minecraft:apple\",Count:1b}"            # item stacks are given as stringified NBT
    entries:
    - type: "ITEM_STACK"                                    # entries have a type - currently only ITEM_STACK or COMMAND
      stack: "{id:\"minecraft:apple\",Count:1b}"
      priceToBuy: 10                                        # price to buy and sell in gunpowder currency
      priceToSell: 10
  - name: "commands"                                        # another category
    icon:
      type: "ITEM_STACK"
      stack: "{id:\"minecraft:stone_pickaxe\",Count:1b,tag:{Damage:0}}"
    entries:
      - type: "COMMAND"                                            # another type of entry
        command: "lp user --buyer-- permission set test.test true" # --buyer-- will be replaced with the buyer name
        icon: "{id:\"minecraft:stone\",Count:1b,tag:{RepairCost:0,display:{Name:'{\"text\":\"Permission Node: test.test\"}'}}}" # These strings can be copied from the `/shopconfigurator export-itemstack` command, with the stack in your hand.
        priceToBuy: 100
```