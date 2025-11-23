-- ============================================================================
-- FUNCIÓN: FN_CALCULAR_CALIFICACION_CLIENTE
-- Descripción: Calcula la calificación de un cliente basada en su historial
--              de préstamos usando una fórmula ponderada.
--
-- Autor: Henersson Cobo, Jorge Mera, Fabián Ome
-- Proyecto: Sistema de Préstamos y Casa de Empeño
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
--
-- Fórmula de Calificación:
--   Base: 5.0 puntos
--   + 0.3 puntos por cada préstamo cancelado (máximo +3.0)
--   + 0.1 puntos por cada préstamo activo (máximo +1.0)
--   - 1.5 puntos por cada préstamo en mora
--   - 2.0 puntos por cada préstamo vencido
--   Rango final: 1.0 - 10.0
-- ============================================================================

CREATE OR REPLACE FUNCTION FN_CALCULAR_CALIFICACION_CLIENTE(
    p_id_cliente NUMBER
) RETURN NUMBER
IS
    v_total_prestamos NUMBER := 0;
    v_prestamos_cancelados NUMBER := 0;
    v_prestamos_activos NUMBER := 0;
    v_prestamos_mora NUMBER := 0;
    v_prestamos_vencidos NUMBER := 0;
    v_calificacion NUMBER := 5.0; -- Calificación base
BEGIN
    -- Contar total de préstamos
    SELECT COUNT(*)
    INTO v_total_prestamos
    FROM PRESTAMO
    WHERE ID_CLIENTE = p_id_cliente;

    -- Si no tiene préstamos, retornar calificación base
    IF v_total_prestamos = 0 THEN
        RETURN 5.0;
    END IF;

    -- Contar préstamos cancelados (pagados exitosamente)
    SELECT COUNT(*)
    INTO v_prestamos_cancelados
    FROM PRESTAMO
    WHERE ID_CLIENTE = p_id_cliente
    AND ESTADO_PRESTAMO = 'CANCELADO';

    -- Contar préstamos activos
    SELECT COUNT(*)
    INTO v_prestamos_activos
    FROM PRESTAMO
    WHERE ID_CLIENTE = p_id_cliente
    AND ESTADO_PRESTAMO = 'ACTIVO';

    -- Contar préstamos en mora
    SELECT COUNT(*)
    INTO v_prestamos_mora
    FROM PRESTAMO
    WHERE ID_CLIENTE = p_id_cliente
    AND ESTADO_PRESTAMO = 'EN_MORA';

    -- Contar préstamos vencidos
    SELECT COUNT(*)
    INTO v_prestamos_vencidos
    FROM PRESTAMO
    WHERE ID_CLIENTE = p_id_cliente
    AND ESTADO_PRESTAMO = 'VENCIDO';

    -- FÓRMULA DE CALIFICACIÓN:
    -- Base: 5.0 puntos
    -- +0.3 por cada préstamo cancelado (máximo +3.0)
    -- +0.1 por cada préstamo activo (máximo +1.0)
    -- -1.5 por cada préstamo en mora
    -- -2.0 por cada préstamo vencido (no pagado)

    v_calificacion := 5.0;

    -- Sumar puntos por préstamos pagados
    v_calificacion := v_calificacion + LEAST(v_prestamos_cancelados * 0.3, 3.0);

    -- Sumar puntos por préstamos activos (en buen estado)
    v_calificacion := v_calificacion + LEAST(v_prestamos_activos * 0.1, 1.0);

    -- Restar puntos por moras
    v_calificacion := v_calificacion - (v_prestamos_mora * 1.5);

    -- Restar puntos por vencidos
    v_calificacion := v_calificacion - (v_prestamos_vencidos * 2.0);

    -- Asegurar que esté en rango 1.0 - 10.0
    v_calificacion := GREATEST(1.0, LEAST(10.0, v_calificacion));

    RETURN ROUND(v_calificacion, 2);

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error en FN_CALCULAR_CALIFICACION_CLIENTE: ' || SQLERRM);
        RETURN 5.0; -- Retornar calificación base en caso de error
END FN_CALCULAR_CALIFICACION_CLIENTE;
/

-- Agregar comentario de documentación
COMMENT ON FUNCTION FN_CALCULAR_CALIFICACION_CLIENTE IS
'Calcula la calificación de un cliente basada en su historial de préstamos.
Fórmula: Base 5.0 + Cancelados*0.3 + Activos*0.1 - Mora*1.5 - Vencidos*2.0
Rango: 1.0 - 10.0
Autor: Henersson Cobo, Jorge Mera, Fabián Ome
Proyecto: Casa de Empeño - Bases de Datos II';

-- Mostrar mensaje de confirmación
PROMPT ✓ Función FN_CALCULAR_CALIFICACION_CLIENTE creada exitosamente

