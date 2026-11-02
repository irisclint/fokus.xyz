package xyz.fokus.lyrics;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Sources {
    private static final String[][] SITES = new String[][]{
        {"YouTube Music", "youtube music"},
        {"YouTube", "youtube"},
        {"Spotify", "spotify"},
        {"SoundCloud", "soundcloud"},
        {"Apple Music", "apple music"},
        {"Deezer", "deezer"},
        {"TIDAL", "tidal"},
        {"Amazon Music", "amazon music"},
        {"Twitch", "twitch"},
        {"Kick", "kick.com", "kick"},
        {"Bandcamp", "bandcamp"},
        {"Mixcloud", "mixcloud"},
        {"Audiomack", "audiomack"},
        {"Pandora", "pandora"},
        {"Qobuz", "qobuz"},
        {"Napster", "napster"},
        {"Last.fm", "last.fm"},
        {"Anghami", "anghami"},
        {"JioSaavn", "jiosaavn"},
        {"Boomplay", "boomplay"},
        {"Yandex Music", "yandex music", "яндекс музыка"},
        {"VK Music", "vk music", "vk музыка"},
        {"TuneIn", "tunein"},
        {"radio.de", "radio.de"},
        {"Radio Garden", "radio garden"},
        {"laut.fm", "laut.fm"},
        {"Nightride FM", "nightride"},
        {"Plex", "plex"},
        {"Jellyfin", "jellyfin"},
        {"Navidrome", "navidrome"},
        {"Vimeo", "vimeo"},
        {"Dailymotion", "dailymotion"},
        {"TikTok", "tiktok"},
        {"Instagram", "instagram"},
        {"Netflix", "netflix"},
        {"Prime Video", "prime video"},
        {"Disney+", "disney+"},
        {"Crunchyroll", "crunchyroll"}
    };
    private static final Pattern DOMAIN = Pattern.compile("(?i)^(https?://)?(www\\.)?([a-z0-9-]+(\\.[a-z0-9-]+)+)/?$");

    private Sources() {
    }

    private static boolean hasWord(String text, String keyword) {
        int from = 0;

        int i;
        while ((i = text.indexOf(keyword, from)) >= 0) {
            int end = i + keyword.length();
            boolean before = i == 0 || !Character.isLetterOrDigit(text.charAt(i - 1));
            boolean after = end >= text.length() || !Character.isLetterOrDigit(text.charAt(end));
            if (before && after) {
                return true;
            }

            from = i + 1;
        }

        return false;
    }

    static String name(String app, String tab, String artist, String title) {
        String a = app.toLowerCase(Locale.ROOT);
        if (a.contains("spotify")) {
            return "Spotify";
        } else if (a.contains("applemusic") || a.contains("apple music") || a.contains("itunes")) {
            return "Apple Music";
        } else if (a.contains("deezer")) {
            return "Deezer";
        } else if (a.contains("tidal")) {
            return "TIDAL";
        } else if (a.contains("amazon")) {
            return "Amazon Music";
        } else if (a.contains("soundcloud")) {
            return "SoundCloud";
        } else if (a.contains("vlc")) {
            return "VLC";
        } else if (a.contains("foobar")) {
            return "foobar2000";
        } else if (!a.contains("zunemusic") && !a.contains("media player")) {
            String t = tab.toLowerCase(Locale.ROOT);
            if (!t.isEmpty()) {
                String rest = t;

                for (String own : new String[]{title, artist}) {
                    if (!own.isBlank()) {
                        rest = rest.replace(own.toLowerCase(Locale.ROOT), " ");
                    }
                }

                for (String[] site : SITES) {
                    for (int i = 1; i < site.length; i++) {
                        if (hasWord(rest, site[i])) {
                            return site[0];
                        }
                    }
                }

                if (t.contains(" • ")) {
                    return "Spotify";
                }
            }

            Matcher m = DOMAIN.matcher(artist.trim());
            return m.matches() ? m.group(3) : browser(a, app);
        } else {
            return "Media Player";
        }
    }

    private static String browser(String a, String app) {
        if (a.contains("msedge")) {
            return "Edge";
        } else if (a.contains("chrome")) {
            return "Chrome";
        } else if (a.contains("firefox") || a.contains("308046b0af4a39cb")) {
            return "Firefox";
        } else if (a.contains("brave")) {
            return "Brave";
        } else if (a.contains("opera")) {
            return "Opera";
        } else if (a.contains("vivaldi")) {
            return "Vivaldi";
        } else if (a.contains("arc")) {
            return "Arc";
        } else {
            String s = app.replaceAll("(?i)\\.exe$", "");
            int bang = s.lastIndexOf(33);
            return bang >= 0 ? s.substring(bang + 1) : s;
        }
    }

    static String cleanArtist(String artist) {
        String s = artist.trim();
        if (DOMAIN.matcher(s).matches()) {
            return "";
        } else {
            if (s.endsWith(" - Topic")) {
                s = s.substring(0, s.length() - 8);
            }

            if (s.endsWith("VEVO") && s.length() > 4 && !s.contains(" ")) {
                s = s.substring(0, s.length() - 4);
            }

            return s;
        }
    }
}
