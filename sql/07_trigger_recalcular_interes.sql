-- ============================================================================
-- TRIGGER: TRG_RECALCULAR_INTERES
-- Descripcion: Recalcula el interes si se modifica la fecha de vencimiento
--              de un prestamo existente.
--
-- Autor: Henersson Cobo
-- Proyecto: Sistema de Prestamos y Casa de Empeno
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
--
-- Eventos: BEFORE UPDATE OF FECHA_VENCIMIENTO ON PRESTAMO
-- Nivel: FOR EACH ROW
-- ============================================================================

CREATE OR REPLACE TRIGGER TRG_RECALCULAR_INTERES
BEFORE UPDATE OF FECHA_VENCIMIENTO ON PRESTAMO
FOR EACH ROW
BEGIN
    -- Solo recalcular si cambio la fecha de vencimiento
    IF :OLD.FECHA_VENCIMIENTO != :NEW.FECHA_VENCIMIENTO THEN
        :NEW.INTERES_GENERADO := FN_CALCULAR_INTERES(
            :NEW.MONTO,
            :NEW.FECHA_PRESTAMO,
            :NEW.FECHA_VENCIMIENTO
        );

        DBMS_OUTPUT.PUT_LINE('Interes recalculado por cambio de fecha');
        DBMS_OUTPUT.PUT_LINE('Nuevo interes: $' || TO_CHAR(:NEW.INTERES_GENERADO, '999,999,999.99'));
    END IF;
END TRG_RECALCULAR_INTERES;
/

-- Agregar comentario de documentacion
COMMENT ON TRIGGER TRG_RECALCULAR_INTERES IS
'Recalcula el interes si se modifica la fecha de vencimiento.
Se ejecuta antes de UPDATE en la tabla PRESTAMO.
Autor: Henersson Cobo
Proyecto: Casa de Empeno - Bases de Datos II';

-- Mostrar mensaje de confirmacion
PROMPT Trigger TRG_RECALCULAR_INTERES creado exitosamente
