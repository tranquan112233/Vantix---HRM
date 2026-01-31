CREATE DATABASE Vantix CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE Vantix;

-- =======================================================
-- 1. BẢNG ROLES (VAI TRÒ)
-- =======================================================
CREATE TABLE Roles (
    RoleID INT AUTO_INCREMENT PRIMARY KEY,       -- Mã vai trò
    RoleName VARCHAR(100) NOT NULL UNIQUE,       -- Tên vai trò (Admin, HR, Staff)
    Description VARCHAR(255)                     -- Mô tả
);

-- =======================================================
-- 2. BẢNG DEPARTMENTS (PHÒNG BAN)
-- =======================================================
CREATE TABLE Departments (
    DepartmentID INT AUTO_INCREMENT PRIMARY KEY, -- Mã phòng ban
    DepartmentName VARCHAR(100) NOT NULL UNIQUE, -- Tên phòng ban
    Description VARCHAR(255)                     -- Mô tả
);

-- =======================================================
-- 3. BẢNG USERS (NHÂN VIÊN & TÀI KHOẢN)
-- =======================================================
CREATE TABLE Users (
    UserID INT AUTO_INCREMENT PRIMARY KEY,       -- Khóa chính
    
    -- THÔNG TIN ĐĂNG NHẬP (BẮT BUỘC)
    Username VARCHAR(100) NOT NULL UNIQUE,       -- Tên đăng nhập
    Password VARCHAR(200) NOT NULL,              -- Mật khẩu
    
    -- THÔNG TIN CÁ NHÂN
    EmployeeCode VARCHAR(50) NOT NULL UNIQUE,    -- Mã nhân viên
    Fullname VARCHAR(100) NOT NULL,              -- Họ và tên
    Gender ENUM('Male', 'Female', 'Other')DEFAULT 'Other',-- Giới tính
    Address VARCHAR(255),                        -- Địa chỉ
    Email VARCHAR(100),                          -- Email
    Phone VARCHAR(20),                           -- Số điện thoại
    AvatarURL VARCHAR(255),                      -- Ảnh đại diện
    
    -- THÔNG TIN CÔNG VIỆC
    Status ENUM('Working','Resigned','OnLeave') DEFAULT 'Working', -- Trạng thái
    
    RoleID INT,                                  -- Vai trò
    DepartmentID INT,                            -- Phòng ban
    
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP, -- Ngày tạo
    
    FOREIGN KEY (RoleID) REFERENCES Roles(RoleID),
    FOREIGN KEY (DepartmentID) REFERENCES Departments(DepartmentID)
);

-- =======================================================
-- 4. BẢNG SHIFTS (CA LÀM VIỆC)
-- =======================================================
CREATE TABLE Shifts (
    ShiftID INT AUTO_INCREMENT PRIMARY KEY,      -- Mã ca
    ShiftName VARCHAR(50) NOT NULL,              -- Tên ca
    StartTime TIME NOT NULL,                     -- Giờ bắt đầu
    EndTime TIME NOT NULL                        -- Giờ kết thúc
);

-- =======================================================
-- 5. BẢNG ATTENDANCE (CHẤM CÔNG)
-- =======================================================
CREATE TABLE Attendance (
    AttendanceID INT AUTO_INCREMENT PRIMARY KEY, -- Mã chấm công
    UserID INT NOT NULL,                         -- Nhân viên
    ShiftID INT NOT NULL,                        -- Ca làm việc
    WorkDate DATE NOT NULL,                      -- Ngày làm
    
    CheckIn TIME,                                -- Giờ vào
    CheckOut TIME,                               -- Giờ ra
    WorkHours DECIMAL(5,2),                      -- Tổng giờ làm
    LateMinutes INT DEFAULT 0,                   -- Phút trễ
    EarlyLeaveMinutes INT DEFAULT 0,             -- Phút về sớm
    Status ENUM('Draft', 'Pending', 'Approved', 'Rejected'),-- Trạng thái phiếu
    
    FOREIGN KEY (UserID) REFERENCES Users(UserID),
    FOREIGN KEY (ShiftID) REFERENCES Shifts(ShiftID)
);

-- =======================================================
-- 6. BẢNG LEAVE_REQUESTS (ĐƠN XIN NGHỈ)
-- =======================================================
CREATE TABLE LeaveRequests (
    LeaveID INT AUTO_INCREMENT PRIMARY KEY,      -- Mã đơn
    UserID INT NOT NULL,                         -- Người gửi
    LeaveType VARCHAR(50),                       -- Loại nghỉ
    
    FromDate DATE NOT NULL,                      -- Từ ngày
    ToDate DATE NOT NULL,                        -- Đến ngày
    TotalDays DECIMAL(4,1) NOT NULL,             -- Tổng số ngày
    Reason VARCHAR(255),                         -- Lý do
    
    Status ENUM('Pending','Approved','Rejected') DEFAULT 'Pending', -- Trạng thái
    ApprovedBy INT,                              -- Người duyệt
    RejectionReason VARCHAR(255),                -- Lý do từ chối
    
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP, -- Ngày tạo
    
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (ApprovedBy) REFERENCES Users(UserID) ON DELETE SET NULL
);

-- =======================================================
-- 7. BẢNG CONTRACTS (HỢP ĐỒNG)
-- =======================================================
CREATE TABLE Contracts (
    ContractID INT AUTO_INCREMENT PRIMARY KEY,   -- Mã hợp đồng
    UserID INT NOT NULL,                         -- Nhân viên
    ContractCode VARCHAR(50),                    -- Số hợp đồng
    ContractType VARCHAR(50),                    -- Loại hợp đồng
    
    Positions VARCHAR(100),               		 -- Chức vụ lúc ký
    Salary DECIMAL(18,2),                        -- Lương cứng
    StartDate DATE,                              -- Ngày hiệu lực
    EndDate DATE,                                -- Ngày hết hạn
    
    Status VARCHAR(20) DEFAULT 'ACTIVE',         -- Trạng thái
    
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
    
);

-- =======================================================
-- 8. BẢNG CONTRACT_ANNEXES (PHỤ LỤC HỢP ĐỒNG)
-- =======================================================
CREATE TABLE ContractAnnexes (
    AnnexID INT AUTO_INCREMENT PRIMARY KEY,      -- Mã phụ lục
    ContractID INT NOT NULL,                     -- Hợp đồng gốc
    AnnexCode VARCHAR(50),                       -- Số phụ lục
    
    EffectiveDate DATE NOT NULL,                 -- Ngày hiệu lực
    NewSalary DECIMAL(18,2) NULL,                -- Lương mới
    NewPositions VARCHAR(100) NULL,               -- Chức vụ mới
    
    Content TEXT,                                -- Nội dung
    IsActive BOOLEAN,               			 -- Kích hoạt
    
    FOREIGN KEY (ContractID) REFERENCES Contracts(ContractID)
);

-- =======================================================
-- 9. BẢNG PAYROLL (BẢNG LƯƠNG)
-- =======================================================
CREATE TABLE Payroll (
    PayrollID INT AUTO_INCREMENT PRIMARY KEY,    -- Mã lương
    UserID INT NOT NULL,                         -- Nhân viên
    SalaryMonth CHAR(7),                         -- Tháng lương (YYYY-MM)
    
    BaseSalary DECIMAL(18,2),                    -- Lương cơ bản
    ActualWorkDays DECIMAL(4,1),                 -- Ngày công thực
    Allowance DECIMAL(18,2),                     -- Phụ cấp
    OvertimePay DECIMAL(18,2),                   -- Tăng ca
    Bonus DECIMAL(18,2),                         -- Thưởng
    
    BhxhAmount DECIMAL(18,2),					 -- Bảo hiểm xã hội
	BhytAmount DECIMAL(18,2),					 -- Bảo hiểm y tế
	BhtnAmount DECIMAL(18,2),					 -- Bảo hiểm thất nghiệp
	TaxAmount DECIMAL(18,2), 					 -- Thuế TNCN
	OtherDeduction DECIMAL(18,2),				 -- Tổng các khoản thuế bảo hiểm
    
	TotalIncome DECIMAL(18,2),  				 -- Tổng lương tháng
    TotalDeduction DECIMAL(18,2),                -- Tổng khấu trừ (do đi trẽ & nghỉ không phép)
    NetSalary DECIMAL(18,2),                     -- Thực lĩnh
    
    PaidDate DATE,                               -- Ngày trả
    
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

-- =======================================================
-- 10. BẢNG NOTIFICATIONS (THÔNG BÁO)
-- =======================================================
CREATE TABLE Notifications (
    NotificationID INT AUTO_INCREMENT PRIMARY KEY, -- Mã thông báo
    Title VARCHAR(150),                          -- Tiêu đề
    Content TEXT,                                -- Nội dung
    
    TargetRoleID INT NULL,                       -- Gửi cho vai trò
    TargetUserID INT NULL,                       -- Gửi cho cá nhân
    
    AttachmentURL VARCHAR(255),                  -- File đính kèm
    SenderID INT,                                -- Người gửi
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP, -- Ngày tạo
    
    FOREIGN KEY (TargetUserID) REFERENCES Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (SenderID) REFERENCES Users(UserID),
    FOREIGN KEY (TargetRoleID) REFERENCES Roles(RoleID)
);