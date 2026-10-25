# MyPlugin — Spigot 1.8.8 Starter Project

## One-time setup

1. **Build the Spigot 1.8.8 API/server jar** (Mojang's EULA means you can't download prebuilt jars, so BuildTools compiles it locally):
   ```bash
   mkdir ~/BuildTools && cd ~/BuildTools
   curl -O https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
   java -jar BuildTools.jar --rev 1.8.8
   ```
   Run this with a **Java 8 JDK** — newer JDKs often fail to build old Minecraft versions.

   This installs `spigot-1.8.8-R0.1-SNAPSHOT.jar` into your local Maven repo (`~/.m2`), which is what this project's `pom.xml` depends on.

## Opening in IntelliJ

1. **File → Open** and select this `MyPlugin` folder (the one containing `pom.xml`).
2. IntelliJ should detect it as a Maven project automatically. If not: right-click `pom.xml` → **Add as Maven Project**.
3. Make sure a JDK 8 is configured: **File → Project Structure → Project → SDK**.
4. Let Maven reload/import — it'll pull `spigot-api` from your local `.m2` repo.

## Building

```bash
mvn clean package
```

The output jar will be at `target/MyPlugin.jar`.

## Testing locally

1. Make a separate folder for your test server, put the built `spigot-1.8.8.jar` (from the BuildTools step) in it.
2. Run it once: `java -jar spigot-1.8.8.jar` — accept the EULA when it generates `eula.txt`.
3. Drop `target/MyPlugin.jar` into that server's `plugins/` folder.
4. Start the server again and run `/hello` in-game or console to test the sample command.

## Project layout

```
MyPlugin/
├── pom.xml
├── src/main/java/com/yourname/myplugin/MyPlugin.java   ← main plugin class
├── src/main/resources/plugin.yml                        ← plugin metadata
└── README.md
```

Rename the package (`com.yourname.genericspawners`), class, and `plugin.yml` fields to match your actual plugin name before you start building it out.
