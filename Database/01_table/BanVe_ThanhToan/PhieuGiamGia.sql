CREATE TABLE PHIEUGIAMGIA (
    MaPGG                   VARCHAR2(50) PRIMARY KEY,
    MaChuSoPGG              VARCHAR2(100),
    LoaiGiamGia             VARCHAR2(20) DEFAULT 'CO_DINH',
    GiaTriGiamGia           NUMBER(18,2),
    GiaTriApDungToiThieu    NUMBER(18,2),
    NgayBatDauApDung        TIMESTAMP,
    NgayKetThucApDung       TIMESTAMP,
    SLDaDung                NUMBER              DEFAULT 0,
    SLToiDa                 NUMBER,
    NgayTaoPGG              TIMESTAMP           DEFAULT SYSTIMESTAMP,
    MaNV                    VARCHAR2(50)
);
