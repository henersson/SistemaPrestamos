-- ============================================================================
-- TRIGGER: TRG_CALCULAR_INTERES_PRESTAMO
-- Descripcion: Calcula automaticamente el interes al crear un nuevo prestamo
--              basandose en el monto y el plazo del prestamo.
--
-- Autor: Henersson Cobo, Jorge Mera, Fabian Ome
-- Proyecto: Sistema de Prestamos y Casa de Empeno
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
--
-- Eventos: BEFORE INSERT ON PRESTAMO
-- Nivel: FOR EACH ROW
-- ============================================================================

CREATE OR REPLACE TRIGGER TRG_CALCULAR_INTERES_PRESTAMO
BEFORE INSERT ON PRESTAMO
FOR EACH ROW
BEGIN
    -- Calcular interes automaticamente
    :NEW.INTERES_GENERADO := FN_CALCULAR_INTERES(
        :NEW.MONTO,
        NVL(:NEW.FECHA_PRESTAMO, SYSDATE),
        :NEW.FECHA_VENCIMIENTO
    );

    -- Si no se especifico fecha de prestamo, usar fecha actual
    IF :NEW.FECHA_PRESTAMO IS NULL THEN
        :NEW.FECHA_PRESTAMO := SYSDATE;
    END IF;

    -- Si no se especifico estado, poner ACTIVO
    IF :NEW.ESTADO_PRESTAMO IS NULL THEN
        :NEW.ESTADO_PRESTAMO := 'ACTIVO';
    END IF;

    DBMS_OUTPUT.PUT_LINE('=======================================');
    DBMS_OUTPUT.PUT_LINE('Nuevo prestamo - Interes calculado');
    DBMS_OUTPUT.PUT_LINE('Monto: $' || TO_CHAR(:NEW.MONTO, '999,999,999.99'));
    DBMS_OUTPUT.PUT_LINE('Interes: $' || TO_CHAR(:NEW.INTERES_GENERADO, '999,999,999.99'));
    DBMS_OUTPUT.PUT_LINE('=======================================');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error en trigger calcular interes: ' || SQLERRM);
        RAISE;
END TRG_CALCULAR_INTERES_PRESTAMO;
/

-- Agregar comentario de documentacion
COMMENT ON TRIGGER TRG_CALCULAR_INTERES_PRESTAMO IS
'Calcula automaticamente el interes al crear un nuevo prestamo.
Se ejecuta antes de INSERT en la tabla PRESTAMO.
Autor: Henersson Cobo, Jorge Mera, Fabian Ome
Proyecto: Casa de Empeno - Bases de Datos II';

-- Mostrar mensaje de confirmacion
PROMPT Trigger TRG_CALCULAR_INTERES_PRESTAMO creado exitosamente

