# 👨‍👩‍👧‍👦 Family Contact Manager
A desktop application built with **Java 17+**, **Swing**, and **MySQL**.

---

## 📁 Project Structure

```
FamilyContactManager/
├── setup.sql                          ← Run this in MySQL first
├── lib/
│   └── mysql-connector-j-8.x.jar     ← Download and place here
└── src/main/java/com/family/
    ├── App.java                       ← Entry point
    ├── model/
    │   ├── Contact.java
    │   └── ContactGroup.java
    ├── dao/
    │   ├── ContactDAO.java
    │   └── GroupDAO.java
    ├── ui/
    │   ├── MainWindow.java
    │   └── ContactFormDialog.java
    └── util/
        ├── DBConnection.java
        └── DBInitializer.java
```

---

## ⚙️ Setup Steps

### 1. Prerequisites
- Java 17 or higher
- MySQL 8.x running on `localhost:3306`
- MySQL Connector/J driver (JDBC)

### 2. Download MySQL JDBC Driver
Download from: https://dev.mysql.com/downloads/connector/j/
Place the `.jar` in the `lib/` folder.

### 3. Configure Database Credentials
Edit `src/main/java/com/family/util/DBConnection.java`:
```java
private static final String USER     = "root";
private static final String PASSWORD = "your_actual_password";
```

### 4. Create the Database
```bash
mysql -u root -p < setup.sql
```
Or paste the contents of `setup.sql` into MySQL Workbench and run.

### 5. Compile & Run

**Using javac (manual):**
```bash
# Compile
javac -cp "lib/mysql-connector-j-8.x.jar" \
      -d out \
      $(find src -name "*.java")

# Run
java -cp "out:lib/mysql-connector-j-8.x.jar" com.family.App
```

**Using IntelliJ IDEA:**
1. Open the project folder
2. Add the MySQL `.jar` to Project Structure → Libraries
3. Set `com.family.App` as the Run Configuration main class
4. Click ▶ Run

**Using Eclipse:**
1. Import as Java Project
2. Right-click project → Build Path → Add External JARs → select the MySQL `.jar`
3. Right-click `App.java` → Run As → Java Application

---

## ✨ Features

| Feature | Description |
|---|---|
| **Add / Edit / Delete** | Full CRUD for contacts |
| **Search** | Real-time search by name, phone, email, relationship |
| **Groups** | Assign contacts to groups; filter sidebar by group |
| **Birthday Reminders** | See who has a birthday in the next 30 days |
| **Photo Support** | Attach a local image to each contact |
| **Detail Panel** | Click any contact to see full info on the right |
| **Double-click to Edit** | Quick-edit by double-clicking a row |

---

## 🎨 UI Overview

```
┌──────────────┬───────────────────────────────┬──────────────────┐
│   Sidebar    │         Contact Table          │  Detail Panel    │
│              │  ┌─────────────────────────┐  │                  │
│ 🏠 All       │  │ Search bar  [Add] [Edit] │  │  👤             │
│ 👥 Family    │  ├─────────────────────────┤  │  Park Junho      │
│ 👥 Friends   │  │ Name  | Relation | Phone│  │  Son             │
│ 👥 Emergency │  │ ...   | ...      | ...  │  │  📞 010-3456-... │
│              │  └─────────────────────────┘  │  📧 junho@...    │
│ 🎂 Birthdays │                               │  🎂 Nov 05, 1998 │
└──────────────┴───────────────────────────────┴──────────────────┘
```

---

## 🔧 Customization

- **Change DB host/port**: Edit `DBConnection.java` URL string
- **Add new fields**: Extend `Contact.java`, update `ContactDAO.java` SQL, and add fields to `ContactFormDialog.java`
- **Change theme colors**: All colors are constants at the top of `MainWindow.java`
