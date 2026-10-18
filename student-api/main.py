from datetime import datetime, timedelta, timezone
import jwt
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from sqlalchemy.orm import Session

from database import Base, engine, get_db
from models import Student
from schemas import StudentCreate, StudentResponse, TokenResponse

SECRET_KEY = "student-management-secret-key"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60

USERS_DB = {
    "admin": "1234",
    "student": "1234"
}

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="Student Management API",
    version="1.0"
)


def create_access_token(data: dict):
    to_encode = data.copy()
    expire = datetime.now(timezone.utc) + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)


def get_current_user(token: str = Depends(oauth2_scheme)):
    masked_token = f"{token[:12]}...***" if len(token) > 12 else "***"
    print(f"[Auth Header] Authorization: Bearer {masked_token}")
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = payload.get("sub")
        if username is None or username not in USERS_DB:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid token",
                headers={"WWW-Authenticate": "Bearer"},
            )
        return username
    except jwt.PyJWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Could not validate credentials",
            headers={"WWW-Authenticate": "Bearer"},
        )


@app.get("/")
def root():
    return {
        "message": "Student Management API"
    }


@app.post(
    "/token",
    response_model=TokenResponse
)
def login(
    form_data: OAuth2PasswordRequestForm = Depends()
):
    expected_password = USERS_DB.get(form_data.username)
    if expected_password is None or expected_password != form_data.password:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )

    access_token = create_access_token(data={"sub": form_data.username})
    return {
        "access_token": access_token,
        "token_type": "bearer"
    }


@app.get(
    "/students",
    response_model=list[StudentResponse]
)
def get_students(
    db: Session = Depends(get_db),
    current_user: str = Depends(get_current_user)
):
    return db.query(Student).order_by(Student.id.asc()).all()


@app.get(
    "/students/{student_id}",
    response_model=StudentResponse
)
def get_student(
    student_id: int,
    db: Session = Depends(get_db),
    current_user: str = Depends(get_current_user)
):
    student = db.query(Student).filter(
        Student.id == student_id
    ).first()
    if student is None:
        raise HTTPException(
            status_code=404,
            detail="Student not found"
        )
    return student


@app.post(
    "/students",
    response_model=StudentResponse
)
def create_student(
    student: StudentCreate,
    db: Session = Depends(get_db),
    current_user: str = Depends(get_current_user)
):
    existing = db.query(Student).filter(
        Student.student_code == student.student_code
    ).first()
    if existing is not None:
        existing.name = student.name
        existing.major = student.major
        db.commit()
        db.refresh(existing)
        return existing

    new_student = Student(
        student_code=student.student_code,
        name=student.name,
        major=student.major
    )

    db.add(new_student)
    db.commit()
    db.refresh(new_student)

    return new_student


@app.put(
    "/students/{student_id}",
    response_model=StudentResponse
)
def update_student(
    student_id: int,
    student: StudentCreate,
    db: Session = Depends(get_db),
    current_user: str = Depends(get_current_user)
):
    existing = db.query(Student).filter(
        Student.id == student_id
    ).first()

    if existing is None:
        raise HTTPException(
            status_code=404,
            detail="Student not found"
        )

    existing.student_code = student.student_code
    existing.name = student.name
    existing.major = student.major

    db.commit()
    db.refresh(existing)

    return existing


@app.delete(
    "/students/{student_id}"
)
def delete_student(
    student_id: int,
    db: Session = Depends(get_db),
    current_user: str = Depends(get_current_user)
):
    student = db.query(Student).filter(
        Student.id == student_id
    ).first()

    if student is None:
        raise HTTPException(
            status_code=404,
            detail="Student not found"
        )

    db.delete(student)
    db.commit()

    return {
        "message": "Student deleted"
    }
