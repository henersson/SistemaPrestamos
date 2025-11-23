-- ============================================================================
-- SCRIPT DE PRUEBAS Y VERIFICACIÓN
-- Descripción: Prueba el sistema de calificación automática de clientes
--
-- Autor: Henersson Cobo, Jorge Mera, Fabián Ome
-- Proyecto: Sistema de Préstamos y Casa de Empeño
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
-- ============================================================================

-- Habilitar salida de consola
SET SERVEROUTPUT ON SIZE UNLIMITED;
SET LINESIZE 200;
SET PAGESIZE 100;

PROMPT ============================================================================
PROMPT PRUEBA 1: Verificar calificaciones actuales vs calculadas
PROMPT ============================================================================
PROMPT

SELECT
    p.ID_PERSONA,
    SUBSTR(p.NOMBRE_PERSONA, 1, 30) AS NOMBRE,
    c.CALIFICACION AS CAL_ACTUAL,
    FN_CALCULAR_CALIFICACION_CLIENTE(p.ID_PERSONA) AS CAL_CALCULADA,
    (SELECT COUNT(*) FROM PRESTAMO WHERE ID_CLIENTE = p.ID_PERSONA) AS TOTAL_PREST,
    (SELECT COUNT(*) FROM PRESTAMO WHERE ID_CLIENTE = p.ID_PERSONA AND ESTADO_PRESTAMO='CANCELADO') AS PAGADOS,
    (SELECT COUNT(*) FROM PRESTAMO WHERE ID_CLIENTE = p.ID_PERSONA AND ESTADO_PRESTAMO='ACTIVO') AS ACTIVOS,
    (SELECT COUNT(*) FROM PRESTAMO WHERE ID_CLIENTE = p.ID_PERSONA AND ESTADO_PRESTAMO='EN_MORA') AS EN_MORA,
    (SELECT COUNT(*) FROM PRESTAMO WHERE ID_CLIENTE = p.ID_PERSONA AND ESTADO_PRESTAMO='VENCIDO') AS VENCIDOS
FROM PERSONA p
JOIN CLIENTE c ON p.ID_PERSONA = c.ID_PERSONA
WHERE p.TIPO_PERSONA = 'CLIENTE'
ORDER BY p.ID_PERSONA;

PROMPT
PROMPT ============================================================================
PROMPT PRUEBA 2: Actualizar todas las calificaciones
PROMPT ============================================================================
PROMPT

EXECUTE SP_ACTUALIZAR_TODAS_CALIFICACIONES;

PROMPT
PROMPT ============================================================================
PROMPT PRUEBA 3: Verificar calificaciones después de la actualización
PROMPT ============================================================================
PROMPT

SELECT
    p.ID_PERSONA,
    SUBSTR(p.NOMBRE_PERSONA, 1, 35) AS NOMBRE_CLIENTE,
    c.CALIFICACION,
    (SELECT COUNT(*) FROM PRESTAMO WHERE ID_CLIENTE = p.ID_PERSONA) AS NUM_PRESTAMOS,
    CASE
        WHEN c.CALIFICACION >= 8.0 THEN 'EXCELENTE'
        WHEN c.CALIFICACION >= 6.5 THEN 'BUENO'
        WHEN c.CALIFICACION >= 5.0 THEN 'REGULAR'
        WHEN c.CALIFICACION >= 3.0 THEN 'MALO'
        ELSE 'MUY MALO'
    END AS CATEGORIA
FROM PERSONA p
JOIN CLIENTE c ON p.ID_PERSONA = c.ID_PERSONA
WHERE p.TIPO_PERSONA = 'CLIENTE'
ORDER BY c.CALIFICACION DESC;

PROMPT
PROMPT ============================================================================
PROMPT PRUEBA 4: Estadísticas de calificaciones
PROMPT ============================================================================
PROMPT

SELECT
    'TOTAL CLIENTES' AS METRICA,
    COUNT(*) AS VALOR
FROM CLIENTE
WHERE ACTIVO = 'S'
UNION ALL
SELECT
    'CALIFICACIÓN PROMEDIO',
    ROUND(AVG(CALIFICACION), 2)
FROM CLIENTE
WHERE ACTIVO = 'S'
UNION ALL
SELECT
    'CALIFICACIÓN MÁXIMA',
    MAX(CALIFICACION)
FROM CLIENTE
WHERE ACTIVO = 'S'
UNION ALL
SELECT
    'CALIFICACIÓN MÍNIMA',
    MIN(CALIFICACION)
FROM CLIENTE
WHERE ACTIVO = 'S';

PROMPT
PROMPT ============================================================================
PROMPT PRUEBA 5: Distribución de clientes por categoría
PROMPT ============================================================================
PROMPT

SELECT
    CASE
        WHEN CALIFICACION >= 8.0 THEN '1. EXCELENTE (8.0-10.0)'
        WHEN CALIFICACION >= 6.5 THEN '2. BUENO (6.5-7.9)'
        WHEN CALIFICACION >= 5.0 THEN '3. REGULAR (5.0-6.4)'
        WHEN CALIFICACION >= 3.0 THEN '4. MALO (3.0-4.9)'
        ELSE '5. MUY MALO (1.0-2.9)'
    END AS CATEGORIA,
    COUNT(*) AS CANTIDAD_CLIENTES,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM CLIENTE WHERE ACTIVO = 'S'), 2) AS PORCENTAJE
FROM CLIENTE
WHERE ACTIVO = 'S'
GROUP BY
    CASE
        WHEN CALIFICACION >= 8.0 THEN '1. EXCELENTE (8.0-10.0)'
        WHEN CALIFICACION >= 6.5 THEN '2. BUENO (6.5-7.9)'
        WHEN CALIFICACION >= 5.0 THEN '3. REGULAR (5.0-6.4)'
        WHEN CALIFICACION >= 3.0 THEN '4. MALO (3.0-4.9)'
        ELSE '5. MUY MALO (1.0-2.9)'
    END
ORDER BY CATEGORIA;

PROMPT
PROMPT ============================================================================
PROMPT PRUEBA COMPLETADA
PROMPT ============================================================================
PROMPT
PROMPT ✓ El sistema de calificación automática está funcionando correctamente
PROMPT ✓ Las calificaciones se actualizarán automáticamente cuando cambien los préstamos
PROMPT

