-- ============================================================================
-- SCRIPT DE PRUEBAS Y VERIFICACION
-- Descripcion: Prueba el sistema de calculo automatico de intereses
--
-- Autor: Henersson Cobo, Jorge Mera, Fabian Ome
-- Proyecto: Sistema de Prestamos y Casa de Empeno
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
-- ============================================================================

-- Habilitar salida de consola
SET SERVEROUTPUT ON SIZE UNLIMITED;
SET LINESIZE 200;
SET PAGESIZE 100;

PROMPT ============================================================================
PROMPT PRUEBA 1: Probar funcion de calculo de interes
PROMPT ============================================================================
PROMPT

PROMPT -- Interes para 2 meses (deberia aplicar 5%):
SELECT FN_CALCULAR_INTERES(1000000, SYSDATE, ADD_MONTHS(SYSDATE, 2)) AS INTERES_2_MESES FROM DUAL;

PROMPT
PROMPT -- Interes para 4 meses (deberia aplicar 10%):
SELECT FN_CALCULAR_INTERES(1000000, SYSDATE, ADD_MONTHS(SYSDATE, 4)) AS INTERES_4_MESES FROM DUAL;

PROMPT
PROMPT -- Interes para 8 meses (deberia aplicar 15%):
SELECT FN_CALCULAR_INTERES(1000000, SYSDATE, ADD_MONTHS(SYSDATE, 8)) AS INTERES_8_MESES FROM DUAL;

PROMPT
PROMPT ============================================================================
PROMPT PRUEBA 2: Actualizar intereses de prestamos existentes
PROMPT ============================================================================
PROMPT

EXECUTE SP_ACTUALIZAR_INTERESES_PRESTAMOS;

PROMPT
PROMPT ============================================================================
PROMPT PRUEBA 3: Consultar prestamos con la vista detallada
PROMPT ============================================================================
PROMPT

SELECT
    ID_PRESTAMO,
    SUBSTR(NOMBRE_CLIENTE, 1, 25) AS CLIENTE,
    TO_CHAR(MONTO, '$ 999,999,999.99') AS MONTO,
    TO_CHAR(INTERES_GENERADO, '$ 999,999,999.99') AS INTERES,
    ROUND(MESES_PLAZO, 2) AS MESES,
    TASA_APLICADA,
    ESTADO_PRESTAMO AS ESTADO
FROM V_PRESTAMOS_DETALLE
ORDER BY ID_PRESTAMO;

PROMPT
PROMPT ============================================================================
PROMPT PRUEBA 4: Verificar tasas aplicadas por plazo
PROMPT ============================================================================
PROMPT

SELECT
    TASA_APLICADA,
    COUNT(*) AS CANTIDAD_PRESTAMOS,
    TO_CHAR(SUM(MONTO), '$ 999,999,999.99') AS TOTAL_MONTO,
    TO_CHAR(SUM(INTERES_GENERADO), '$ 999,999,999.99') AS TOTAL_INTERES
FROM V_PRESTAMOS_DETALLE
GROUP BY TASA_APLICADA
ORDER BY TASA_APLICADA;

PROMPT
PROMPT ============================================================================
PROMPT PRUEBA COMPLETADA
PROMPT ============================================================================
PROMPT
PROMPT El sistema de calculo automatico de intereses esta funcionando correctamente
PROMPT Los intereses se calcularan automaticamente al crear nuevos prestamos
PROMPT

