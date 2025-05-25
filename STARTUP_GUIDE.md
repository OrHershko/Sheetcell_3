# 🚀 SheetCell Application - Startup Guide

## **Prerequisites**
- ☑️ Java 17 or higher
- ☑️ Maven 3.6+
- ☑️ Apache Tomcat 9/10
- ☑️ JavaFX 19+ (if not bundled)

## **Quick Start (Recommended)**

### 1. **Build Everything**
```bash
./scripts/build.sh
```
✅ This compiles all modules and creates deployable artifacts

### 2. **Start the Server**
```bash
./scripts/deploy-server.sh
```
✅ Automatically deploys to Tomcat and starts the server
✅ Server available at: `http://localhost:8080/sheetcell-server-1.0.0/`

### 3. **Launch the Client**
```bash
./scripts/run-client.sh
```
✅ Opens the JavaFX client application

## **Manual Setup (Alternative)**

### **Server Setup**
```bash
# 1. Build the project
mvn clean package

# 2. Deploy to Tomcat
cp modules/sheetcell-server/target/sheetcell-server-1.0.0.war $TOMCAT_HOME/webapps/

# 3. Start Tomcat
$TOMCAT_HOME/bin/startup.sh   # Linux/Mac
$TOMCAT_HOME/bin/startup.bat  # Windows

# 4. Verify server is running
curl http://localhost:8080/sheetcell-server-1.0.0/
```

### **Client Setup**
```bash
# Option 1: Maven JavaFX plugin
cd modules/sheetcell-client
mvn javafx:run

# Option 2: Direct JAR execution (requires JavaFX path)
java --module-path /path/to/javafx/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar modules/sheetcell-client/target/sheetcell-client-1.0.0.jar
```

## **Application Flow**

1. **Login Screen** 📱
   - Enter username
   - Server connection established

2. **Sheet Manager** 📊
   - Create new spreadsheets
   - Load existing files
   - Manage permissions

3. **Main Application** ✨
   - Real-time collaborative editing
   - Cell formulas and functions
   - Data visualization
   - Version history

## **Troubleshooting**

### **Server Issues**
```bash
# Check if Tomcat is running
ps aux | grep tomcat

# Check server logs
tail -f $TOMCAT_HOME/logs/catalina.out

# Verify port availability
netstat -tulpn | grep :8080
```

### **Client Issues**
```bash
# Check JavaFX availability
java --list-modules | grep javafx

# Run with debug output
mvn javafx:run -X

# Check for missing dependencies
mvn dependency:tree
```

### **Common Solutions**
- **Port 8080 occupied**: Change Tomcat port in `server.xml`
- **JavaFX not found**: Install OpenJFX or use bundled version
- **Build failures**: Run `mvn clean install` from root directory

## **Development Mode**

For development with auto-reload:

```bash
# Terminal 1: Start server in debug mode
export CATALINA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
$TOMCAT_HOME/bin/startup.sh

# Terminal 2: Client development mode
cd modules/sheetcell-client
mvn javafx:run -Dargs="--debug"
```

## **Configuration**

### **Server Configuration**
Edit `config/application.properties`:
```properties
# Server settings
server.port=8080
server.max.sheets=100
server.max.users=50

# File upload settings
upload.max.size=10MB
upload.allowed.types=xml,xlsx

# Logging
logging.level=INFO
logging.file=logs/sheetcell.log
```

### **Client Configuration**
The client connects to `http://localhost:8080/sheetcell-server-1.0.0/` by default.

## **Sample Data**

Load the sample spreadsheet:
```bash
# Copy sample file to accessible location
cp samples/sample-sheet.xml /tmp/

# In the application: File → Load → select sample-sheet.xml
```

## **Stopping the Application**

```bash
# Stop Tomcat
$TOMCAT_HOME/bin/shutdown.sh

# Client closes when window is closed
# Or: Ctrl+C in terminal if running via Maven
```

---

🎉 **You're ready to use SheetCell!** 

The application provides real-time collaborative spreadsheet editing with advanced features like formulas, data visualization, and version control. 