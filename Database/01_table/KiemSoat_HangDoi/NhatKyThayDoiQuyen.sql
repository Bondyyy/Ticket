-- Nhật ký thay đổi quyền (audit trail bảo mật - tái sử dụng tư duy từ workspace gốc)
CREATE TABLE NHATKYQUYENHANTRO (
    MaNhatKy            VARCHAR2(50) PRIMARY KEY,
    HanhDong            VARCHAR2(50),   -- 'GAN_QUYEN' | 'HUY_QUYEN' | 'DOI_VAI_TRO'
    MaVaiTroCu          VARCHAR2(50),
    MaVaiTroMoi         VARCHAR2(50),
    ThoiGianThucHien    TIMESTAMP       DEFAULT SYSTIMESTAMP,
    GhiChu              VARCHAR2(500),
    MaND_ThucHien       VARCHAR2(50),
    MaND_BiThayDoi      VARCHAR2(50)
);
