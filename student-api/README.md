# Student Management API (FastAPI + PostgreSQL)

Backend REST API สำหรับระบบจัดการข้อมูลนักศึกษา (Lab Part 2)

## 1. การตั้งค่าฐานข้อมูล PostgreSQL (Part A)

สร้างฐานข้อมูลชื่อ `student_db` และสร้างตาราง `students`:

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

## 2. การติดตั้งและรัน FastAPI (Part B)

```bash
cd student-api
python -m venv venv
venv\Scripts\activate
pip install -r requirements.txt
```

แก้ไขรหัสผ่าน PostgreSQL ในไฟล์ `database.py` ให้ตรงกับเครื่องของคุณ:
```python
DATABASE_URL = "postgresql+psycopg2://postgres:password@localhost:5432/student_db"
```

รันเซิร์ฟเวอร์:
```bash
uvicorn main:app --reload
```

เปิด Swagger UI เพื่อทดสอบ API:
- http://127.0.0.1:8000/docs

## 3. รายการ REST API Endpoints

| HTTP Method | Endpoint | หน้าที่ |
| :--- | :--- | :--- |
| `GET` | `/students` | อ่านข้อมูลนักศึกษาทั้งหมด |
| `GET` | `/students/{id}` | อ่านข้อมูลนักศึกษารายคน |
| `POST` | `/students` | เพิ่มข้อมูลนักศึกษาใหม่ |
| `PUT` | `/students/{id}` | แก้ไขข้อมูลนักศึกษา |
| `DELETE` | `/students/{id}` | ลบข้อมูลนักศึกษา |
