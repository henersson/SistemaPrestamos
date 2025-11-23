-- =====================================================================
-- Script: 19_agregar_columna_multa.sql
-- Descripción: Agrega la columna MULTA a la tabla PRESTAMO
-- Autor: Sistema
-- Fecha: 2025-11-23
-- =====================================================================

SET ECHO ON
SET SERVEROUTPUT ON SIZE UNLIMITED

PROMPT =====================================================================
PROMPT Verificando y agregando columna MULTA a la tabla PRESTAMO
PROMPT =====================================================================

DECLARE
    v_count NUMBER;
    v_sql VARCHAR2(500);
BEGIN
    -- Verificar si la columna MULTA ya existe
    SELECT COUNT(*)
    INTO v_count
    FROM USER_TAB_COLUMNS
    WHERE TABLE_NAME = 'PRESTAMO'
    AND COLUMN_NAME = 'MULTA';

    IF v_count = 0 THEN
        -- La columna no existe, crearla
        DBMS_OUTPUT.PUT_LINE('La columna MULTA no existe. Creando...');

        EXECUTE IMMEDIATE 'ALTER TABLE PRESTAMO ADD (MULTA NUMBER(10,2) DEFAULT 0 NOT NULL)';

        DBMS_OUTPUT.PUT_LINE('Columna MULTA creada exitosamente.');

        -- Agregar comentario a la columna
        EXECUTE IMMEDIATE 'COMMENT ON COLUMN PRESTAMO.MULTA IS ''Multa aplicada al préstamo por mora o incumplimiento''';

        -- Actualizar los préstamos existentes con multa 0
        UPDATE PRESTAMO
        SET MULTA = 0
        WHERE MULTA IS NULL;

        COMMIT;

        DBMS_OUTPUT.PUT_LINE('Préstamos existentes actualizados con MULTA = 0.');
        DBMS_OUTPUT.PUT_LINE('Proceso completado exitosamente.');
    ELSE
        DBMS_OUTPUT.PUT_LINE('La columna MULTA ya existe en la tabla PRESTAMO.');
        DBMS_OUTPUT.PUT_LINE('No se realizaron cambios.');
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR: ' || SQLERRM);
        ROLLBACK;
        RAISE;
END;
/

-- Verificar la estructura final
PROMPT
PROMPT =====================================================================
PROMPT Verificando estructura de la tabla PRESTAMO
PROMPT =====================================================================
PROMPT

SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NULLABLE, DATA_DEFAULT
FROM USER_TAB_COLUMNS
WHERE TABLE_NAME = 'PRESTAMO'
ORDER BY COLUMN_ID;

PROMPT
PROMPT =====================================================================
PROMPT Script completado
PROMPT =====================================================================
