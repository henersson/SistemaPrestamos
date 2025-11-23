-- ============================================================================
-- VISTA: V_PRESTAMOS_DETALLE
-- Descripcion: Vista con informacion completa de prestamos incluyendo
--              nombres de clientes/asesores y calculos de intereses.
--
-- Autor: Henersson Cobo, Jorge Mera, Fabian Ome
-- Proyecto: Sistema de Prestamos y Casa de Empeno
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
-- ============================================================================

CREATE OR REPLACE VIEW V_PRESTAMOS_DETALLE AS
SELECT
    p.ID_PRESTAMO,
    p.ID_CLIENTE,
    pc.NOMBRE_PERSONA AS NOMBRE_CLIENTE,
    p.ID_ASESOR,
    pa.NOMBRE_PERSONA AS NOMBRE_ASESOR,
    p.MONTO,
    p.INTERES_GENERADO,
    NVL(p.MULTA, 0) AS MULTA,
    (p.MONTO + p.INTERES_GENERADO + NVL(p.MULTA, 0)) AS TOTAL_DEUDA,
    p.FECHA_PRESTAMO,
    p.FECHA_VENCIMIENTO,
    p.ESTADO_PRESTAMO,
    p.TASA_INTERES,
    MONTHS_BETWEEN(p.FECHA_VENCIMIENTO, p.FECHA_PRESTAMO) AS MESES_PLAZO,
    CASE
        WHEN MONTHS_BETWEEN(p.FECHA_VENCIMIENTO, p.FECHA_PRESTAMO) < 3 THEN '5%'
        WHEN MONTHS_BETWEEN(p.FECHA_VENCIMIENTO, p.FECHA_PRESTAMO) <= 6 THEN '10%'
        ELSE '15%'
    END AS TASA_APLICADA,
    TRUNC(SYSDATE - p.FECHA_VENCIMIENTO) AS DIAS_VENCIDO
FROM PRESTAMO p
JOIN PERSONA pc ON p.ID_CLIENTE = pc.ID_PERSONA
JOIN PERSONA pa ON p.ID_ASESOR = pa.ID_PERSONA;

-- Agregar comentario de documentacion
COMMENT ON VIEW V_PRESTAMOS_DETALLE IS
'Vista con informacion completa de prestamos incluyendo nombres y calculos.
Incluye monto, interes, multa, total deuda, tasas aplicadas y dias de vencimiento.
Autor: Henersson Cobo, Jorge Mera, Fabian Ome
Proyecto: Casa de Empeno - Bases de Datos II';

-- Mostrar mensaje de confirmacion
PROMPT Vista V_PRESTAMOS_DETALLE creada exitosamente

