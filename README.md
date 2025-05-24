# SheetCell Application (שטיסל)

This project is a client-server spreadsheet application built in Java. It provides comprehensive spreadsheet functionalities, allowing users to create, edit, manage, and share data in a tabular format. The application utilizes JavaFX for its graphical user interface and is designed with a modular architecture to support concurrent user interactions and version control.

## Overall Architecture

The SheetCell application follows a client-server model:

*   **Server-Side:** A Tomcat server hosts the `Engine` module, managing sheet data, user authentication, permissions, and concurrent access.
*   **Client-Side:** The `Client` application, built upon `JavaFX-UI` components, interacts with the server to provide users with a rich interface for spreadsheet operations.
*   **Data Transfer:** `DTO` (Data Transfer Objects) are used for structured communication between the client, server, and engine.

### Modules

The project is divided into the following key modules:

*   **`Engine`**:
    *   The core logic unit of the application.
    *   Manages all spreadsheet operations: cell creation, value updates (direct and formula-based), dependency tracking (מח"מ - מערכת חישובים מבוזרת), and recalculations.
    *   Handles version management for entire sheets and individual cells.
    *   A passive module that responds to requests from the `Server` or `JavaFX-UI` (in standalone mode).

*   **`DTO` (Data Transfer Objects)**:
    *   A collection of simple objects used to pass data between different layers and modules (e.g., `Client` to `Server`, `Server` to `Engine`).
    *   Ensures standardized data structures for communication.

*   **`JavaFX-UI`**:
    *   Provides a library of reusable JavaFX components for the spreadsheet interface (e.g., grid display, action line, menus).
    *   Contains the functionality for a standalone desktop spreadsheet application (as developed in תרגיל 2), capable of working with local XML files.

*   **`Client`**:
    *   The main client-side application that users interact with.
    *   Builds upon the components and capabilities of the `JavaFX-UI` module.
    *   Connects to the `Server` to:
        *   Handle user login (unique usernames).
        *   Display a sheet management dashboard (view all server sheets, upload new sheets, see permissions).
        *   Open and interact with individual sheets based on user permissions (OWNER, WRITER, READER).

*   **`Server`**:
    *   Hosts the `Engine` to perform spreadsheet operations.
    *   Manages multiple sheets from various users.
    *   Handles user authentication and session management.
    *   Enforces the permissions system (OWNER, READER, WRITER) for accessing and modifying sheets.
    *   Manages concurrent access to sheets, ensuring data integrity and notifying users of updates.
    *   Designed to be deployed on an Apache Tomcat web server.

## Features

### Core Spreadsheet Functionality
*   **XML Sheet Loading:** Load spreadsheet data from XML files. Initially for local files in standalone mode, then server-managed for client-server mode.
*   **Data Display:** View sheet data, including original formulas/values and their effective (calculated) values. Visualize cell dependencies.
*   **Cell Updates & Recalculation:** Modify cell values, triggering automatic recalculation of dependent cells (מח"מ).
*   **Version Control:**
    *   Track versions for entire sheets.
    *   Track versions for individual cell changes, including who made the change.
    *   View read-only previous versions of a sheet.

### JavaFX UI (Standalone and Client Base)
*   **Graphical Spreadsheet:** Interactive grid for displaying and editing cells.
*   **Customization:**
    *   Adjustable column widths and row heights.
    *   Cell content alignment (left, center, right).
    *   Cell styling: background color and text color.
*   **Range Management:**
    *   Define named ranges of cells (e.g., "A1:B5" as "MyData").
    *   Delete existing ranges.
    *   View ranges highlighted on the grid.
*   **Complex Functions:** Implement functions that can operate on cell ranges (e.g., SUM, AVG).
*   **Data Operations:**
    *   Sort data within a selected range by one or more columns.
    *   Filter data within a range based on criteria for specific columns.
    *   Sorted/filtered views can be displayed in a temporary popup window.
*   **"What-If" Scenarios:** Dynamically input values into cells (especially formula cells) to see potential outcomes without permanently altering the sheet.
*   **File Handling:** Load XML sheet files using a FileChooser dialog, with progress indication for loading.
*   **Theming:** Switch between UI skins (e.g., dark mode, light mode, default theme).

### Client-Server Enhancements
*   **User Authentication:** Secure login using a unique username.
*   **Sheet Management Dashboard:**
    *   View a list of all sheets available on the server.
    *   Upload new sheets in XML format (compatible with תרגיל 2 format) to the server. The server stores and manages these sheets.
    *   Display key information for each sheet: owner's username, sheet name, dimensions (rows x cols), and the logged-in user's permission level for that sheet.
*   **Permissions System:**
    *   **OWNER:** Full control, can delete the sheet, manage permissions.
    *   **WRITER:** Can edit cell values.
    *   **READER:** Can only view the sheet data.
    *   Users can request permissions for sheets they don't own. Owners can approve or reject these requests.
*   **Concurrent Editing & Collaboration:**
    *   Multiple users with WRITER permission can edit the same sheet simultaneously, provided they are on the latest version of the sheet.
    *   Users editing an older version will be prompted to update.
    *   Read-only users (READERs or those on older versions) receive notifications when a newer version of the sheet is available.
    *   Updated cells display the username of the user who last modified them.

## Setup and Build Instructions

### Prerequisites
*   **JDK (Java Development Kit):** A recent JDK version that includes JavaFX is recommended (e.g., OpenJDK 17 or later). If using a JDK without bundled JavaFX, you'll need to add the JavaFX SDK libraries to your project manually.
*   **Apache Tomcat:** For the server-side deployment. Version 10.1.x is suitable (as referenced in `Server.iml`).
*   **IDE:** The project is primarily designed and configured for IntelliJ IDEA.

### Importing into IntelliJ IDEA
1.  Clone the repository.
2.  Open IntelliJ IDEA and choose "Open..." selecting the cloned project directory.
3.  Ensure IntelliJ recognizes the project structure and modules (`Client`, `DTO`, `Engine`, `JavaFX-UI`, `Server`).
4.  Verify module dependencies are correctly set up as defined in the `.iml` files and `.idea/modules.xml`. IntelliJ usually handles this automatically upon import.

### Server Setup (Tomcat)
1.  **Install Tomcat:** Download and install Apache Tomcat (e.g., version 10.1.26).
2.  **Deploy `Server` Module:**
    *   Build the `Server` module into a WAR (Web Application Archive) file. IntelliJ can be configured to produce this artifact.
    *   Deploy the generated WAR file to Tomcat's `webapps` directory.
    *   Alternatively, you can configure Tomcat to point to the exploded webapp output directory of the `Server` module (typically `out/artifacts/Server_Web_exploded` or similar in IntelliJ).
3.  **Start Tomcat:** Run Tomcat's startup script (`startup.sh` or `startup.bat`).

### Running the Application

1.  **Start the Server:** Ensure Tomcat is running with the `Server` module successfully deployed.
2.  **Run the Client Application:**
    *   The main entry point for the client is `main.SheetcellClientMain.java` located in the `Client` module (`Client/src/main/SheetcellClientMain.java`).
    *   Run this `main` method from IntelliJ IDEA. The client application will start and should attempt to connect to the server (defaulting to `localhost` and the standard Tomcat port).

3.  **Optional: Standalone Mode (תרגיל 2 functionality)**
    *   To run the application in a standalone mode that works with local XML files and does not require the server:
    *   The entry point is `main.Main.java` located in the `JavaFX-UI` module (`JavaFX-UI/src/main/Main.java`).
    *   Run this `main` method.

## Code Structure

The project is organized into distinct modules to promote separation of concerns and reusability:

*   **`Client/`**: Contains the main client application code, including UI interactions, communication with the server, and management of the user session. It utilizes components from `JavaFX-UI`.
*   **`DTO/`**: Holds Data Transfer Objects, which are plain Java objects used to carry data between modules (e.g., `CellDTO`, `SheetDTO`).
*   **`Engine/`**: The core logic for spreadsheet operations, independent of UI or server concerns. Handles calculations, data storage, and versioning.
*   **`JavaFX-UI/`**: A library of JavaFX components and the main class for running the spreadsheet application in a standalone, local file-based mode.
*   **`Server/`**: The web application module that hosts the `Engine`. It handles HTTP requests, user management, sheet persistence, and concurrent access control. Designed for deployment on Tomcat.
*   **`.idea/`**: IntelliJ IDEA project configuration files.
*   **`lib/`**: Contains external libraries used by the project (though specific library management might be handled by IntelliJ based on `.iml` files).

## Implemented Bonuses
(Please list any implemented bonuses here for the grader)

*   [Bonus 1: e.g., Advanced formulas]
*   [Bonus 2: e.g., Charting capabilities]

## Submission Details
*   **Name(s):** [Enter Name(s) Here]
*   **ID(s):** [Enter ID(s) Here]
*   **Email(s):** [Enter Email(s) Here]
