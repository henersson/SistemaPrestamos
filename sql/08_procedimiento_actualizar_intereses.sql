-- ============================================================================
-- PROCEDIMIENTO: SP_ACTUALIZAR_INTERESES_PRESTAMOS
-- Descripcion: Recalcula y actualiza los intereses de todos los prestamos
--              activos basandose en su monto y plazo actual.
--
-- Autor: Henersson Cobo
-- Proyecto: Sistema de Prestamos y Casa de Empeno
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
--
-- Uso: EXECUTE SP_ACTUALIZAR_INTERESES_PRESTAMOS;
-- ============================================================================

CREATE OR REPLACE PROCEDURE SP_ACTUALIZAR_INTERESES_PRESTAMOS
IS
    CURSOR cur_prestamos IS
        SELECT ID_PRESTAMO, MONTO, FECHA_PRESTAMO, FECHA_VENCIMIENTO
        FROM PRESTAMO
        WHERE ESTADO_PRESTAMO IN ('ACTIVO', 'EN_MORA');

    v_count NUMBER := 0;
    v_nuevo_interes NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('===================================================');
    DBMS_OUTPUT.PUT_LINE('  Actualizando intereses de prestamos activos');
    DBMS_OUTPUT.PUT_LINE('===================================================');
    DBMS_OUTPUT.PUT_LINE(' ');

    FOR prestamo_rec IN cur_prestamos LOOP
        v_nuevo_interes := FN_CALCULAR_INTERES(
            prestamo_rec.MONTO,
            prestamo_rec.FECHA_PRESTAMO,
            prestamo_rec.FECHA_VENCIMIENTO
        );

        UPDATE PRESTAMO
        SET INTERES_GENERADO = v_nuevo_interes
        WHERE ID_PRESTAMO = prestamo_rec.ID_PRESTAMO;

        v_count := v_count + 1;

        DBMS_OUTPUT.PUT_LINE('Prestamo ' || prestamo_rec.ID_PRESTAMO ||
                             ' -> Interes: $' || TO_CHAR(v_nuevo_interes, '999,999,999.99'));
    END LOOP;

    COMMIT;

    DBMS_OUTPUT.PUT_LINE(' ');
    DBMS_OUTPUT.PUT_LINE('===================================================');
    DBMS_OUTPUT.PUT_LINE('Total prestamos actualizados: ' || v_count);
    DBMS_OUTPUT.PUT_LINE('===================================================');

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
        RAISE;
END SP_ACTUALIZAR_INTERESES_PRESTAMOS;
/

-- Agregar comentario de documentacion
COMMENT ON PROCEDURE SP_ACTUALIZAR_INTERESES_PRESTAMOS IS
'Recalcula y actualiza los intereses de todos los prestamos activos.
Debe ejecutarse despues de crear la funcion FN_CALCULAR_INTERES.
Autor: Henersson Cobo
Proyecto: Casa de Empeno - Bases de Datos II';

-- Mostrar mensaje de confirmacion
PROMPT Procedimiento SP_ACTUALIZAR_INTERESES_PRESTAMOS creado exitosamente
