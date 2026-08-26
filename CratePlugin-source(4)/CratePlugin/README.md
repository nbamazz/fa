# CratePlugin

A crate/key plugin for Paper/Spigot 1.20.x with a CS2-style spinning case animation.

## Building the .jar

I couldn't compile this in my sandbox (it has no internet access to the PaperMC
Maven repo), so build it yourself — it's one command:

**Option A — locally (needs Java 17+ and Maven):**
```
mvn package
```
The finished jar appears at `target/CratePlugin.jar`. Drop it in your server's `/plugins` folder and restart.

**Option B — no local install needed:**
Push this folder to a GitHub repo. The included `.github/workflows/build.yml`
will build it automatically — download the jar from the Actions tab afterward.

## Commands (all under `/crate`)

Admin (`crate.admin`, defaults to OP):
- `/crate create <id>` — define a new crate
- `/crate setblock <id> <material>` — look at any block within 6 blocks and it becomes that crate (placed as `<material>`, e.g. `ENDER_CHEST`, `CHEST`, `BARREL`, any block you want)
- `/crate addreward <id> <material|PRIME_PICKAXE|EXCAVATOR_PICKAXE> <amount> <weight> <enchants|none> <name...>` — add a possible prize, weight controls odds. Enchants use `name:level,name:level` format (e.g. `sharpness:5,unbreaking:3`), or `none` for no enchants. Levels can go above vanilla max. `PRIME_PICKAXE` and `EXCAVATOR_PICKAXE` are two separate custom pickaxes that each mine a 3x3 area in one hit (diamond-based and netherite-based respectively) — add either to any crate as its own reward.
- `/crate removecrate <id>`
- `/crate key <player> <keyId> <amount>` — give one player keys
- `/crate keyall <keyId> <amount>` — **gives every online player `<amount>` keys of whichever `<keyId>` you choose**
- `/crate open <id>` — test-open a crate yourself, no key needed
- `/crate reload`

Everyone (`crate.use`, default true):
- `/crate list`
- Right-click a bound crate block while holding the matching key to open it (spins, then grants the prize)

## Setting up the Prime Case example

```
/crate create prime
/crate setblock prime ENDER_CHEST
/crate addreward prime PRIME_PICKAXE 1 5 none Prime Pickaxe
/crate addreward prime EXCAVATOR_PICKAXE 1 3 none Excavator Pickaxe
/crate addreward prime NETHERITE_SWORD 1 10 sharpness:5,unbreaking:3 Netherite Sword
/crate addreward prime DIAMOND 8 30 none Diamonds
/crate addreward prime EMERALD 1 65 none Emerald
/crate keyall prime_key 1
```
Now every online player has a key; right-clicking the ender chest you bound spins the reel and, if they land on the pickaxe, grants a Diamond Pickaxe (Efficiency III) that mines a 3x3 area in one hit.

## Config

`config.yml` controls the spin animation speed/duration and all chat messages
(colors use `&` codes). Crate definitions themselves live in `crates.yml`,
generated automatically as you use the commands above — no need to hand-edit it.
