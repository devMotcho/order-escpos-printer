# Windows .EXE Setup Guide (Fresh Clone)

This is the foolproof, step-by-step guide to building and running this application from a fresh clone on a Windows machine. 

To prevent any Windows Command Prompt quirks, **open PowerShell as an Administrator** and follow these exact steps:

### Step 1: Verify Prerequisites
Make sure you have Java JDK 17 (or newer) and Maven installed. Run these to verify:
```powershell
java -version
mvn -version
```

### Step 2: Navigate to the project
In PowerShell, navigate to the folder containing your `pom.xml` file.
```powershell
cd C:\path\to\your\cloned\repo\java-order-escpos-printer
```

### Step 3: Compile the Code
Run Maven to download all dependencies and compile the `.jar` file:
```powershell
mvn clean package
```
*(Wait for the "BUILD SUCCESS" message)*

### Step 4: Prepare the Packaging Folder
Run these exact PowerShell commands to create a clean input folder and copy the newly built `.jar` file into it. This guarantees no corrupt or missing files:
```powershell
Remove-Item -Recurse -Force input_jpackage -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path input_jpackage
Copy-Item -Path "target\java-order-escpos-printer-1.0-SNAPSHOT.jar" -Destination "input_jpackage\"
```

### Step 5: Generate the `.exe` Application
First, make sure no old broken builds are lying around, then run the `jpackage` command:
```powershell
Remove-Item -Recurse -Force OrderPrinter -ErrorAction SilentlyContinue

jpackage --type app-image `
--name OrderPrinter `
--input input_jpackage `
--main-jar java-order-escpos-printer-1.0-SNAPSHOT.jar `
--main-class com.escpos.printer.Main
```
*(When this finishes, a new folder named `OrderPrinter` will appear in your directory).*

### Step 6: Configure the `.env` file
1. Go into the new `OrderPrinter` folder.
2. Create your `.env` file right next to `OrderPrinter.exe`.
3. **CRITICAL:** Ensure you use `API_USERNAME` instead of `USERNAME` to avoid collisions with the built-in Windows environment variable. Your file should look exactly like this:

```env
MAX_ATTEMPTS=5
RETRY_DELAY=10
LINE_WIDTH=80

BASE_URL=https://your-domain.com
AUTH_URL=/auth/
ORDERS_URL=/order/print-orders/
UPDATE_ORDER_URL=/order/print-orders-status/{id}/
CHECK_SERVER_HEALTH=/app/health-check/

PRINTER_IP=192.168.1.100
PRINTER_PORT=9100

API_USERNAME=your_actual_username
PASSWORD=your_actual_password
```

### Step 7: Run it!
Double-click `OrderPrinter.exe` inside the `OrderPrinter` folder. 

Because you followed these steps on a fresh clone, you are mathematically guaranteed to have a clean, uncorrupted Java runtime, the correct `.jar` file, and the updated `API_USERNAME` patch!
