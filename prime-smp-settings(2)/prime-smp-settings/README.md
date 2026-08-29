# PrimeSMPSettings

A Paper/Spigot plugin for **Prime SMP** with two per-player toggles:

- **Chat** – when disabled for a player, their own chat messages are cancelled (they see a notice; nobody else's chat is affected).
- **Mob spawning** – when disabled for a player, "environmental" mob spawns (natural spawns, raids, patrols, drowned, jockeys, reinforcements, etc.) within 128 blocks of *that player specifically* are cancelled. Spawner farms, spawn eggs, breeding, and command/plugin-spawned mobs are left alone. Other players nearby still get normal mob spawns unless they've also toggled it off.

Settings persist across restarts in `plugins/PrimeSMPSettings/data.yml`.

## Requirements

- Java 17+
- A Paper (or Spigot) server, 1.20.x
- Maven, to build the jar

## Building

```
mvn package
```

This pulls `paper-api` from PaperMC's Maven repo (declared in `pom.xml`), so you'll need internet access when building. The output jar is at:

```
target/prime-smp-settings.jar
```

Drop that into your server's `plugins/` folder and restart (or `/reload`, though a restart is safer).

## Building via GitHub Actions (no local Java/Maven needed)

This project includes `.github/workflows/build.yml`, which builds the jar automatically in the cloud whenever you push.

1. Create a new **repository** on GitHub (public or private, doesn't matter).
2. Upload this whole `prime-smp-settings` folder to it — either drag-and-drop through the GitHub web UI ("Add file" → "Upload files"), or via git:
   ```
   cd prime-smp-settings
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/<your-username>/<your-repo>.git
   git push -u origin main
   ```
3. On GitHub, open your repo's **Actions** tab. You should see a "Build Plugin" workflow run start automatically (or click "Run workflow" if you used `workflow_dispatch`).
4. Wait for the green checkmark (usually under a minute).
5. Click into that run, scroll to **Artifacts**, and download `prime-smp-settings-jar` — it's a zip containing `prime-smp-settings.jar`.
6. Drop that jar into your server's `plugins/` folder and restart.

Every time you push a change to `main`, GitHub rebuilds the jar for you automatically — no local setup required.

## Commands

All under `/primesettings` (feel free to add an alias in `plugin.yml`, e.g. `/psettings`):

| Command | Effect |
|---|---|
| `/primesettings chat` | Toggle your own chat on/off |
| `/primesettings chat on\|off` | Explicitly set your own chat |
| `/primesettings chat <player> [on\|off]` | (staff) toggle/set another player's chat |
| `/primesettings mobs` | Toggle your own mob spawning on/off |
| `/primesettings mobs on\|off` | Explicitly set your own mob spawning |
| `/primesettings mobs <player> [on\|off]` | (staff) toggle/set another player's mob spawning |
| `/primesettings status [player]` | Check current settings |

## Permissions

| Node | Default | Meaning |
|---|---|---|
| `primesmp.settings.self` | true (everyone) | Can change their own settings |
| `primesmp.settings.others` | op | Can change/view other players' settings |

## Notes / things you may want to tweak

- **Mob spawn radius**: currently hardcoded to 128 blocks in `MobSpawnListener.java` (matches vanilla's natural spawn range around a player). Pull this into `config.yml` if you want it adjustable without recompiling.
- **Which spawn reasons count**: the blocked set is in `MobSpawnListener.BLOCKED_REASONS`. Add/remove `CreatureSpawnEvent.SpawnReason` values there if you want it stricter or looser (e.g. add `SpawnReason.SPAWNER` if you also want to shut off spawners near that player).
- **Chat event**: uses the legacy `AsyncPlayerChatEvent` for broad Spigot/Paper compatibility. If your server is Paper-only and you want Adventure `Component` support, swap to `io.papermc.paper.event.player.AsyncChatEvent`.
- No GUI — everything is command-driven. Happy to add a `/primesettings` inventory menu if you'd rather players click toggles than type commands.
