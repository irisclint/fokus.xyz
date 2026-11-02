# fokus.xyz

PvP-Mod für **Minecraft 26.1, 26.2 und 26.3** (Fabric) – eine einzige Jar für alle drei Versionen.
Link: https://flexbio.link/1

## Installieren

Die neueste `fokus.xyz.jar` aus den [Releases](https://github.com/irisclint/fokus.xyz/releases)
in den `mods`-Ordner ziehen, fertig. Fabric API wird **nicht** gebraucht. Den Dateinamen nicht
ändern (der Iris Client schaltet Mods ab, deren Name eine andere Minecraft-Version nennt).

Updates kommen von selbst: Beim Start prüft die Mod die neueste Release, lädt sie im Hintergrund,
prüft die SHA-256-Prüfsumme und tauscht die Datei, wenn Minecraft geschlossen wird.

## Menü

ESC → Knopf **fokus.xyz**, oder im Spiel die rechte Strg-Taste. Linksklick an/aus, Rechtsklick
Einstellungen, Mittelklick Taste belegen, einfach lostippen zum Suchen. Englisch und Deutsch.

## Was drin ist

- **PvP:** Toggle Sprint/Sneak, Zoom, Freelook, Festes Sichtfeld, Custom Crosshair (12 Stile, pixelgenau), Leben im Namen, Hitmarker,
  Hit Sounds, No Hurt Cam.
- **HUD (über 30 Teile):** Wasserzeichen, FPS, CPS, Ping, Koordinaten, Uhr, Speed, Server,
  Keystrokes, Rüstung, Effekte, Target HUD, Kampf-Timer, Session-Statistik (Kills, Tode, K/D), Schadenszahlen, Bogen-Anzeige, Wenig-Leben-Warnung, Combo, Reach, Item-Zähler, Totem Pops, Cooldowns,
  Kompass, Todespunkt (Chat + Pfeil mit Entfernung), Spieler in der Nähe, Hunger, Rüstungswarnung, Spielzeit, Tageszeit, Biom, RAM, TPS,
  Lichtlevel, Haltbarkeit – alles im HUD-Editor verschiebbar.
- **Chat & Tab:** Ping in der Tab-Liste als farbige Zahl, Zeitstempel vor jeder Nachricht (drei Formate, eigene Klammern und Farbe), gleiche
  Nachrichten hintereinander werden zu einer Zeile mit Zähler gestapelt.
- **Musik:** Now Playing mit Cover und Lyrics – am Himmel oder auf dem Bildschirm, jede App und
  jede Seite im Browser, Titel und Liedtexte in jeder Sprache (lrclib.net, NetEase).
- **Animationen:** Schwung-Stile, Blockhit, Item-Ansicht pro Item-Art, Wechsel- und
  Inventar-Animation.
- **Visuals:** Item-Aura (Leuchten mit Farbverlauf um Totems, Perlen & Co. – in Hotbar, Inventar und Welt),
  Treffer-Farbe, Partikel (mehr Krits, immer Schärfe, ohne Explosionen/Totem), End-Kristalle, Totems, Better Items (17 Gruppen, 14 Stile), Himmel (Farben, Wolken,
  Mond, Tag & Nacht), Klares Wetter, Klare Sicht (auch ohne Blindheit- und Dunkelheit-Nebel), Block-Umrandung (Farbe, Breite), Fullbright, Low Fire.
- **Profile & Teilen:** drei Setups; das eigene Setup als Text an Freunde schicken.

## Bauen und veröffentlichen

Java 25:

```
gradlew build                              # Jar in build/libs/
gradlew build -Pminecraft_version=26.1     # gegen eine andere Version prüfen
```

Neue Version: `mod_version` in `gradle.properties` erhöhen, dann
`powershell -ExecutionPolicy Bypass -File release.ps1 -Notes "Was neu ist"` – baut, legt Jar,
`update.json` und ZIP in `dist\<version>\` ab und erstellt die GitHub-Release.

Gebaut wird gegen 26.3; was sich zwischen den Versionen unterscheidet (Tasten GLFW/SDL, Gui/Hud,
Hand-Renderer, umbenannte Methoden), wird zur Laufzeit aufgelöst. `FokusMixinPlugin` wendet
jedes Mixin nur an, wenn seine Zielklasse und -methode in der laufenden Version existiert.
