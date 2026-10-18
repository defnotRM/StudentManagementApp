# Student Management App

โปรเจกต์แอปพลิเคชันจัดการข้อมูลนักศึกษาสำหรับรายวิชาการพัฒนาแอปพลิเคชันบนอุปกรณ์เคลื่อนที่ (ระบบรวม Lab 1 ถึง Lab 7)

## รายละเอียดโปรเจกต์

- **Lab 1:** พัฒนาแอปพลิเคชัน Android ด้วย Jetpack Compose ตามสถาปัตยกรรม MVVM ร่วมกับ Repository และจัดเก็บข้อมูลภายในเครื่องด้วย Room Database (SQLite)
- **Lab 2:** เชื่อมต่อแอปพลิเคชัน Android เข้ากับระบบ Backend REST API ที่พัฒนาด้วย FastAPI และฐานข้อมูล PostgreSQL ผ่าน Retrofit
- **Lab 3:** เพิ่มระบบยืนยันตัวตน (Authentication) ด้วย JWT (JSON Web Token) โดยมีหน้าเข้าสู่ระบบ (`LoginScreen`), การจัดเก็บ Access Token ด้วย `TokenStorage`, และการแนบ Header `Authorization: Bearer <token>` อัตโนมัติผ่าน `AuthInterceptor`
- **Lab 4:** ปรับปรุงสถาปัตยกรรมสู่ระบบ Offline-First และ Synchronization เต็มรูปแบบ โดยหน้าจออ่านข้อมูลจาก Room ผ่าน `Flow` (`StateFlow`) เป็นหลัก เมื่อเพิ่มนักศึกษาใหม่จะบันทึกลง Room ด้วยสถานะ `PENDING` ทันทีแม้ไม่มีอินเทอร์เน็ต และเมื่อกดปุ่ม `Sync Now` ระบบจะส่งรายการที่ค้างอยู่ (`PENDING`) ไปยัง FastAPI เพื่อบันทึกลง PostgreSQL แล้วอัปเดต `serverId` และเปลี่ยนสถานะเป็น `SYNCED`
- **Lab 5:** เพิ่มระบบ Background Synchronization ด้วย Android WorkManager โดยสร้าง `SyncWorker` (`CoroutineWorker`) สำหรับซิงก์ข้อมูลเบื้องหลัง และสร้าง `SyncScheduler` เพื่อตั้งงานซิงก์แบบครั้งเดียว (`OneTimeWorkRequest` ผ่านปุ่ม `Sync in Background`) และแบบทำซ้ำอัตโนมัติทุก 15 นาที (`PeriodicWorkRequest`) เมื่อมีสัญญาณเครือข่าย (`NetworkType.CONNECTED`)
- **Lab 6:** ย้ายการสร้างและจัดการ Dependency ทั้งหมดจาก Manual DI (`AppContainer`) ไปใช้ Dagger Hilt:
  - เพิ่ม `@HiltAndroidApp` ที่ `StudentApplication` พร้อมตั้งค่า `HiltWorkerFactory` สำหรับ WorkManager
  - เพิ่ม `@AndroidEntryPoint` ที่ `MainActivity`
  - สร้าง `DatabaseModule` ใน `di/DatabaseModule.kt` สำหรับ Provide `AppDatabase` และ `StudentDao`
  - สร้าง `NetworkModule` ใน `di/NetworkModule.kt` สำหรับ Provide `TokenStorage`, `OkHttpClient`, `Retrofit`, `StudentApi`, และ `AuthApi`
  - เพิ่ม `@HiltViewModel` และ `@Inject constructor` ใน `StudentViewModel` และ `LoginViewModel` และเชื่อมต่อกับ Jetpack Compose ด้วย `hiltViewModel()`
- **Lab 7:** ปรับปรุงสู่สถาปัตยกรรม Clean Architecture และระบบ Unit Testing:
  - **Presentation Layer:** `StudentScreen`, `StudentViewModel`, `LoginScreen`, `LoginViewModel` จัดการ UI State และรับ Event จากผู้ใช้
  - **Domain Layer:** ประกอบด้วย Domain Model (`domain.model.Student`), Repository Interface (`domain.repository.StudentRepository`), และ Use Cases (`GetStudentsUseCase`, `SyncStudentsUseCase`, `AddStudentUseCase`, `DeleteStudentUseCase`) ที่ไม่ขึ้นกับ Android Framework, Room หรือ Retrofit โดยตรง
  - **Data Layer:** `StudentRepositoryImpl`, `StudentDao`, `StudentApi` ทำหน้าที่ดึงและแปลงข้อมูล (Mapping) ระหว่าง Local Database (Room) กับ Remote REST API
  - **Dependency Injection:** ใช้ `RepositoryModule` (`@Binds`) เพื่อผูก `StudentRepositoryImpl` เข้ากับ `StudentRepository` Interface
  - **Unit Testing:** เขียน Unit Test สำหรับ `GetStudentsUseCase` ด้วย JUnit 4, Fake Repository และ `kotlinx-coroutines-test` (`runTest`)

## โครงสร้างโปรเจกต์ (Clean Architecture)

- `app/src/main/java/com/example/studentmanagementapp/`
  - `domain/`
    - `model/Student.kt` - Domain Model กลางของ Business Logic
    - `repository/StudentRepository.kt` - Repository Interface
    - `usecase/` - Business Logic Use Cases (`GetStudentsUseCase`, `SyncStudentsUseCase`, `AddStudentUseCase`, `DeleteStudentUseCase`)
  - `data/`
    - `local/` - Room Database (`AppDatabase`, `StudentDao`, `StudentEntity`)
    - `remote/` - Retrofit Client, APIs (`AuthApi`, `StudentApi`), Token Management
    - `repository/StudentRepositoryImpl.kt` - Data Layer Implementation
  - `di/` - Dagger Hilt Modules (`DatabaseModule`, `NetworkModule`, `RepositoryModule`)
  - `ui/` - Jetpack Compose UI และ ViewModels (`StudentScreen`, `StudentViewModel`, `LoginScreen`, `LoginViewModel`)
  - `worker/` - WorkManager Background Sync (`SyncWorker`, `SyncScheduler`)
- `student-api/` - Backend Server (FastAPI, OAuth2 + JWT Authentication, SQLAlchemy, Pydantic, PostgreSQL)

## วิธีการรันระบบ

### 1. ตั้งค่าฐานข้อมูล PostgreSQL
สร้างฐานข้อมูลชื่อ `student_db` และตาราง `students` ใน PostgreSQL:

```sql
CREATE DATABASE student_db;

CREATE TABLE students (
    id SERIAL PRIMARY KEY,
    student_code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    major VARCHAR(100) NOT NULL
);

INSERT INTO students (student_code, name, major) VALUES
('65001', 'Somchai Jaidee', 'Computer Science'),
('65002', 'Somsri Dee', 'Information Technology'),
('65003', 'Anan Kong', 'Data Science');
```

### 2. รันฝั่ง Backend (FastAPI)
เปิด Terminal ในโฟลเดอร์ `student-api` แล้วรันคำสั่ง:

```bash
cd student-api
python -m venv venv
venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --reload
```

### 3. รันฝั่ง Android Application
1. เปิดโปรเจกต์ใน Android Studio แล้วกด Run
2. เข้าสู่ระบบด้วย Username: `admin` และ Password: `1234`
3. แอปจะทำงานตามสถาปัตยกรรม Clean Architecture ร่วมกับ Dagger Hilt และ Room Offline-First โดยอัตโนมัติ

### 4. รันการทดสอบ Unit Test
รันคำสั่งใน Terminal ของโปรเจกต์:

```bash
./gradlew testDebugUnitTest
```
