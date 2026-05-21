CREATE OR REPLACE FUNCTION fn_MaHoaDuLieuNhayCam (
    p_GiaTri    IN VARCHAR2,
    p_LoaiDL    IN VARCHAR2  -- 'SDT' | 'EMAIL'
) RETURN VARCHAR2 IS
BEGIN
    IF p_LoaiDL = 'SDT' THEN
        -- Hiển thị 3 số đầu và 2 số cuối: 090****67
        RETURN SUBSTR(p_GiaTri, 1, 3) || '****' || SUBSTR(p_GiaTri, -2);
    ELSIF p_LoaiDL = 'EMAIL' THEN
        -- Hiển thị phần trước @ và domain: ng***@gmail.com
        RETURN SUBSTR(p_GiaTri, 1, 2) || '***' ||
               SUBSTR(p_GiaTri, INSTR(p_GiaTri, '@'));
    END IF;
    RETURN '***';
END fn_MaHoaDuLieuNhayCam;
/
