# PNG Icon Generator

A small Java 8 Swing utility that takes a single source PNG and generates the
full set of icon sizes needed for `JFrame.setIconImages()`:

```
icon-16.png
icon-32.png
icon-48.png
icon-64.png
icon-256.png
```

No external dependencies — just `javax.imageio` and `Graphics2D`.

## Why

Swing's `setIconImages(List<Image>)` expects several pre-scaled PNGs so
Windows can pick the right resolution for the title bar, Alt+Tab, taskbar,
and shortcut icons. Manually exporting five sizes from an image editor every
time you tweak a logo gets old fast — this tool does it in one click.

## Features

- Drop in one PNG (ideally 256×256 or larger, square) and generate all five
  standard sizes in one pass
- High-quality downscaling using a multi-step bicubic resize (halving the
  image repeatedly before the final pass) instead of a single low-quality
  scale, which avoids the muddy/aliased look you get scaling e.g. 1024→16
  in one step
- Live preview of the source image before generating
- Remembers your last-used source and output folders between runs
  (`java.util.prefs.Preferences`)
- Custom `AppTheme` class: a light UI with a magenta→red 3D gradient button
  style (rounded corners, glossy highlight, hover/press states), built with
  a custom `ButtonUI` — no extra look-and-feel libraries required

## Requirements

- Java 8 or later (JDK, for building; JRE is enough to run a compiled jar)

## Usage

1. Launch the app.
2. Click **Browse...** next to *Source PNG* and pick your image.
3. Click **Browse...** next to *Output folder* and pick where the icons
   should be written (defaults to the source image's folder).
4. Click **Generate Icons**.
5. Drop the resulting `icon-*.png` files into your project's resources
   folder and load them with `setIconImages()`:

```java
List<Image> icons = new ArrayList<>();
for (String size : new String[]{"16", "32", "48", "64", "256"}) {
    URL url = getClass().getResource("/resources/icon-" + size + ".png");
    if (url != null) {
        icons.add(Toolkit.getDefaultToolkit().getImage(url));
    }
}
if (!icons.isEmpty()) {
    setIconImages(icons);
}
```

## Building

This is a NetBeans project (Ant-based). Open it in NetBeans and run/build
normally, or build from the command line with Ant:

```bash
ant build
ant run
```

## Project structure

- `PngIconGenerator.java` — the UI and the resize/export logic
- `AppTheme.java` — reusable UIManager theming + custom gradient button UI,
  drop it into other Swing projects as-is

## License

MIT — do whatever you like with it.
