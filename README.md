# Order ESC/POS Printer Program

This repository contains a high-performing Java application designed to poll an external API for food orders and automatically print them to an 80mm ESC/POS thermal printer over a local network. 

Check out the [high-level diagram of the information workflow](./docs/high_lvl_diagram_info_workflow.pdf).

## Overview

The application runs as a background service that:
1. **Authenticates** with a remote API to receive a JWT access token.
2. **Polls** periodically to fetch unprinted food orders.
3. **Prints** the orders to a networked ESC/POS thermal printer (via IP/Port), correctly formatting the receipt layout with custom alignments, bold text, and variable font sizes.
4. **Updates** the order status on the remote server to "printed".

It features a lightweight, cross-platform Swing GUI that indicates the real-time background service status and includes an alerting system for critical unrecoverable failures.

## Key Features

- **Background Polling Service**: Uses Java's `ScheduledExecutorService` for thread-safe, continuous execution.
- **Robust API Client**: Performs JWT authentication, retrieves lists of orders, updates order statuses, and checks server health.
- **Network Thermal Printing**: Connects directly to ESC/POS thermal printers over IP/Port sockets, transmitting raw byte commands for optimal layout handling (cutting paper, bolding, center alignment, etc.).
- **Cross-Platform User Interface**: A responsive `Swing` window that provides a status overview, interactive buttons (Restart, Stop Alert, Exit), and a live-updating log console via an Observer pattern.
- **Resilience & Alerting**: Handles temporary network/printer disconnections gracefully with a configurable retry limit. Emits a continuous, synthesized audio alert upon critical failures.
- **Clean Architecture**: Adheres strictly to Object-Oriented Programming (OOP) principles, featuring strict separation of Data Transfer Objects (DTOs) from Domain Models, a custom Mapper pattern, and Dependency Injection for modular, testable code.

## Project Structure

- `com.escpos.printer.model`: Internal Domain Models (`Order`, `Customer`, `Product`, etc.).
- `com.escpos.printer.dto`: Data Transfer Objects for JSON parsing.
- `com.escpos.printer.mapper`: Converts DTOs to Domain Models.
- `com.escpos.printer.client`: API and ESC/POS Printer Clients.
- `com.escpos.printer.service`: Contains the background `PollingService`.
- `com.escpos.printer.alert`: The `AlertManager` synthesizing audio warnings.
- `com.escpos.printer.ui`: Swing-based GUI.

## Prerequisites

- **Java 17+**
- **Maven** (for building and dependency management)
- A network-connected ESC/POS compatible thermal printer.

## Setup & Execution

1. **Clone the repository:**
   ```bash
   git clone <repository_url>
   cd order-escpos-printer/java-order-escpos-printer
   ```

2. **Configure Environment Variables:**
   Create a `.env` file in the project root with the following properties (map them according to `AppSettings.java` if using a specific `.env` mapping):
   ```env
   MAX_ATTEMPTS=5
   RETRY_DELAY=10
   LINE_WIDTH=80
   
   BASE_URL=https://api.yourdomain.com
   AUTH_URL=/auth/
   ORDERS_URL=/order/print-orders/
   UPDATE_ORDER_URL=/order/print-orders-status/{id}/
   CHECK_SERVER_HEALTH=/app/health-check/
   
   PRINTER_IP=192.168.1.100
   PRINTER_PORT=9100
   
   USERNAME=your_username
   PASSWORD=your_password
   ```

3. **Build the Standalone Executable (Fat JAR):**
   The project is configured with the `maven-shade-plugin` to package all dependencies into a single JAR file.
   ```bash
   mvn clean package
   ```
   This will generate a `.jar` file in the `target/` directory (e.g., `java-order-escpos-printer-1.0-SNAPSHOT.jar`).

4. **Run the Application:**
   You can run the compiled JAR directly on any OS with Java installed:
   ```bash
   java -jar target/java-order-escpos-printer-1.0-SNAPSHOT.jar
   ```

## Packaging & Deployment

For detailed instructions on packaging the application as a standalone native executable (`.app` for macOS or `.exe` for Windows) and properly configuring the `.env` file for production deployment, please refer to the [Deployment Guide](./docs/deployment_guide.md).
