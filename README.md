# Student Management App

โปรเจกต์แอปพลิเคชันจัดการข้อมูลนักศึกษาสำหรับรายวิชาการพัฒนาแอปพลิเคชันบนอุปกรณ์เคลื่อนที่ (Data Storage and Access Part 1 & Part 2)

## รายละเอียดโปรเจกต์

- **Part 1:** พัฒนาแอปพลิเคชัน Android ด้วย Jetpack Compose ตามสถาปัตยกรรม MVVM ร่วมกับ Repository และจัดเก็บข้อมูลภายในเครื่องด้วย Room Database (SQLite)
- **Part 2:** เชื่อมต่อแอปพลิเคชัน Android เข้ากับระบบ Backend REST API ที่พัฒนาด้วย FastAPI และฐานข้อมูล PostgreSQL ผ่าน Retrofit พร้อมรองรับการทำงานแบบ Online/Offline (Offline-First) โดยเมื่อเชื่อมต่อเซิร์ฟเวอร์ได้จะดึงข้อมูลจาก PostgreSQL มาบันทึกลง Room และหากไม่สามารถเชื่อมต่อเซิร์ฟเวอร์ได้จะดึงข้อมูลเดิมจาก Room ขึ้นมาแสดงผล

## โครงสร้างโปรเจกต์

- `app/` - โค้ดฝั่ง Android Application (Jetpack Compose, ViewModel, Repository, Room, Retrofit)
- `student-api/` - โค้ดฝั่ง Backend Server (FastAPI, SQLAlchemy, Pydantic, PostgreSQL)

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

เมื่อรันสำเร็จสามารถเปิดทดสอบ API ผ่าน Swagger UI ได้ที่ `http://127.0.0.1:8000/docs`

### 3. รันฝั่ง Android Application
1. เปิดโปรเจกต์ใน Android Studio
2. กด Sync Project with Gradle Files
3. เปิด Android Emulator (ระบบใช้ `http://10.0.2.2:8000/` ในการเชื่อมต่อไปยัง FastAPI บนเครื่องคอมพิวเตอร์)
4. กดปุ่ม Run เพื่อติดตั้งและรันแอปพลิเคชัน
