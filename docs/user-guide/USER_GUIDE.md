# SheetCell User Guide

Welcome to SheetCell, a collaborative spreadsheet application that allows multiple users to work together on spreadsheets in real-time.

## Getting Started

### Logging In

1. **Launch the Application**
   - Start the SheetCell client application
   - You'll see the login screen

2. **Enter Your Username**
   - Type a unique username (no password required)
   - Click "Login" to access the application

### Main Dashboard

After logging in, you'll see the main dashboard with:
- **Available Sheets**: List of all sheets you have access to
- **Upload Button**: Add new sheets to the server
- **Refresh Button**: Update the sheet list
- **User Info**: Your current username and session details

## Working with Sheets

### Creating a New Sheet

1. **Upload an XML File**
   - Click the "Upload Sheet" button
   - Select an XML file from your computer
   - The sheet will be uploaded and you'll become the owner

2. **Sheet Information**
   - Each sheet shows: name, owner, dimensions, and your permission level
   - Permission levels: OWNER, WRITER, READER

### Opening a Sheet

1. **Select a Sheet**
   - Double-click on any sheet you have access to
   - The sheet will open in a new window

2. **Sheet Interface**
   - **Grid**: Main spreadsheet area
   - **Action Line**: Shows selected cell and allows editing
   - **Menu Bar**: Access to various functions
   - **Status Bar**: Current user and sheet information

## Editing Cells

### Basic Cell Operations

1. **Select a Cell**
   - Click on any cell to select it
   - Selected cell is highlighted with a border

2. **Edit Cell Content**
   - Double-click a cell or press F2 to edit
   - Type your content (text, numbers, or formulas)
   - Press Enter to confirm changes

3. **Cell Types**
   - **Text**: Any string value
   - **Numbers**: Numeric values for calculations
   - **Formulas**: Start with `{` and end with `}` (e.g., `{SUM,A1:A5}`)

### Formula System

SheetCell supports various functions:

- **Basic Math**: `{PLUS,A1,B1}`, `{MINUS,A1,B1}`, `{TIMES,A1,B1}`, `{DIVIDE,A1,B1}`
- **Range Functions**: `{SUM,A1:A5}`, `{AVERAGE,A1:A5}`
- **Cell References**: Use cell coordinates like A1, B2, etc.

### Example Formulas
```
{PLUS,A1,B1}        # Add values in A1 and B1
{SUM,A1:A10}        # Sum all values from A1 to A10
{TIMES,B2,0.08}     # Multiply B2 by 0.08 (8%)
{AVERAGE,C1:C5}     # Calculate average of C1 to C5
```

## Collaboration Features

### Permission System

1. **Owner Permissions**
   - Full control over the sheet
   - Can modify any cell
   - Can manage other users' permissions
   - Can delete the sheet

2. **Writer Permissions**
   - Can edit cell values
   - Can view all sheet data
   - Cannot manage permissions

3. **Reader Permissions**
   - Can only view sheet data
   - Cannot edit cells
   - Cannot modify the sheet

### Requesting Access

1. **Find a Sheet**
   - Browse available sheets in the dashboard
   - Sheets you don't have access to will show "No Permission"

2. **Request Permission**
   - Click on a sheet you want access to
   - Select the permission level you need (WRITER or READER)
   - Submit your request

3. **Wait for Approval**
   - The sheet owner will receive your request
   - You'll be notified when approved or rejected

### Real-time Collaboration

1. **Live Updates**
   - See changes made by other users in real-time
   - Modified cells show the username of who made the change
   - Cell colors may change to indicate recent modifications

2. **Version Control**
   - Each change creates a new version
   - You can view previous versions of the sheet
   - Version history shows who made what changes

3. **Conflict Resolution**
   - If you're working on an old version, you'll be prompted to update
   - The system prevents data loss from concurrent edits

## Advanced Features

### Range Operations

1. **Named Ranges**
   - Define ranges like "A1:B5" with custom names
   - Use named ranges in formulas
   - Manage ranges through the Range menu

2. **Sorting Data**
   - Select a range of cells
   - Choose sort criteria (ascending/descending)
   - Sort by one or multiple columns

3. **Filtering Data**
   - Apply filters to show only specific data
   - Set criteria for different columns
   - View filtered results in a popup window

### Customization

1. **Cell Styling**
   - Change background colors
   - Modify text colors
   - Adjust text alignment (left, center, right)

2. **Layout Adjustments**
   - Resize column widths by dragging borders
   - Adjust row heights
   - Customize the overall appearance

3. **Themes**
   - Switch between light and dark themes
   - Apply custom color schemes
   - Save your preferred settings

### What-If Analysis

1. **Scenario Testing**
   - Temporarily change cell values
   - See how changes affect dependent cells
   - Test different scenarios without saving changes

2. **Dependency Visualization**
   - View which cells depend on the selected cell
   - See which cells the selected cell depends on
   - Understand the calculation flow

## Tips and Best Practices

### Efficient Workflow

1. **Use Meaningful Names**
   - Give your sheets descriptive names
   - Use clear cell labels and headers

2. **Organize Your Data**
   - Keep related data together
   - Use consistent formatting
   - Add comments for complex formulas

3. **Collaborate Effectively**
   - Communicate with team members about changes
   - Use version history to track progress
   - Request appropriate permission levels

### Troubleshooting

1. **Connection Issues**
   - Check your internet connection
   - Verify the server is running
   - Try refreshing the application

2. **Formula Errors**
   - Check formula syntax (must start with `{` and end with `}`)
   - Verify cell references are correct
   - Ensure referenced cells contain valid data

3. **Permission Problems**
   - Contact the sheet owner for access
   - Verify you're logged in with the correct username
   - Check if your permission request is still pending

### Keyboard Shortcuts

- **F2**: Edit selected cell
- **Enter**: Confirm cell edit
- **Escape**: Cancel cell edit
- **Arrow Keys**: Navigate between cells
- **Ctrl+S**: Save sheet (if you have write permissions)
- **Ctrl+Z**: Undo last action
- **Ctrl+R**: Refresh sheet data

## Getting Help

If you need assistance:

1. **Check this User Guide** for common questions
2. **Review the API Documentation** for technical details
3. **Contact Support** for technical issues
4. **Check the Development Guide** if you're contributing to the project

---

**Happy Spreadsheeting!** 🎉 