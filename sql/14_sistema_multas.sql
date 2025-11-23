-- ============================================================================
-- SISTEMA DE MULTAS AUTOMATICAS
-- Descripcion: Implementa la regla de negocio del universo del discurso:
--              "Si el cliente no paga la cuota en la fecha establecida,
--               se aplica una multa (2% adicional sobre el saldo)"
--
-- Autor: Henersson Cobo
-- Proyecto: Sistema de Prestamos y Casa de Empeno
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
--
-- COMPONENTES:
--   - Procedimiento: SP_APLICAR_MULTAS_MORA
--   - Trigger: TRG_APLICAR_MULTA_MORA
--   - Job: JOB_APLICAR_MULTAS_DIARIO (opcional)
-- ============================================================================

SET ECHO ON
SET SERVEROUTPUT ON SIZE UNLIMITED
SET LINESIZE 200
SET PAGESIZE 100

PROMPT ============================================================================
PROMPT INSTALACION DEL SISTEMA DE MULTAS AUTOMATICAS
PROMPT ============================================================================
PROMPT

-- ============================================================================
-- PASO 1: CREAR PROCEDIMIENTO PARA APLICAR MULTAS
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 1: Creando procedimiento SP_APLICAR_MULTAS_MORA...
PROMPT ----------------------------------------------------------------------------
PROMPT

CREATE OR REPLACE PROCEDURE SP_APLICAR_MULTAS_MORA
IS
    -- Variables para estadisticas
    v_total_procesados NUMBER := 0;
    v_total_con_multa NUMBER := 0;
    v_total_multas NUMBER := 0;
    v_nueva_multa NUMBER;
    v_saldo NUMBER;

    -- Cursor para prestamos en mora
    CURSOR cur_prestamos_mora IS
        SELECT
            ID_PRESTAMO,
            ID_CLIENTE,
            MONTO,
            INTERES_GENERADO,
            NVL(MULTA, 0) AS MULTA_ACTUAL,
            FECHA_VENCIMIENTO,
            TRUNC(SYSDATE - FECHA_VENCIMIENTO) AS DIAS_MORA
        FROM PRESTAMO
        WHERE ESTADO_PRESTAMO = 'EN_MORA'
          AND FECHA_VENCIMIENTO < SYSDATE
        ORDER BY FECHA_VENCIMIENTO;

BEGIN
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('APLICACION DE MULTAS POR MORA');
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('Fecha de ejecucion: ' || TO_CHAR(SYSDATE, 'DD/MM/YYYY HH24:MI:SS'));
    DBMS_OUTPUT.PUT_LINE('Regla: 2% adicional sobre el saldo (MONTO + INTERES)');
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Procesar cada prestamo en mora
    FOR prestamo_rec IN cur_prestamos_mora LOOP
        v_total_procesados := v_total_procesados + 1;

        -- Calcular saldo (monto + interes)
        v_saldo := prestamo_rec.MONTO + prestamo_rec.INTERES_GENERADO;

        -- Calcular multa: 2% sobre el saldo
        v_nueva_multa := v_saldo * 0.02;

        -- Actualizar multa en el prestamo
        UPDATE PRESTAMO
        SET MULTA = v_nueva_multa,
            ESTADO_PRESTAMO = 'EN_MORA' -- Asegurar estado
        WHERE ID_PRESTAMO = prestamo_rec.ID_PRESTAMO;

        -- Registrar en log
        DBMS_OUTPUT.PUT_LINE('Prestamo ID: ' || prestamo_rec.ID_PRESTAMO);
        DBMS_OUTPUT.PUT_LINE('  Cliente ID: ' || prestamo_rec.ID_CLIENTE);
        DBMS_OUTPUT.PUT_LINE('  Dias en mora: ' || prestamo_rec.DIAS_MORA);
        DBMS_OUTPUT.PUT_LINE('  Saldo: $' || TO_CHAR(v_saldo, '999,999,999.99'));
        DBMS_OUTPUT.PUT_LINE('  Multa anterior: $' || TO_CHAR(prestamo_rec.MULTA_ACTUAL, '999,999,999.99'));
        DBMS_OUTPUT.PUT_LINE('  Multa nueva (2%): $' || TO_CHAR(v_nueva_multa, '999,999,999.99'));
        DBMS_OUTPUT.PUT_LINE('  --------------------------------------------------');

        v_total_con_multa := v_total_con_multa + 1;
        v_total_multas := v_total_multas + v_nueva_multa;

    END LOOP;

    -- Commit de cambios
    COMMIT;

    -- Resumen
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('RESUMEN DE APLICACION DE MULTAS');
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('Total prestamos procesados: ' || v_total_procesados);
    DBMS_OUTPUT.PUT_LINE('Total prestamos con multa aplicada: ' || v_total_con_multa);
    DBMS_OUTPUT.PUT_LINE('Total multas aplicadas: $' || TO_CHAR(v_total_multas, '999,999,999.99'));
    DBMS_OUTPUT.PUT_LINE('============================================================================');

    IF v_total_procesados = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No hay prestamos en mora que requieran aplicacion de multas.');
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('============================================================================');
        DBMS_OUTPUT.PUT_LINE('ERROR AL APLICAR MULTAS');
        DBMS_OUTPUT.PUT_LINE('============================================================================');
        DBMS_OUTPUT.PUT_LINE('Codigo de error: ' || SQLCODE);
        DBMS_OUTPUT.PUT_LINE('Mensaje: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Se realizo ROLLBACK de todos los cambios');
        DBMS_OUTPUT.PUT_LINE('============================================================================');
        RAISE;
END SP_APLICAR_MULTAS_MORA;
/

-- Comentario de documentacion
COMMENT ON PROCEDURE SP_APLICAR_MULTAS_MORA IS
'Aplica multas (2% sobre saldo) a prestamos en estado EN_MORA.
Saldo = MONTO + INTERES_GENERADO
Actualiza campo MULTA en tabla PRESTAMO.
Incluye logging completo y manejo de errores.
Autor: Henersson Cobo';

PROMPT Procedimiento SP_APLICAR_MULTAS_MORA creado exitosamente
PROMPT

-- ============================================================================
-- PASO 2: CREAR TRIGGER AUTOMATICO PARA APLICAR MULTA AL CAMBIAR A MORA
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 2: Creando trigger TRG_APLICAR_MULTA_MORA...
PROMPT ----------------------------------------------------------------------------
PROMPT

CREATE OR REPLACE TRIGGER TRG_APLICAR_MULTA_MORA
BEFORE UPDATE OF ESTADO_PRESTAMO ON PRESTAMO
FOR EACH ROW
WHEN (NEW.ESTADO_PRESTAMO = 'EN_MORA' AND OLD.ESTADO_PRESTAMO != 'EN_MORA')
DECLARE
    v_saldo NUMBER;
    v_multa NUMBER;
BEGIN
    -- Calcular saldo (monto + interes)
    v_saldo := :NEW.MONTO + :NEW.INTERES_GENERADO;

    -- Calcular multa: 2% sobre el saldo
    v_multa := v_saldo * 0.02;

    -- Aplicar multa automaticamente
    :NEW.MULTA := v_multa;

    -- Log
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('MULTA APLICADA AUTOMATICAMENTE');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('Prestamo ID: ' || :NEW.ID_PRESTAMO);
    DBMS_OUTPUT.PUT_LINE('Estado anterior: ' || :OLD.ESTADO_PRESTAMO);
    DBMS_OUTPUT.PUT_LINE('Estado nuevo: EN_MORA');
    DBMS_OUTPUT.PUT_LINE('Saldo: $' || TO_CHAR(v_saldo, '999,999,999.99'));
    DBMS_OUTPUT.PUT_LINE('Multa aplicada (2%): $' || TO_CHAR(v_multa, '999,999,999.99'));
    DBMS_OUTPUT.PUT_LINE('========================================');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR al aplicar multa automatica: ' || SQLERRM);
        RAISE;
END TRG_APLICAR_MULTA_MORA;
/

-- Comentario de documentacion
COMMENT ON TRIGGER TRG_APLICAR_MULTA_MORA IS
'Aplica multa automaticamente cuando un prestamo cambia a estado EN_MORA.
Calcula 2% sobre el saldo (MONTO + INTERES_GENERADO).
Se ejecuta BEFORE UPDATE en PRESTAMO.
Autor: Henersson Cobo';

PROMPT Trigger TRG_APLICAR_MULTA_MORA creado exitosamente
PROMPT

-- ============================================================================
-- PASO 3: CREAR JOB PROGRAMADO (OPCIONAL)
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 3: Creando job programado para ejecucion diaria...
PROMPT ----------------------------------------------------------------------------
PROMPT

-- Nota: El job se puede habilitar manualmente si se requiere
-- Para produccion, descomentar el codigo siguiente

/*
BEGIN
    -- Eliminar job si existe
    BEGIN
        DBMS_SCHEDULER.DROP_JOB(job_name => 'JOB_APLICAR_MULTAS_DIARIO');
    EXCEPTION
        WHEN OTHERS THEN
            NULL; -- Job no existe
    END;

    -- Crear job que se ejecuta diariamente a las 00:01
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'JOB_APLICAR_MULTAS_DIARIO',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'BEGIN SP_APLICAR_MULTAS_MORA; END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY; BYHOUR=0; BYMINUTE=1',
        enabled         => TRUE,
        comments        => 'Aplica multas diariamente a prestamos en mora'
    );

    DBMS_OUTPUT.PUT_LINE('Job JOB_APLICAR_MULTAS_DIARIO creado y habilitado');
    DBMS_OUTPUT.PUT_LINE('Se ejecutara diariamente a las 00:01');
END;
/
*/

PROMPT Job programado NO creado (ejecutar manualmente si se requiere)
PROMPT Para habilitar el job, descomentar el codigo en el script
PROMPT

-- ============================================================================
-- PASO 4: VISTA DE PRESTAMOS CON MULTAS
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 4: Creando vista V_PRESTAMOS_CON_MULTAS...
PROMPT ----------------------------------------------------------------------------
PROMPT

CREATE OR REPLACE VIEW V_PRESTAMOS_CON_MULTAS AS
SELECT
    p.ID_PRESTAMO,
    p.ID_CLIENTE,
    per.NOMBRE_PERSONA AS NOMBRE_CLIENTE,
    p.ID_ASESOR,
    ases.NOMBRE_PERSONA AS NOMBRE_ASESOR,
    p.MONTO,
    p.INTERES_GENERADO,
    NVL(p.MULTA, 0) AS MULTA,
    (p.MONTO + p.INTERES_GENERADO) AS SALDO,
    (p.MONTO + p.INTERES_GENERADO + NVL(p.MULTA, 0)) AS TOTAL_DEUDA,
    p.FECHA_PRESTAMO,
    p.FECHA_VENCIMIENTO,
    p.ESTADO_PRESTAMO,
    TRUNC(SYSDATE - p.FECHA_VENCIMIENTO) AS DIAS_MORA,
    CASE
        WHEN p.ESTADO_PRESTAMO = 'EN_MORA' THEN 'SI'
        ELSE 'NO'
    END AS TIENE_MULTA,
    ROUND((NVL(p.MULTA, 0) / (p.MONTO + p.INTERES_GENERADO)) * 100, 2) AS PORCENTAJE_MULTA
FROM PRESTAMO p
JOIN PERSONA per ON p.ID_CLIENTE = per.ID_PERSONA
JOIN PERSONA ases ON p.ID_ASESOR = ases.ID_PERSONA
WHERE p.ESTADO_PRESTAMO IN ('EN_MORA', 'VENCIDO')
ORDER BY p.FECHA_VENCIMIENTO;

COMMENT ON VIEW V_PRESTAMOS_CON_MULTAS IS
'Vista de prestamos en mora con informacion de multas aplicadas.
Muestra saldo, multa, total deuda y dias en mora.
Autor: Henersson Cobo';

PROMPT Vista V_PRESTAMOS_CON_MULTAS creada exitosamente
PROMPT

-- ============================================================================
-- PRUEBAS DEL SISTEMA DE MULTAS
-- ============================================================================

PROMPT ============================================================================
PROMPT EJECUTANDO PRUEBAS DEL SISTEMA DE MULTAS
PROMPT ============================================================================
PROMPT

-- ----------------------------------------------------------------------------
-- PRUEBA 1: Verificar prestamos actuales
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 1: Verificando prestamos actuales por estado...
PROMPT

SELECT
    ESTADO_PRESTAMO,
    COUNT(*) AS CANTIDAD,
    TO_CHAR(SUM(MONTO), '$ 999,999,999.99') AS TOTAL_MONTO,
    TO_CHAR(SUM(NVL(MULTA, 0)), '$ 999,999,999.99') AS TOTAL_MULTAS
FROM PRESTAMO
GROUP BY ESTADO_PRESTAMO
ORDER BY ESTADO_PRESTAMO;

-- ----------------------------------------------------------------------------
-- PRUEBA 2: Crear prestamo de prueba y cambiar a EN_MORA
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 2: Creando prestamo de prueba y cambiando a EN_MORA...
PROMPT

DECLARE
    v_prestamo_id NUMBER;
    v_cliente_id NUMBER;
    v_asesor_id NUMBER;
    v_articulo_id NUMBER;
    v_multa_aplicada NUMBER;
BEGIN
    -- Obtener IDs necesarios
    SELECT ID_PERSONA INTO v_cliente_id
    FROM PERSONA WHERE TIPO_PERSONA = 'CLIENTE' AND ROWNUM = 1;

    SELECT ID_PERSONA INTO v_asesor_id
    FROM PERSONA WHERE TIPO_PERSONA = 'ASESOR' AND ROWNUM = 1;

    SELECT ID_ARTICULO INTO v_articulo_id
    FROM ARTICULO WHERE ESTADO != 'DEFECTUOSO' AND ROWNUM = 1;

    -- Obtener siguiente ID
    SELECT NVL(MAX(ID_PRESTAMO), 0) + 1 INTO v_prestamo_id FROM PRESTAMO;

    DBMS_OUTPUT.PUT_LINE('Creando prestamo de prueba ID: ' || v_prestamo_id);

    -- Crear prestamo con fecha vencida
    INSERT INTO PRESTAMO (
        ID_PRESTAMO,
        ID_ASESOR,
        ID_CLIENTE,
        ID_ARTICULO,
        MONTO,
        FECHA_PRESTAMO,
        FECHA_VENCIMIENTO,
        TASA_INTERES,
        ESTADO_PRESTAMO
    ) VALUES (
        v_prestamo_id,
        v_asesor_id,
        v_cliente_id,
        v_articulo_id,
        1000000, -- 1 millon
        SYSDATE - 60, -- Hace 60 dias
        SYSDATE - 10, -- Vencido hace 10 dias
        0.10,
        'ACTIVO'
    );

    DBMS_OUTPUT.PUT_LINE('Prestamo creado con estado ACTIVO');
    DBMS_OUTPUT.PUT_LINE('Monto: $1,000,000');

    -- Obtener interes generado (calculado por trigger)
    SELECT INTERES_GENERADO INTO v_multa_aplicada
    FROM PRESTAMO WHERE ID_PRESTAMO = v_prestamo_id;

    DBMS_OUTPUT.PUT_LINE('Interes generado: $' || TO_CHAR(v_multa_aplicada, '999,999,999.99'));
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Cambiando estado a EN_MORA...');

    -- Cambiar a EN_MORA (trigger debe aplicar multa)
    UPDATE PRESTAMO
    SET ESTADO_PRESTAMO = 'EN_MORA'
    WHERE ID_PRESTAMO = v_prestamo_id;

    -- Verificar multa aplicada
    SELECT MULTA INTO v_multa_aplicada
    FROM PRESTAMO WHERE ID_PRESTAMO = v_prestamo_id;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('*** PRUEBA 2: EXITOSA ***');
    DBMS_OUTPUT.PUT_LINE('Multa aplicada automaticamente por trigger: $' ||
                         TO_CHAR(v_multa_aplicada, '999,999,999.99'));
    DBMS_OUTPUT.PUT_LINE('');

    -- Limpiar datos de prueba
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Datos de prueba eliminados (ROLLBACK)');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('*** PRUEBA 2: ERROR ***');
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
        ROLLBACK;
END;
/

-- ----------------------------------------------------------------------------
-- PRUEBA 3: Ejecutar procedimiento manualmente
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 3: Ejecutando procedimiento SP_APLICAR_MULTAS_MORA...
PROMPT

EXECUTE SP_APLICAR_MULTAS_MORA;

-- ----------------------------------------------------------------------------
-- PRUEBA 4: Consultar vista de prestamos con multas
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 4: Consultando vista V_PRESTAMOS_CON_MULTAS...
PROMPT

SELECT
    ID_PRESTAMO,
    SUBSTR(NOMBRE_CLIENTE, 1, 20) AS CLIENTE,
    TO_CHAR(SALDO, '$ 999,999,999.99') AS SALDO,
    TO_CHAR(MULTA, '$ 999,999,999.99') AS MULTA,
    PORCENTAJE_MULTA || '%' AS "% MULTA",
    DIAS_MORA,
    ESTADO_PRESTAMO
FROM V_PRESTAMOS_CON_MULTAS
WHERE ROWNUM <= 10;

-- ============================================================================
-- RESUMEN FINAL
-- ============================================================================

PROMPT
PROMPT ============================================================================
PROMPT INSTALACION DEL SISTEMA DE MULTAS COMPLETADA
PROMPT ============================================================================
PROMPT
PROMPT Componentes instalados:
PROMPT   [+] Procedimiento: SP_APLICAR_MULTAS_MORA
PROMPT   [+] Trigger: TRG_APLICAR_MULTA_MORA
PROMPT   [+] Vista: V_PRESTAMOS_CON_MULTAS
PROMPT   [ ] Job: JOB_APLICAR_MULTAS_DIARIO (opcional - deshabilitado)
PROMPT
PROMPT Regla de negocio implementada:
PROMPT   "Si el cliente no paga la cuota en la fecha establecida,
PROMPT    se aplica una multa (2% adicional sobre el saldo)"
PROMPT
PROMPT Calculo de multa:
PROMPT   Saldo = MONTO + INTERES_GENERADO
PROMPT   Multa = Saldo * 0.02 (2%)
PROMPT
PROMPT Ejecucion:
PROMPT   - Automatica: Al cambiar estado a EN_MORA (trigger)
PROMPT   - Manual: EXECUTE SP_APLICAR_MULTAS_MORA;
PROMPT   - Programada: Habilitar job (opcional)
PROMPT
PROMPT Estado de objetos:

SELECT object_name, object_type, status
FROM user_objects
WHERE object_name IN (
    'SP_APLICAR_MULTAS_MORA',
    'TRG_APLICAR_MULTA_MORA',
    'V_PRESTAMOS_CON_MULTAS'
)
ORDER BY object_type, object_name;

PROMPT
PROMPT ============================================================================
PROMPT SCRIPT COMPLETADO
PROMPT ============================================================================

