# Windows .EXE Setup Guide

To set up the application as a standalone `.exe` on a Windows PC with the `.env` file accessible, you can use the built-in **`jpackage`** tool (available in JDK 14+). 

The `AppSettings.java` is already configured to search for the `.env` file in a couple of reliable fallback locations.

Since `jpackage` does not support cross-compilation (meaning you cannot build a Windows `.exe` from a Mac), you will run the build command directly on the Windows machine.

Here is the step-by-step guide to get this running:

### Step 1: Transfer the Files
Transfer your project to the Windows machine (or just the `input_jpackage` folder containing the shaded Uber-JAR: `java-order-escpos-printer-1.0-SNAPSHOT.jar`).

### Step 2: Ensure JDK is Installed
Make sure you have **Java JDK 17** (or newer) installed on the Windows machine and that `jpackage` is available in your Command Prompt/PowerShell. You can verify this by running:
```cmd
jpackage --version
```

### Step 3: Run the `jpackage` Command
Open Command Prompt or PowerShell, navigate to the directory containing your `input_jpackage` folder, and run the following command to create a standalone application image:

```cmd
jpackage --type app-image --name OrderPrinter --input input_jpackage --main-jar java-order-escpos-printer-1.0-SNAPSHOT.jar --main-class com.escpos.printer.Main
```

This will create a new folder named `OrderPrinter` in your current directory. 
*Note: This folder will contain `OrderPrinter.exe` alongside a bundled Java runtime (`runtime` folder). This means you can distribute this whole folder to any other Windows PC, and it will run without needing Java installed on those machines!*

### Step 4: Placing the `.env` File
Based on the `AppSettings.java` configuration, you have two excellent choices for where to place the `.env` file on the Windows machine:

**Option A: The App Directory (Portable approach)**
You can place the `.env` file directly inside the newly generated `OrderPrinter` folder, right next to `OrderPrinter.exe`. Because `Dotenv` checks the current working directory, it will find it when you double-click the executable.

**Option B: The User Home Directory (Most robust approach)**
A fallback is implemented in the code:
```java
env = Dotenv.configure().directory(System.getProperty("user.home") + "/.rodizio").ignoreIfMissing().load();
```
Because of this, you can just create a `.rodizio` folder in the Windows User's home directory and put the `.env` there:
1. Open File Explorer and go to `C:\Users\<YourWindowsUsername>\`
2. Create a folder named `.rodizio`
3. Place your `.env` file inside this folder (so the exact path becomes `C:\Users\<YourWindowsUsername>\.rodizio\.env`).

Option B is highly recommended for Windows because it avoids any issues that might arise if you create a Desktop Shortcut (which can sometimes mess with the "Current Working Directory" context of the `.exe`).
