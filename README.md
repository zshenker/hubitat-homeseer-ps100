# HomeSeer PS100 mmWave Presence Sensor — Hubitat Driver

A [Hubitat Elevation](https://hubitat.com/) driver for the
[HomeSeer PS100](https://docs.homeseer.com/products/ps100), a 24 GHz mmWave
radar presence sensor.

The PS100 is a USB-C powered, always-on Z-Wave Plus device that detects very
small movements (true *presence*, not just motion) at up to ~19.6 ft / 600 cm
across a 120° field of view. This driver surfaces presence/motion to Hubitat and
exposes the device's Z-Wave configuration parameters (per-range sensitivity,
no-motion timeout, and front LED behavior/colors) as driver preferences.

> **Note:** This is a community driver and is not affiliated with or endorsed by
> HomeSeer. HomeSeer's compatibility guide lists Hubitat as *partially
> compatible* — the radio reports presence/no-presence reliably; advanced LED
> color control depends on your hub being able to send parameter commands in
> automations.

## Features

- **Presence** (`present` / `not present`) and **Motion** (`active` /
  `inactive`) attributes — works with both presence- and motion-based apps.
- Full per-range **sensitivity** configuration (8 distance zones).
- Configurable **no-motion timeout** (10–3600 s).
- **Motion LED** enable/disable and steady **left/right LED colors**.
- Reads back firmware version and manufacturer/product IDs into Device Data.
- S0/S2 secure pairing supported (commands are auto-encapsulated).

## Capabilities

`PresenceSensor`, `MotionSensor`, `Sensor`, `Configuration`, `Refresh`

## Installation

### Option A — Hubitat Package Manager (recommended)

1. Install [Hubitat Package Manager](https://hubitatpackagemanager.hubitatcommunity.com/) if you haven't already.
2. In HPM, choose **Install → Search by Keywords** and search for
   `PS100` (once this package is added to the community repository), **or**
   choose **Install → From a URL** and paste the manifest URL:
   ```
   https://raw.githubusercontent.com/zshenker/hubitat-homeseer-ps100/main/packageManifest.json
   ```
3. Follow the prompts to install. HPM will keep the driver up to date.

### Option B — Manual

1. In Hubitat, go to **Drivers Code → New Driver → Import**.
2. Paste this URL and click **Import**, then **Save**:
   ```
   https://raw.githubusercontent.com/zshenker/hubitat-homeseer-ps100/main/drivers/homeseer-ps100-presence-sensor.groovy
   ```

## Pairing the device

1. Power the PS100 with the included USB-C supply. The two front LEDs flash
   blue → red → green when active.
2. Put your Hubitat into Z-Wave inclusion (**Settings → Z-Wave Details → Discover Devices**).
3. Briefly press the **red Z-Wave button** on the back of the PS100.
4. After inclusion, open the new device and set the driver to
   **HomeSeer PS100 mmWave Presence Sensor** if it wasn't auto-assigned, then
   click **Save Device** and **Configure**.

**Factory reset:** press and hold the red Z-Wave button for ~30 seconds (only if
the controller is missing or the device is unresponsive). Re-include afterward.

## Supported hardware revisions

There are two PS100 Z-Wave revisions; both are supported and auto-detected by
product ID. Parameters that only exist on one revision are shown in the
preferences only once that unit is identified (after the first **Configure**).

| Revision | Mfr | Product Type | Product ID | Notable params |
|----------|-----|--------------|------------|----------------|
| PS100 (original, "HS-PS100") | `000C` | `0204` | `0002` | has param **2** (distance report); **no** LED color |
| PS100 v2 | `000C` | `0204` | `0003` | has params **20/21** (LED colors); **no** param 2 |

## Configuration parameters

These map to the
[HomeSeer Z-Wave parameter spec](https://docs.homeseer.com/products/ps100-specs-z-wave-parameters)
and the [Z-Wave JS device database](https://devices.zwave-js.io/?jumpTo=0x000c:0x0204:0x0002),
and are editable from the device preferences:

| # | Preference | Size | Default (orig / v2) | Range / Options |
|---|------------|------|---------------------|-----------------|
| 1 | No-motion timeout (s) | 4 | 10 | 10–3600 |
| 2 | Distance report interval (s) *(original only)* | 4 | 10 | 2–600 |
| 3 | Bluetooth radio | 1 | Disabled | Disabled / Enabled |
| 4 | Motion LED | 1 | Enabled | Disabled / Enabled |
| 6 | Sensitivity 0–75 cm | 1 | 50 / 50 | 0–100 (0 = off) |
| 7 | Sensitivity 75–150 cm | 1 | 50 / 50 | 0–100 |
| 8 | Sensitivity 150–225 cm | 1 | 50 / 0 | 0–100 |
| 9 | Sensitivity 225–300 cm | 1 | 50 / 0 | 0–100 |
| 10 | Sensitivity 300–375 cm | 1 | 90 / 0 | 0–100 |
| 11 | Sensitivity 375–450 cm | 1 | 90 / 0 | 0–100 |
| 12 | Sensitivity 450–525 cm | 1 | 90 / 0 | 0–100 |
| 13 | Sensitivity 525–600 cm | 1 | 90 / 0 | 0–100 |
| 20 | Left LED color *(v2 only)* | 1 | Off | Off/Red/Green/Blue/Magenta/Yellow/Cyan/White |
| 21 | Right LED color *(v2 only)* | 1 | Off | Off/Red/Green/Blue/Magenta/Yellow/Cyan/White |

After changing preferences, click **Save Preferences** — the driver pushes the
changed values to the device and re-reads them to confirm. Parameters you leave
untouched keep the device's own factory values (the driver does not overwrite
them on install).

### Tuning tips (from HomeSeer)

- Enable only the distance ranges that cover your actual monitored area; disable
  ranges beyond it (set sensitivity to `0`).
- Reduce sensitivity in ranges affected by ceiling fans, HVAC vents, or pets.
- `100` can cause the sensor to "stick" in the present state in some rooms —
  `90` is the most sensitive practical value in most environments.

## Repository layout

```
.
├── drivers/
│   └── homeseer-ps100-presence-sensor.groovy   # the driver
├── packageManifest.json                        # HPM package manifest
├── repository.json                             # HPM author repository listing
├── CHANGELOG.md
├── LICENSE
└── README.md
```

## Important: update the placeholder URLs

This repo assumes the GitHub path `zshenker/hubitat-homeseer-ps100` and the
`main` branch. If your GitHub username, repo name, or default branch differ,
update the raw URLs in:

- `drivers/homeseer-ps100-presence-sensor.groovy` (`importUrl`)
- `packageManifest.json` (`location`, `documentationLink`, `id`s stay unique)
- `repository.json` (`gitHubUrl`, `location`)

The driver ships with verified fingerprints for both PS100 revisions
(`000C:0204:0002` and `000C:0204:0003`), so it should auto-assign on inclusion.
If yours reports different IDs under **Device → Device Details → Data**, let me
know and add a matching `fingerprint` line.

## Adding to Hubitat Package Manager's public list

To make this discoverable via HPM search, submit a PR adding your
`repository.json` raw URL to the community
[repositories list](https://github.com/HubitatCommunity/hubitatpackagemanager)
(`repositories.json`). See the
[HPM developer docs](https://hubitatpackagemanager.hubitatcommunity.com/devs1.html).

## License

[Apache License 2.0](LICENSE)
