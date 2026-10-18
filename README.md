# Student Management App

โปรเจกต์แอปพลิเคชันจัดการข้อมูลนักศึกษาสำหรับรายวิชาการพัฒนาแอปพลิเคชันบนอุปกรณ์เคลื่อนที่ (Lab 1, Lab 2, Lab 3, และ Lab 4)

## รายละเอียดโปรเจกต์

- **Lab 1:** พัฒนาแอปพลิเคชัน Android ด้วย Jetpack Compose ตามสถาปัตยกรรม MVVM ร่วมกับ Repository และจัดเก็บข้อมูลภายในเครื่องด้วย Room Database (SQLite)
- **Lab 2:** เชื่อมต่อแอปพลิเคชัน Android เข้ากับระบบ Backend REST API ที่พัฒนาด้วย FastAPI และฐานข้อมูล PostgreSQL ผ่าน Retrofit
- **Lab 3:** เพิ่มระบบยืนยันตัวตน (Authentication) ด้วย JWT (JSON Web Token) โดยมีหน้าเข้าสู่ระบบ (`LoginScreen`), การจัดเก็บ Access Token ด้วย `TokenStorage`, และการแนบ Header `Authorization: Bearer <token>` อัตโนมัติผ่าน `AuthInterceptor`
- **Lab 4:** ปรับปรุงสถาปัตยกรรมสู่ระบบ Offline-First และ Synchronization เต็มรูปแบบ โดยหน้าจออ่านข้อมูลจาก Room ผ่าน `Flow` (`StateFlow`) เป็นหลัก เมื่อเพิ่มนักศึกษาใหม่จะบันทึกลง Room ด้วยสถานะ `PENDING` ทันทีแม้ไม่มีอินเทอร์เน็ต และเมื่อกดปุ่ม `Sync Now` ระบบจะส่งรายการที่ค้างอยู่ (`PENDING`) ไปยัง FastAPI เพื่อบันทึกลง PostgreSQL แล้วอัปเดต `serverId` และเปลี่ยนสถานะเป็น `SYNCED`

## โครงสร้างโปรเจกต์

- `app/` - โค้ดฝั่ง Android Application (Jetpack Compose, ViewModel, Repository, Room, Retrofit, OkHttp Interceptor, TokenStorage)
- `student-api/` - โค้ดฝั่ง Backend Server (FastAPI, OAuth2 + JWT Authentication, SQLAlchemy, Pydantic, PostgreSQL)

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

### 3. รันฝั่ง Android Application และทดสอบ Offline-First (Lab 4)
1. เปิดโปรเจกต์ใน Android Studio แล้วกด Run
2. เข้าสู่ระบบด้วย Username: `admin` และ Password: `1234`
3. ทดสอบปิดอินเทอร์เน็ตหรือหยุด FastAPI Server แล้วกดปุ่ม `Add Student` เพื่อเพิ่มนักศึกษาใหม่
4. รายชื่อนักศึกษาใหม่จะแสดงบนหน้าจอทันทีโดยมีสถานะ `Sync: PENDING`
5. เปิดอินเทอร์เน็ตหรือรัน FastAPI Server ขึ้นมาใหม่ แล้วกดปุ่ม `Sync Now`
6. ข้อมูลจะถูกส่งเข้า PostgreSQL และสถานะบนหน้าจอจะเปลี่ยนเป็น `Sync: SYNCED`
