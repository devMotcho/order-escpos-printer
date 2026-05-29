# Software Deployment Guide

This guide details how to compile, configure, and install new versions of the Rodizio Order Printer software on target machines (macOS or Windows).

## The Environment Configuration (`.env`)

To ensure cross-platform compatibility and a seamless user experience, the application has been designed to look for its configuration in two primary locations:

### 1. Next to the Application (Recommended)
You can simply place the `.env` file directly next to the generated `.app` bundle (on macOS) or `.exe` file (on Windows). The application will dynamically determine its installation path and read the configuration file from there. If you move the application, just remember to move the `.env` file with it.

### 2. Standard Global Location
Alternatively, you can place the `.env` file in a standard location on the target machine:
`~/.rodizio/.env` (which corresponds to `/Users/Username/.rodizio/.env` on macOS or `C:\Users\Username\.rodizio\.env` on Windows).

**Step 1: Configuration**
1. Open your `.env` file and configure the parameters for the specific restaurant (such as `PRINTER_IP`, `USERNAME`, etc.).
2. Decide whether to distribute the `.env` file alongside the native executable or install it globally in `~/.rodizio/`.

## Application Compilation and Packaging

### Step 2: Build the Standalone JAR (Fat JAR)
On your development machine, after making changes to the source code, open a terminal in the `java-order-escpos-printer` directory and compile the project into a standalone JAR file:
```bash
mvn clean package
```
This generates a single `java-order-escpos-printer-1.0-SNAPSHOT.jar` file inside the `target/` folder containing all necessary dependencies.

### Step 3: Create the Native Executable

**For macOS (`.app`):**
To generate a professional macOS Application Bundle complete with a custom icon, use the `jpackage` tool:
```bash
mkdir -p input_jpackage
cp target/java-order-escpos-printer-1.0-SNAPSHOT.jar input_jpackage/
jpackage --type app-image \
  --name "printer2" \
  --input input_jpackage \
  --main-jar java-order-escpos-printer-1.0-SNAPSHOT.jar \
  --main-class com.escpos.printer.Main \
  --icon src/main/resources/MyIcon.icns \
  --dest dist
```
*Note: Make sure your icon has been converted to `.icns` format. The resulting `printer2.app` will be available in the `dist/` folder.*

**macOS Icon Caching Troubleshooting:**
If the app's custom icon doesn't show up in Finder but appears correctly in the Dock or `Cmd + Tab` menu, this is due to macOS's aggressive icon caching. To fix this, rename the `.app` bundle or move it to a different directory (like the Desktop) to force Finder to reload the icon.

**For Windows (`.exe`):**
Because macOS cannot compile native Windows executables directly, copy the standalone `java-order-escpos-printer-1.0-SNAPSHOT.jar` to a Windows machine with Java 14+ installed and run:
```cmd
jpackage --type exe --name "RodizioPrinter" --input target --main-jar java-order-escpos-printer-1.0-SNAPSHOT.jar --main-class com.escpos.printer.Main --win-shortcut
```

### Step 4: Installation
Distribute the resulting `.app` (macOS) or `.exe` installer (Windows) to the target machine. Ensure the `.env` configuration file is either placed next to the executable or in `~/.rodizio/` (as described in Step 1), and the application will run natively and correctly on double-click.
