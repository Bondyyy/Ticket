CREATE TABLE NHANVIEN (
    MaNV                VARCHAR2(50) PRIMARY KEY,
    LoaiNV              VARCHAR2(50),   -- 'Ban tổ chức' | 'Nhân viên soát vé' | 'Quản lý'
    NgayVaoLam          DATE,
    TrangThaiLamViec    VARCHAR2(50),
    LuongCoBan          NUMBER(18,2),
    PhuCap              NUMBER(18,2),
    MaNQL               VARCHAR2(50),
    MaND                VARCHAR2(50),   UNIQUE(MaND)
);
