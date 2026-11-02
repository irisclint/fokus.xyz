# Reports what Windows says is playing - Spotify, Apple Music, or any player in a browser tab
# (YouTube, YouTube Music, SoundCloud, Deezer, Tidal, Twitch, Kick ...) - as one JSON line per
# second. When several apps have a session, the one that is playing wins. Read-only: it never
# controls playback.
#
# Two extras, both only when the song changes:
#  - the cover Windows shows is written to the file given as the first argument;
#  - for a browser, the title of the tab that plays it, read from the browser's tab strip, so the
#    mod can tell which site it is. Web page contents are never read.
$ErrorActionPreference = 'SilentlyContinue'
# Every title in every script must arrive intact. The console's own code page would turn
# Japanese, Cyrillic, Arabic ... (and the bullet Spotify puts in its tab) into question marks, so
# the output is UTF-8 and, on top of that, plain ASCII: everything else is written as a JSON
# \uXXXX escape, which no code page can damage. This file itself stays ASCII too - Windows
# PowerShell reads a script without a byte-order mark in the local code page.
[Console]::OutputEncoding = New-Object System.Text.UTF8Encoding $false
function Ascii-Json([string]$json) {
    $sb = New-Object System.Text.StringBuilder
    foreach ($ch in $json.ToCharArray()) {
        if ([int]$ch -lt 128) { [void]$sb.Append($ch) } else { [void]$sb.AppendFormat('\u{0:x4}', [int]$ch) }
    }
    $sb.ToString()
}
$coverFile = if ($args.Count -gt 0 -and $args[0] -ne 'once') { $args[0] } else { $null }
$once = $args -contains 'once'

Add-Type -AssemblyName System.Runtime.WindowsRuntime
Add-Type -AssemblyName UIAutomationClient, UIAutomationTypes
$asTask = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object {
    $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and
    $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]
function Await($op, [Type]$type) {
    $task = $asTask.MakeGenericMethod($type).Invoke($null, @($op))
    $task.Wait(5000) | Out-Null
    $task.Result
}
[Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType = WindowsRuntime] | Out-Null
[Windows.Storage.Streams.IRandomAccessStreamWithContentType, Windows.Storage.Streams, ContentType = WindowsRuntime] | Out-Null
$managerType = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]
$propsType = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties]
$streamType = [Windows.Storage.Streams.IRandomAccessStreamWithContentType]
$manager = Await ($managerType::RequestAsync()) $managerType
$asStream = [System.IO.WindowsRuntimeStreamExtensions].GetMethod('AsStreamForRead',
    [Type[]]@([Windows.Storage.Streams.IInputStream]))

# Browser sessions name the browser, never the site. Process names per browser.
$browsers = [ordered]@{
    'brave' = 'brave'; 'msedge' = 'msedge'; 'chrome' = 'chrome'; '308046B0AF4A39CB' = 'firefox';
    'firefox' = 'firefox'; 'opera' = 'opera'; 'vivaldi' = 'vivaldi'; 'arc' = 'arc'; 'zen' = 'zen';
    'librewolf' = 'librewolf'; 'floorp' = 'floorp'; 'waterfox' = 'waterfox'; 'yandex' = 'browser'
}
$A = [System.Windows.Automation.AutomationElement]
$CT = [System.Windows.Automation.ControlType]
$walker = [System.Windows.Automation.TreeWalker]::ControlViewWalker

# The names of the tabs in one browser window. Walks the window's frame only: a web page
# (Document) is never entered, which keeps it quick and leaves the page alone.
function Tab-Names($window) {
    $names = New-Object System.Collections.Generic.List[string]
    $queue = New-Object System.Collections.Generic.Queue[object]
    $queue.Enqueue(@($window, 0))
    $seen = 0
    while ($queue.Count -gt 0 -and $seen -lt 600) {
        $item = $queue.Dequeue(); $el = $item[0]; $depth = $item[1]; $seen++
        $child = $walker.GetFirstChild($el)
        while ($child) {
            $type = $child.Current.ControlType
            if ($type -eq $CT::TabItem) {
                $names.Add($child.Current.Name)
            } elseif ($type -ne $CT::Document -and $depth -lt 14) {
                $queue.Enqueue(@($child, $depth + 1))
            }
            $child = $walker.GetNextSibling($child)
        }
    }
    $names
}

# The tab that plays this title: its name holds the title, or the browser marks it as playing.
function Playing-Tab($app, $title) {
    $proc = $null
    foreach ($key in $browsers.Keys) { if ($app -match [regex]::Escape($key)) { $proc = $browsers[$key]; break } }
    if (-not $proc) { return '' }
    $pids = @(Get-Process -Name $proc | ForEach-Object Id)
    if ($pids.Count -eq 0) { return '' }
    $short = if ($title.Length -gt 24) { $title.Substring(0, 24) } else { $title }
    $audible = ''
    $root = $A::RootElement
    foreach ($w in $root.FindAll([System.Windows.Automation.TreeScope]::Children,
            [System.Windows.Automation.Condition]::TrueCondition)) {
        if ($pids -notcontains $w.Current.ProcessId) { continue }
        $names = @($w.Current.Name) + @(Tab-Names $w)
        foreach ($n in $names) {
            if (-not $n) { continue }
            if ($short -and $n.Contains($short)) { return $n }
            if (-not $audible -and $n -match '(?i)audio|wiedergabe|playing|sound') { $audible = $n }
        }
    }
    $audible
}

function Cover-Bytes($props) {
    if (-not $props.Thumbnail) { return $null }
    $ras = Await ($props.Thumbnail.OpenReadAsync()) $streamType
    if (-not $ras) { return $null }
    # Windows PowerShell cannot hand the WinRT stream to the overload directly; reflection can.
    $stream = $asStream.Invoke($null, @($ras))
    $mem = New-Object System.IO.MemoryStream
    $stream.CopyTo($mem)
    $stream.Dispose()
    $mem.ToArray()
}

$lastKey = ''
$coverId = 0
$coverTries = 0
$lastCoverLength = -1
$tab = ''
$tabTries = 0
while ($true) {
    $line = '{}'
    $session = $null
    foreach ($s in $manager.GetSessions()) {
        if ($s.GetPlaybackInfo().PlaybackStatus.ToString() -eq 'Playing') { $session = $s; break }
    }
    if (-not $session) { $session = $manager.GetCurrentSession() }
    if ($session) {
        $props = Await ($session.TryGetMediaPropertiesAsync()) $propsType
        $timeline = $session.GetTimelineProperties()
        $info = $session.GetPlaybackInfo()
        $app = $session.SourceAppUserModelId
        $key = "$app|$($props.Title)|$($props.Artist)"
        if ($key -ne $lastKey) {
            $lastKey = $key
            $coverTries = 6
            $lastCoverLength = -1
            $tab = Playing-Tab $app $props.Title
            $tabTries = 10
        }
        # A site renames its tab a moment after the song changes: look again every few seconds
        # until the tab carries the new title.
        $short = if ($props.Title.Length -gt 24) { $props.Title.Substring(0, 24) } else { $props.Title }
        if ($tabTries -gt 0 -and -not ($tab -and $tab.Contains($short)) -and ((Get-Date).Second % 3 -eq 0)) {
            $tabTries--
            $found = Playing-Tab $app $props.Title
            if ($found) { $tab = $found }
        }
        # Browsers often hand the artwork over a moment after the title, so look for a few seconds.
        if ($coverFile -and $coverTries -gt 0) {
            $coverTries--
            $bytes = Cover-Bytes $props
            if ($bytes -and $bytes.Length -ne $lastCoverLength) {
                $lastCoverLength = $bytes.Length
                [System.IO.File]::WriteAllBytes($coverFile, $bytes)
                $coverId++
            } elseif (-not $bytes -and $lastCoverLength -ne 0) {
                $lastCoverLength = 0
                $coverId++
                Remove-Item -LiteralPath $coverFile
            }
        }
        $line = [ordered]@{
            title    = $props.Title
            artist   = $props.Artist
            album    = $props.AlbumTitle
            position = $timeline.Position.TotalSeconds
            duration = $timeline.EndTime.TotalSeconds
            updated  = $timeline.LastUpdatedTime.ToUnixTimeMilliseconds()
            status   = $info.PlaybackStatus.ToString()
            app      = $app
            tab      = $tab
            cover    = if ($lastCoverLength -gt 0) { $coverId } else { 0 }
        } | ConvertTo-Json -Compress
    }
    [Console]::Out.WriteLine((Ascii-Json $line))
    [Console]::Out.Flush()
    if ($once) { break }
    Start-Sleep -Milliseconds 1000
}
