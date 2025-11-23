-- ============================================================================
-- SISTEMA DE CAMBIO AUTOMATICO DE ESTADOS DE PRESTAMOS
-- Descripcion: Actualiza automaticamente estados de prestamos segun fechas
--              y condiciones del negocio.
--
-- Autor: Henersson Cobo
-- Proyecto: Sistema de Prestamos y Casa de Empeno
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
--
-- LOGICA:
--   - Si SYSDATE > FECHA_VENCIMIENTO y estado='ACTIVO' → cambiar a 'VENCIDO'
--   - Si hay pago y estado='EN_MORA' → cambiar a 'ACTIVO'
--
-- COMPONENTES:
--   - Procedimiento: SP_ACTUALIZAR_ESTADOS_PRESTAMOS
--   - Trigger: TRG_VALIDAR_ESTADO_FECHA
--   - Job: JOB_ACTUALIZAR_ESTADOS_DIARIO
-- ============================================================================

SET ECHO ON
SET SERVEROUTPUT ON SIZE UNLIMITED
SET LINESIZE 200
SET PAGESIZE 100

PROMPT ============================================================================
PROMPT INSTALACION DEL SISTEMA DE ESTADOS AUTOMATICOS
PROMPT ============================================================================
PROMPT

-- ============================================================================
-- PASO 1: CREAR PROCEDIMIENTO PRINCIPAL
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 1: Creando procedimiento SP_ACTUALIZAR_ESTADOS_PRESTAMOS...
PROMPT ----------------------------------------------------------------------------
PROMPT

CREATE OR REPLACE PROCEDURE SP_ACTUALIZAR_ESTADOS_PRESTAMOS
IS
    -- Variables para estadisticas
    v_total_procesados NUMBER := 0;
    v_activo_a_vencido NUMBER := 0;
    v_activo_a_mora NUMBER := 0;
    v_mora_mantenida NUMBER := 0;

    -- Variables para control de concurrencia
    v_estado_actual VARCHAR2(30);
    v_cambio_realizado BOOLEAN;

    -- Cursor para prestamos que necesitan revision
    CURSOR cur_prestamos IS
        SELECT
            ID_PRESTAMO,
            ID_CLIENTE,
            ESTADO_PRESTAMO,
            FECHA_VENCIMIENTO,
            FECHA_PRESTAMO,
            MONTO,
            INTERES_GENERADO,
            NVL(MULTA, 0) AS MULTA,
            TRUNC(SYSDATE - FECHA_VENCIMIENTO) AS DIAS_DIFERENCIA
        FROM PRESTAMO
        WHERE ESTADO_PRESTAMO IN ('ACTIVO', 'EN_MORA')
          AND FECHA_VENCIMIENTO IS NOT NULL
        FOR UPDATE SKIP LOCKED; -- Manejo de concurrencia

BEGIN
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('ACTUALIZACION AUTOMATICA DE ESTADOS DE PRESTAMOS');
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('Fecha de ejecucion: ' || TO_CHAR(SYSDATE, 'DD/MM/YYYY HH24:MI:SS'));
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Procesar cada prestamo
    FOR prestamo_rec IN cur_prestamos LOOP
        v_total_procesados := v_total_procesados + 1;
        v_cambio_realizado := FALSE;

        BEGIN
            -- REGLA 1: ACTIVO → VENCIDO (si paso la fecha de vencimiento)
            IF prestamo_rec.ESTADO_PRESTAMO = 'ACTIVO'
               AND SYSDATE > prestamo_rec.FECHA_VENCIMIENTO THEN

                -- Verificar si ya paso mas de 0 dias
                IF prestamo_rec.DIAS_DIFERENCIA >= 0 THEN
                    UPDATE PRESTAMO
                    SET ESTADO_PRESTAMO = 'VENCIDO'
                    WHERE ID_PRESTAMO = prestamo_rec.ID_PRESTAMO
                      AND ESTADO_PRESTAMO = 'ACTIVO'; -- Doble verificacion

                    IF SQL%ROWCOUNT > 0 THEN
                        v_activo_a_vencido := v_activo_a_vencido + 1;
                        v_cambio_realizado := TRUE;

                        DBMS_OUTPUT.PUT_LINE('Prestamo ' || prestamo_rec.ID_PRESTAMO ||
                                           ': ACTIVO → VENCIDO (' ||
                                           prestamo_rec.DIAS_DIFERENCIA || ' dias vencido)');
                    END IF;
                END IF;

            -- REGLA 2: ACTIVO → EN_MORA (proximo a vencer - opcional)
            ELSIF prestamo_rec.ESTADO_PRESTAMO = 'ACTIVO'
                  AND prestamo_rec.DIAS_DIFERENCIA BETWEEN -5 AND -1 THEN

                -- Prestamos proximos a vencer (5 dias antes)
                -- Esta regla es opcional, se puede activar si se requiere
                NULL; -- Por ahora no hacemos nada

            -- REGLA 3: EN_MORA se mantiene hasta que se pague o venza completamente
            ELSIF prestamo_rec.ESTADO_PRESTAMO = 'EN_MORA' THEN

                -- Si ya paso la fecha de vencimiento, cambiar a VENCIDO
                IF prestamo_rec.DIAS_DIFERENCIA >= 0 THEN
                    UPDATE PRESTAMO
                    SET ESTADO_PRESTAMO = 'VENCIDO'
                    WHERE ID_PRESTAMO = prestamo_rec.ID_PRESTAMO
                      AND ESTADO_PRESTAMO = 'EN_MORA';

                    IF SQL%ROWCOUNT > 0 THEN
                        v_activo_a_vencido := v_activo_a_vencido + 1;
                        v_cambio_realizado := TRUE;

                        DBMS_OUTPUT.PUT_LINE('Prestamo ' || prestamo_rec.ID_PRESTAMO ||
                                           ': EN_MORA → VENCIDO (' ||
                                           prestamo_rec.DIAS_DIFERENCIA || ' dias vencido)');
                    END IF;
                ELSE
                    v_mora_mantenida := v_mora_mantenida + 1;
                END IF;

            END IF;

        EXCEPTION
            WHEN OTHERS THEN
                DBMS_OUTPUT.PUT_LINE('ERROR en prestamo ' || prestamo_rec.ID_PRESTAMO || ': ' || SQLERRM);
        END;

    END LOOP;

    -- Commit de cambios
    COMMIT;

    -- Resumen
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('RESUMEN DE ACTUALIZACION DE ESTADOS');
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('Total prestamos procesados: ' || v_total_procesados);
    DBMS_OUTPUT.PUT_LINE('ACTIVO → VENCIDO: ' || v_activo_a_vencido);
    DBMS_OUTPUT.PUT_LINE('EN_MORA (mantenido): ' || v_mora_mantenida);
    DBMS_OUTPUT.PUT_LINE('============================================================================');

    IF v_total_procesados = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No hay prestamos que requieran actualizacion de estado.');
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('============================================================================');
        DBMS_OUTPUT.PUT_LINE('ERROR GENERAL EN ACTUALIZACION DE ESTADOS');
        DBMS_OUTPUT.PUT_LINE('============================================================================');
        DBMS_OUTPUT.PUT_LINE('Codigo de error: ' || SQLCODE);
        DBMS_OUTPUT.PUT_LINE('Mensaje: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Se realizo ROLLBACK de todos los cambios');
        DBMS_OUTPUT.PUT_LINE('============================================================================');
        RAISE;
END SP_ACTUALIZAR_ESTADOS_PRESTAMOS;
/

COMMENT ON PROCEDURE SP_ACTUALIZAR_ESTADOS_PRESTAMOS IS
'Actualiza automaticamente estados de prestamos segun fechas de vencimiento.
ACTIVO → VENCIDO si paso la fecha de vencimiento.
EN_MORA → VENCIDO si paso la fecha de vencimiento.
Incluye manejo de concurrencia (FOR UPDATE SKIP LOCKED).
Autor: Henersson Cobo';

PROMPT Procedimiento SP_ACTUALIZAR_ESTADOS_PRESTAMOS creado exitosamente
PROMPT

-- ============================================================================
-- PASO 2: CREAR TRIGGER DE VALIDACION DE ESTADO
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 2: Creando trigger TRG_VALIDAR_ESTADO_FECHA...
PROMPT ----------------------------------------------------------------------------
PROMPT

CREATE OR REPLACE TRIGGER TRG_VALIDAR_ESTADO_FECHA
BEFORE INSERT OR UPDATE ON PRESTAMO
FOR EACH ROW
BEGIN
    -- VALIDACION 1: Si se inserta/actualiza con fecha vencida, ajustar estado
    IF :NEW.FECHA_VENCIMIENTO IS NOT NULL
       AND :NEW.ESTADO_PRESTAMO = 'ACTIVO'
       AND SYSDATE > :NEW.FECHA_VENCIMIENTO THEN

        -- Cambiar automaticamente a VENCIDO
        :NEW.ESTADO_PRESTAMO := 'VENCIDO';

        DBMS_OUTPUT.PUT_LINE('========================================');
        DBMS_OUTPUT.PUT_LINE('ESTADO AJUSTADO AUTOMATICAMENTE');
        DBMS_OUTPUT.PUT_LINE('Prestamo ' || NVL(TO_CHAR(:NEW.ID_PRESTAMO), 'NUEVO'));
        DBMS_OUTPUT.PUT_LINE('Estado intentado: ACTIVO');
        DBMS_OUTPUT.PUT_LINE('Estado ajustado: VENCIDO');
        DBMS_OUTPUT.PUT_LINE('Razon: Fecha de vencimiento ya paso');
        DBMS_OUTPUT.PUT_LINE('========================================');
    END IF;

    -- VALIDACION 2: Si se intenta cambiar de VENCIDO a ACTIVO, validar fecha
    IF :OLD.ESTADO_PRESTAMO IS NOT NULL
       AND :OLD.ESTADO_PRESTAMO IN ('VENCIDO', 'EN_MORA')
       AND :NEW.ESTADO_PRESTAMO = 'ACTIVO'
       AND SYSDATE > :NEW.FECHA_VENCIMIENTO THEN

        RAISE_APPLICATION_ERROR(-20010,
            'ERROR: No se puede cambiar a estado ACTIVO.' || CHR(10) ||
            'La fecha de vencimiento ya paso (' ||
            TO_CHAR(:NEW.FECHA_VENCIMIENTO, 'DD/MM/YYYY') || ').' || CHR(10) ||
            'Para reactivar el prestamo, extienda primero la fecha de vencimiento.');
    END IF;

    -- VALIDACION 3: Registro en log de cambios de estado
    IF :OLD.ESTADO_PRESTAMO IS NOT NULL
       AND :OLD.ESTADO_PRESTAMO != :NEW.ESTADO_PRESTAMO THEN

        DBMS_OUTPUT.PUT_LINE('Cambio de estado - Prestamo ' || :NEW.ID_PRESTAMO ||
                           ': ' || :OLD.ESTADO_PRESTAMO || ' → ' || :NEW.ESTADO_PRESTAMO);
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        -- Re-lanzar errores personalizados
        IF SQLCODE BETWEEN -20999 AND -20001 THEN
            RAISE;
        ELSE
            RAISE_APPLICATION_ERROR(-20099,
                'ERROR en trigger de validacion de estado: ' || SQLERRM);
        END IF;
END TRG_VALIDAR_ESTADO_FECHA;
/

COMMENT ON TRIGGER TRG_VALIDAR_ESTADO_FECHA IS
'Valida y ajusta automaticamente el estado de prestamos segun fecha de vencimiento.
Previene estados inconsistentes con la fecha actual.
Se ejecuta BEFORE INSERT OR UPDATE en PRESTAMO.
Autor: Henersson Cobo';

PROMPT Trigger TRG_VALIDAR_ESTADO_FECHA creado exitosamente
PROMPT

-- ============================================================================
-- PASO 3: CREAR PROCEDIMIENTO INTEGRADO CON MULTAS
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 3: Creando procedimiento integrado SP_PROCESO_DIARIO_PRESTAMOS...
PROMPT ----------------------------------------------------------------------------
PROMPT

CREATE OR REPLACE PROCEDURE SP_PROCESO_DIARIO_PRESTAMOS
IS
    v_inicio TIMESTAMP;
    v_fin TIMESTAMP;
    v_duracion NUMBER;
BEGIN
    v_inicio := SYSTIMESTAMP;

    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('PROCESO DIARIO DE PRESTAMOS');
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('Inicio: ' || TO_CHAR(v_inicio, 'DD/MM/YYYY HH24:MI:SS'));
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- PASO 1: Actualizar estados de prestamos
    DBMS_OUTPUT.PUT_LINE('>>> PASO 1: Actualizando estados de prestamos...');
    DBMS_OUTPUT.PUT_LINE('');
    BEGIN
        SP_ACTUALIZAR_ESTADOS_PRESTAMOS;
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('Estado: COMPLETADO');
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Estado: ERROR - ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('----------------------------------------------------------------------------');
    DBMS_OUTPUT.PUT_LINE('');

    -- PASO 2: Aplicar multas a prestamos en mora
    DBMS_OUTPUT.PUT_LINE('>>> PASO 2: Aplicando multas a prestamos en mora...');
    DBMS_OUTPUT.PUT_LINE('');
    BEGIN
        SP_APLICAR_MULTAS_MORA;
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('Estado: COMPLETADO');
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Estado: ERROR - ' || SQLERRM);
    END;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('----------------------------------------------------------------------------');
    DBMS_OUTPUT.PUT_LINE('');

    -- PASO 3: Transferir articulos vencidos (opcional - mas de 30 dias)
    DBMS_OUTPUT.PUT_LINE('>>> PASO 3: Verificando articulos para transferencia...');
    DBMS_OUTPUT.PUT_LINE('');
    BEGIN
        SP_TRANSFERIR_ARTICULOS_VENCIDOS(30);
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('Estado: COMPLETADO');
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Estado: ERROR - ' || SQLERRM);
    END;

    v_fin := SYSTIMESTAMP;
    v_duracion := EXTRACT(SECOND FROM (v_fin - v_inicio));

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('PROCESO DIARIO COMPLETADO');
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('Fin: ' || TO_CHAR(v_fin, 'DD/MM/YYYY HH24:MI:SS'));
    DBMS_OUTPUT.PUT_LINE('Duracion: ' || ROUND(v_duracion, 2) || ' segundos');
    DBMS_OUTPUT.PUT_LINE('============================================================================');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('============================================================================');
        DBMS_OUTPUT.PUT_LINE('ERROR EN PROCESO DIARIO');
        DBMS_OUTPUT.PUT_LINE('============================================================================');
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('============================================================================');
        RAISE;
END SP_PROCESO_DIARIO_PRESTAMOS;
/

COMMENT ON PROCEDURE SP_PROCESO_DIARIO_PRESTAMOS IS
'Proceso diario integrado que ejecuta:
1. Actualizacion de estados de prestamos
2. Aplicacion de multas a prestamos en mora
3. Transferencia de articulos vencidos
Ideal para programar como job diario.
Autor: Henersson Cobo';

PROMPT Procedimiento SP_PROCESO_DIARIO_PRESTAMOS creado exitosamente
PROMPT

-- ============================================================================
-- PASO 4: CREAR JOB PROGRAMADO
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 4: Creando job programado JOB_PROCESO_DIARIO_PRESTAMOS...
PROMPT ----------------------------------------------------------------------------
PROMPT

-- Eliminar job si existe
BEGIN
    DBMS_SCHEDULER.DROP_JOB(job_name => 'JOB_PROCESO_DIARIO_PRESTAMOS', force => TRUE);
    DBMS_OUTPUT.PUT_LINE('Job anterior eliminado');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Job no existia previamente');
END;
/

-- Crear job que se ejecuta diariamente
BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'JOB_PROCESO_DIARIO_PRESTAMOS',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'BEGIN SP_PROCESO_DIARIO_PRESTAMOS; END;',
        start_date      => TRUNC(SYSDATE + 1) + 1/24, -- Manana a las 01:00 AM
        repeat_interval => 'FREQ=DAILY; BYHOUR=1; BYMINUTE=0',
        enabled         => FALSE, -- Deshabilitado por defecto
        comments        => 'Proceso diario: actualiza estados, aplica multas y transfiere articulos'
    );

    DBMS_OUTPUT.PUT_LINE('Job JOB_PROCESO_DIARIO_PRESTAMOS creado (DESHABILITADO)');
    DBMS_OUTPUT.PUT_LINE('Configurado para ejecutarse diariamente a las 01:00 AM');
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Para habilitar el job, ejecute:');
    DBMS_OUTPUT.PUT_LINE('  EXEC DBMS_SCHEDULER.ENABLE(''JOB_PROCESO_DIARIO_PRESTAMOS'');');
END;
/

PROMPT Job programado creado exitosamente
PROMPT

-- ============================================================================
-- PASO 5: CREAR VISTA DE MONITOREO
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 5: Creando vista V_MONITOREO_PRESTAMOS...
PROMPT ----------------------------------------------------------------------------
PROMPT

CREATE OR REPLACE VIEW V_MONITOREO_PRESTAMOS AS
SELECT
    ESTADO_PRESTAMO,
    COUNT(*) AS CANTIDAD,
    TO_CHAR(SUM(MONTO), '$ 999,999,999.99') AS MONTO_TOTAL,
    TO_CHAR(SUM(INTERES_GENERADO), '$ 999,999,999.99') AS INTERES_TOTAL,
    TO_CHAR(SUM(NVL(MULTA, 0)), '$ 999,999,999.99') AS MULTAS_TOTAL,
    TO_CHAR(SUM(MONTO + INTERES_GENERADO + NVL(MULTA, 0)), '$ 999,999,999.99') AS DEUDA_TOTAL,
    ROUND(AVG(TRUNC(SYSDATE - FECHA_VENCIMIENTO)), 2) AS PROMEDIO_DIAS_VENCIMIENTO,
    MIN(FECHA_VENCIMIENTO) AS FECHA_VENC_MAS_ANTIGUA,
    MAX(FECHA_VENCIMIENTO) AS FECHA_VENC_MAS_RECIENTE
FROM PRESTAMO
GROUP BY ESTADO_PRESTAMO
ORDER BY
    CASE ESTADO_PRESTAMO
        WHEN 'VENCIDO' THEN 1
        WHEN 'EN_MORA' THEN 2
        WHEN 'ACTIVO' THEN 3
        WHEN 'CANCELADO' THEN 4
        WHEN 'ARTICULO_TRANSFERIDO' THEN 5
        ELSE 6
    END;

COMMENT ON VIEW V_MONITOREO_PRESTAMOS IS
'Vista de monitoreo para estado de prestamos.
Agrupa por estado con estadisticas financieras.
Autor: Henersson Cobo';

PROMPT Vista V_MONITOREO_PRESTAMOS creada exitosamente
PROMPT

-- ============================================================================
-- PRUEBAS DEL SISTEMA
-- ============================================================================

PROMPT ============================================================================
PROMPT EJECUTANDO PRUEBAS DEL SISTEMA
PROMPT ============================================================================
PROMPT

-- ----------------------------------------------------------------------------
-- PRUEBA 1: Verificar monitoreo actual
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 1: Estado actual de prestamos...
PROMPT

SELECT * FROM V_MONITOREO_PRESTAMOS;

-- ----------------------------------------------------------------------------
-- PRUEBA 2: Crear prestamo de prueba y verificar ajuste automatico
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 2: Probando ajuste automatico de estado...
PROMPT

DECLARE
    v_prestamo_id NUMBER;
    v_cliente_id NUMBER;
    v_asesor_id NUMBER;
    v_articulo_id NUMBER;
    v_estado_final VARCHAR2(30);
BEGIN
    -- Obtener IDs
    SELECT ID_PERSONA INTO v_cliente_id
    FROM PERSONA WHERE TIPO_PERSONA = 'CLIENTE' AND ROWNUM = 1;

    SELECT ID_PERSONA INTO v_asesor_id
    FROM PERSONA WHERE TIPO_PERSONA = 'ASESOR' AND ROWNUM = 1;

    SELECT ID_ARTICULO INTO v_articulo_id
    FROM ARTICULO WHERE ESTADO NOT IN ('DEFECTUOSO', 'PROPIEDAD_CASA') AND ROWNUM = 1;

    SELECT NVL(MAX(ID_PRESTAMO), 0) + 1 INTO v_prestamo_id FROM PRESTAMO;

    DBMS_OUTPUT.PUT_LINE('Intentando crear prestamo con estado ACTIVO pero fecha vencida...');

    -- Intentar crear con fecha vencida y estado ACTIVO
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
        500000,
        SYSDATE - 20,
        SYSDATE - 5, -- Vencido hace 5 dias
        0.10,
        'ACTIVO' -- Intentar ACTIVO con fecha vencida
    );

    -- Verificar estado final
    SELECT ESTADO_PRESTAMO INTO v_estado_final
    FROM PRESTAMO WHERE ID_PRESTAMO = v_prestamo_id;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('*** PRUEBA 2: RESULTADO ***');
    DBMS_OUTPUT.PUT_LINE('Estado solicitado: ACTIVO');
    DBMS_OUTPUT.PUT_LINE('Estado asignado: ' || v_estado_final);

    IF v_estado_final = 'VENCIDO' THEN
        DBMS_OUTPUT.PUT_LINE('*** PRUEBA 2: EXITOSA ***');
        DBMS_OUTPUT.PUT_LINE('El trigger ajusto automaticamente el estado');
    ELSE
        DBMS_OUTPUT.PUT_LINE('*** PRUEBA 2: FALLIDA ***');
    END IF;

    ROLLBACK;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('*** PRUEBA 2: ERROR ***');
        DBMS_OUTPUT.PUT_LINE(SQLERRM);
        ROLLBACK;
END;
/

-- ----------------------------------------------------------------------------
-- PRUEBA 3: Ejecutar procedimiento de actualizacion
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 3: Ejecutando procedimiento de actualizacion de estados...
PROMPT

EXECUTE SP_ACTUALIZAR_ESTADOS_PRESTAMOS;

-- ----------------------------------------------------------------------------
-- PRUEBA 4: Verificar estado del job
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 4: Verificando configuracion del job...
PROMPT

SELECT
    job_name,
    enabled,
    state,
    TO_CHAR(next_run_date, 'DD/MM/YYYY HH24:MI:SS') AS proxima_ejecucion,
    repeat_interval
FROM user_scheduler_jobs
WHERE job_name = 'JOB_PROCESO_DIARIO_PRESTAMOS';

-- ============================================================================
-- RESUMEN FINAL
-- ============================================================================

PROMPT
PROMPT ============================================================================
PROMPT INSTALACION DEL SISTEMA DE ESTADOS AUTOMATICOS COMPLETADA
PROMPT ============================================================================
PROMPT
PROMPT Componentes instalados:
PROMPT   [+] Procedimiento: SP_ACTUALIZAR_ESTADOS_PRESTAMOS
PROMPT   [+] Procedimiento: SP_PROCESO_DIARIO_PRESTAMOS (integrado)
PROMPT   [+] Trigger: TRG_VALIDAR_ESTADO_FECHA
PROMPT   [+] Job: JOB_PROCESO_DIARIO_PRESTAMOS (deshabilitado)
PROMPT   [+] Vista: V_MONITOREO_PRESTAMOS
PROMPT
PROMPT Logica implementada:
PROMPT   - ACTIVO → VENCIDO (si paso fecha de vencimiento)
PROMPT   - EN_MORA → VENCIDO (si paso fecha de vencimiento)
PROMPT   - Validacion automatica en INSERT/UPDATE
PROMPT   - Manejo de concurrencia (SKIP LOCKED)
PROMPT
PROMPT Ejecucion manual:
PROMPT   EXECUTE SP_ACTUALIZAR_ESTADOS_PRESTAMOS;
PROMPT   EXECUTE SP_PROCESO_DIARIO_PRESTAMOS;
PROMPT
PROMPT Habilitar job programado:
PROMPT   EXEC DBMS_SCHEDULER.ENABLE('JOB_PROCESO_DIARIO_PRESTAMOS');
PROMPT
PROMPT Deshabilitar job:
PROMPT   EXEC DBMS_SCHEDULER.DISABLE('JOB_PROCESO_DIARIO_PRESTAMOS');
PROMPT
PROMPT Consultas utiles:
PROMPT   SELECT * FROM V_MONITOREO_PRESTAMOS;
PROMPT
PROMPT Estado de objetos:

SELECT object_name, object_type, status
FROM user_objects
WHERE object_name IN (
    'SP_ACTUALIZAR_ESTADOS_PRESTAMOS',
    'SP_PROCESO_DIARIO_PRESTAMOS',
    'TRG_VALIDAR_ESTADO_FECHA',
    'V_MONITOREO_PRESTAMOS'
)
ORDER BY object_type, object_name;

PROMPT
PROMPT ============================================================================
PROMPT SCRIPT COMPLETADO
PROMPT ============================================================================

