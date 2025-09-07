# Rental App Group-15 | Readme

## 1) Προαπαιτούμενα (local)

- Git  
- JDK 21 (OpenJDK ή Oracle JDK)  
- Maven 3.9+ (εναλλακτικά το mvnw του project)  
- Docker & Docker Compose plugin  
- Node.js 18+ και npm 9+  

---

## 2.1) Γρήγορη εκκίνηση (προτείνεται)

**A) Κλωνοποίηση (terminal):**
```bash
git clone https://github.com/it2022031/DS-2025.git
# ή
git clone git@github.com:it2022031/DS-2025.git
```

**B) Μετάβαση στο σωστό Directory (terminal):**
```bash
cd DS-2025
```

**C) Εκτέλεση Ολοκληρωμένου Script (terminal):**
```bash
SKIP_TESTS=0 ./scripts/dev_split.sh
```

**D) Load Test Data για την βάση (optional):**
```bash
./scripts/load_all.sh
```

**E) Επίσκεψη Εφαρμογής (browser):**  
[http://localhost:8081](http://localhost:8081)

---

## 2.2) Αν κάτι δεν δουλέψει (manual εναλλακτικές)

**A) Database (Docker):**
```bash
cd backend/demo/src/main/java/com/example/demo
docker compose up -d
# ή
docker-compose up -d   # για άλλη version
```

**B) Backend χειροκίνητα (Spring Boot):**
```bash
cd backend/demo
mvn spring-boot:run       # Με global Maven
./mvnw spring-boot:run    # ή με το wrapper του project
```

**C) Frontend χειροκίνητα (Vue):**
```bash
cd frontend/vue-argon-design-system-master
npm run dev
```

Εναλλακτικά:
```bash
cd frontend/vue-argon-design-system-master
npm install
npm run serve
```

**D) Test Data για την βάση (optional):**
```bash
cd DS-2025
./scripts/load_all.sh
```

**E) Επίσκεψη Εφαρμογής (browser):**  
[http://localhost:8081](http://localhost:8081)

---

## 3) Σημαντικές σημειώσεις

### SQL Data
Σε περίπτωση που θέλετε να δείτε/αλλάξετε τα ενδεικτικά δεδομένα, υπάρχουν δύο SQL αρχεία:  
`DS-2025/backend/demo/src/main/resources/db/migration/`

- `test_dedomena.sql` ← test data (users/properties/rentals/roles κ.λπ.)  
- `load_photos.sql` ← φόρτωση φωτογραφιών (avatars & properties)  

### Login
- Το password για όλους τους ήδη υπάρχοντες χρήστες είναι: **pass123**  
- Μπορείτε να συνδεθείτε αρχικά με **username: admin** για να δείτε και τα username όλων των χρηστών.  

### Ports
- Database: **5432**  
- Backend: **8080**  
- Frontend: **8081**  
