-- ============================================================================
-- PROCEDIMIENTO: SP_ACTUALIZAR_TODAS_CALIFICACIONES
-- Descripción: Recalcula y actualiza las calificaciones de TODOS los clientes
--              activos basándose en su historial de préstamos.
--
-- Autor: Henersson Cobo, Jorge Mera, Fabián Ome
-- Proyecto: Sistema de Préstamos y Casa de Empeño
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
--
-- Uso: EXECUTE SP_ACTUALIZAR_TODAS_CALIFICACIONES;
-- ============================================================================

CREATE OR REPLACE PROCEDURE SP_ACTUALIZAR_TODAS_CALIFICACIONES
IS
    CURSOR cur_clientes IS
        SELECT ID_PERSONA FROM CLIENTE WHERE ACTIVO = 'S';

    v_count NUMBER := 0;
    v_calificacion NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== Actualizando calificaciones de todos los clientes ===');
    DBMS_OUTPUT.PUT_LINE(' ');

    FOR cliente_rec IN cur_clientes LOOP
        -- Calcular nueva calificación
        v_calificacion := FN_CALCULAR_CALIFICACION_CLIENTE(cliente_rec.ID_PERSONA);

        -- Actualizar en la base de datos
        UPDATE CLIENTE
        SET CALIFICACION = v_calificacion
        WHERE ID_PERSONA = cliente_rec.ID_PERSONA;

        v_count := v_count + 1;

        DBMS_OUTPUT.PUT_LINE('  Cliente ' || cliente_rec.ID_PERSONA ||
                             ' → Calificación: ' || v_calificacion);
    END LOOP;

    COMMIT;

    DBMS_OUTPUT.PUT_LINE(' ');
    DBMS_OUTPUT.PUT_LINE('=== Total clientes actualizados: ' || v_count || ' ===');
    DBMS_OUTPUT.PUT_LINE('✓ Proceso completado exitosamente');

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('✗ Error durante la actualización: ' || SQLERRM);
        RAISE;
END SP_ACTUALIZAR_TODAS_CALIFICACIONES;
/

-- Agregar comentario de documentación
COMMENT ON PROCEDURE SP_ACTUALIZAR_TODAS_CALIFICACIONES IS
'Recalcula y actualiza las calificaciones de todos los clientes activos.
Debe ejecutarse después de crear la función FN_CALCULAR_CALIFICACION_CLIENTE.
Autor: Henersson Cobo, Jorge Mera, Fabián Ome
Proyecto: Casa de Empeño - Bases de Datos II';

-- Mostrar mensaje de confirmación
PROMPT ✓ Procedimiento SP_ACTUALIZAR_TODAS_CALIFICACIONES creado exitosamente

