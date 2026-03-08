╔══════════════════════════════════════════════════════════════════╗
║   KMM – Fire Equipment Inspection System           ║
║   Version 1.0  |  Emergency Services Department                 ║
╚══════════════════════════════════════════════════════════════════╝

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
REQUIREMENTS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
• Java 11 or later   → https://adoptium.net/
• Apache Maven 3.8+  → https://maven.apache.org/download.cgi
• Internet connection (first build only — downloads dependencies)
• Webcam (optional — for live QR scanning; manual entry fallback available)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
HOW TO BUILD & RUN
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Windows:
  Double-click run.bat  (or: run.bat in Command Prompt)

Linux / macOS:
  chmod +x run.sh && ./run.sh

Manual (any OS):
  mvn clean package
  java -jar target/KMM-Inspection.jar

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
APPLICATION TABS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🔥 FIRE EXTINGUISHERS (KMM-HSE-FEC-001)
   • 12-item monthly checklist
   • QR scan for Equipment No / Location tag (auto-fills fields)
   • QR scan for Condition tag (auto-fills Yes/No checklist items)
   • Condition codes: ALL_OK | HOSE_DAMAGED | PIN_MISSING | LOW_PRESSURE
   • "Generate & Print QR Equipment Tag" — creates printable tag with:
       - Branded equipment tag (PNG + print)
       - Large QR for location scan
       - Small QR for monthly condition check

💧 FIRE DELUGE SYSTEM
   • 9-item checklist (pressure, valves, lockout, signage)
   • QR scan for location and condition
   • Auto-fills pressure gauge result if QR contains PRESSURE:value

🚿 FIRE HOSE REEL
   • 9-item checklist (drum, hose, nozzle, seal, valve, signage)
   • QR scan buttons on Equipment No and Location fields

🩺 FIRST-AID BOX
   • 8-item checklist (seal, expiry, monthly check, first aider details)
   • QR scan for box tag — parses SEAL:INTACT and EXPIRY:CURRENT codes

📋 RECORDS / EXPORT
   • View all saved records in a colour-coded table
   • Issue count highlighted in red per record
   • Export to CSV (all records, all fields)
   • Print text summary
   • Delete individual records

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
QR CODE FORMAT (for printing equipment tags)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Location QR:
  KMM-EQ|No:KM-SE094|Area:Admin Building|Type:9kg DCP Fire Extinguisher|Site:Parsons|Exp:Sept-26

Condition QR (scan when inspecting):
  KMM-COND|No:KM-SE094|COND:ALL_OK
  KMM-COND|No:KM-SE094|COND:HOSE_DAMAGED
  KMM-COND|No:KM-SE094|COND:PIN_MISSING
  KMM-COND|No:KM-SE094|COND:LOW_PRESSURE

Deluge pressure QR:
  KMM-EQ|No:DS-001|PRESSURE:8.5|Area:Parsons

First-Aid Box QR:
  KMM-EQ|No:FA-012|SEAL:INTACT|EXPIRY:CURRENT|Area:Bruce

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FILE STRUCTURE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
kmm-inspection/
├── pom.xml                          Maven build file
├── run.bat                          Windows launch script
├── run.sh                           Linux/macOS launch script
├── README.txt                       This file
└── src/main/java/kmm/
    ├── KMMApp.java                  Entry point
    ├── MainFrame.java               Main window + tabs
    ├── Theme.java                   Design constants
    ├── UI.java                      Reusable UI helpers
    ├── QRScannerDialog.java         Live webcam QR scanner
    ├── QRCodeGenerator.java         QR tag generator + printer
    ├── ExtinguisherPanel.java       Fire Extinguishers tab
    ├── DelugePanel.java             Fire Deluge System tab
    ├── OtherPanels.java             Hose Reel + First-Aid tabs
    └── RecordsPanel.java            Records / Export tab

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
DEPENDENCIES (auto-downloaded by Maven)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
• Google ZXing 3.5.2   — QR code read/write
• Sarxos Webcam 0.3.12 — Live camera feed for scanning

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
NOTE: Records are in-memory only. Use "Export CSV" to persist data.
      POPI Act: data is collected and used only for inspection purposes.
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
