-- ============================================================================
-- SISTEMA DE TRANSFERENCIA DE ARTICULOS VENCIDOS
-- Descripcion: Implementa la regla de negocio del universo del discurso:
--              "Si el cliente no paga la deuda en el plazo total,
--               el articulo pasa a ser propiedad de la casa de empeno"
--
-- Autor: Henersson Cobo
-- Proyecto: Sistema de Prestamos y Casa de Empeno
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
--
-- COMPONENTES:
--   - Nuevo estado 'PROPIEDAD_CASA' en ARTICULO
--   - Procedimiento: SP_TRANSFERIR_ARTICULOS_VENCIDOS
--   - Vista: V_ARTICULOS_CASA_EMPENO
-- ============================================================================

SET ECHO ON
SET SERVEROUTPUT ON SIZE UNLIMITED
SET LINESIZE 200
SET PAGESIZE 100

PROMPT ============================================================================
PROMPT INSTALACION DEL SISTEMA DE TRANSFERENCIA DE ARTICULOS
PROMPT ============================================================================
PROMPT

-- ============================================================================
-- PASO 1: AGREGAR NUEVO ESTADO A ARTICULO
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 1: Agregando estado PROPIEDAD_CASA a tabla ARTICULO...
PROMPT ----------------------------------------------------------------------------
PROMPT

-- Nota: Si el campo ESTADO tiene constraint CHECK, debemos modificarlo
-- Verificar constraint actual

SELECT constraint_name, search_condition
FROM user_constraints
WHERE table_name = 'ARTICULO'
  AND constraint_type = 'C'
  AND constraint_name LIKE '%ESTADO%';

PROMPT
PROMPT Actualizando constraint de estado (si existe)...

-- Eliminar constraint antigua si existe
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE ARTICULO DROP CONSTRAINT CHK_ARTICULO_ESTADO';
    DBMS_OUTPUT.PUT_LINE('Constraint CHK_ARTICULO_ESTADO eliminado');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Constraint no existia o tiene otro nombre');
END;
/

-- Crear nuevo constraint con estado PROPIEDAD_CASA
ALTER TABLE ARTICULO ADD CONSTRAINT CHK_ARTICULO_ESTADO
CHECK (ESTADO IN ('OPTIMO', 'BUENO', 'REGULAR', 'DEFECTUOSO', 'PROPIEDAD_CASA'));

COMMENT ON CONSTRAINT CHK_ARTICULO_ESTADO IS
'Estados validos del articulo:
- OPTIMO: Excelente condicion
- BUENO: Buena condicion
- REGULAR: Condicion aceptable
- DEFECTUOSO: No aceptado como garantia
- PROPIEDAD_CASA: Transferido a la casa de empeno por impago';

PROMPT Estado PROPIEDAD_CASA agregado exitosamente
PROMPT

-- ============================================================================
-- PASO 2: AGREGAR CAMPO PARA RASTREAR CLIENTE ORIGINAL (OPCIONAL)
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 2: Agregando campos de auditoria a ARTICULO...
PROMPT ----------------------------------------------------------------------------
PROMPT

-- Agregar campo para guardar el cliente original antes de la transferencia
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE ARTICULO ADD (ID_CLIENTE_ORIGINAL NUMBER(10))';
    DBMS_OUTPUT.PUT_LINE('Campo ID_CLIENTE_ORIGINAL agregado');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -1430 THEN
            DBMS_OUTPUT.PUT_LINE('Campo ID_CLIENTE_ORIGINAL ya existe');
        ELSE
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE ARTICULO ADD (FECHA_TRANSFERENCIA DATE)';
    DBMS_OUTPUT.PUT_LINE('Campo FECHA_TRANSFERENCIA agregado');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -1430 THEN
            DBMS_OUTPUT.PUT_LINE('Campo FECHA_TRANSFERENCIA ya existe');
        ELSE
            RAISE;
        END IF;
END;
/

COMMENT ON COLUMN ARTICULO.ID_CLIENTE_ORIGINAL IS
'ID del cliente propietario antes de la transferencia por impago';

COMMENT ON COLUMN ARTICULO.FECHA_TRANSFERENCIA IS
'Fecha en que el articulo paso a ser propiedad de la casa de empeno';

PROMPT Campos de auditoria agregados exitosamente
PROMPT

-- ============================================================================
-- PASO 3: AGREGAR NUEVO ESTADO A PRESTAMO
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 3: Agregando estado ARTICULO_TRANSFERIDO a PRESTAMO...
PROMPT ----------------------------------------------------------------------------
PROMPT

-- Eliminar constraint antigua si existe
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE PRESTAMO DROP CONSTRAINT CHK_PRESTAMO_ESTADO';
    DBMS_OUTPUT.PUT_LINE('Constraint CHK_PRESTAMO_ESTADO eliminado');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Constraint no existia o tiene otro nombre');
END;
/

-- Crear nuevo constraint
ALTER TABLE PRESTAMO ADD CONSTRAINT CHK_PRESTAMO_ESTADO
CHECK (ESTADO_PRESTAMO IN ('ACTIVO', 'CANCELADO', 'EN_MORA', 'VENCIDO', 'ARTICULO_TRANSFERIDO'));

COMMENT ON CONSTRAINT CHK_PRESTAMO_ESTADO IS
'Estados validos del prestamo:
- ACTIVO: Prestamo en curso
- CANCELADO: Prestamo pagado completamente
- EN_MORA: Prestamo con retraso en pagos
- VENCIDO: Prestamo no pagado despues del vencimiento
- ARTICULO_TRANSFERIDO: Articulo paso a propiedad de la casa';

PROMPT Estado ARTICULO_TRANSFERIDO agregado exitosamente
PROMPT

-- ============================================================================
-- PASO 4: CREAR PROCEDIMIENTO DE TRANSFERENCIA
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 4: Creando procedimiento SP_TRANSFERIR_ARTICULOS_VENCIDOS...
PROMPT ----------------------------------------------------------------------------
PROMPT

CREATE OR REPLACE PROCEDURE SP_TRANSFERIR_ARTICULOS_VENCIDOS(
    p_dias_minimos NUMBER DEFAULT 30
)
IS
    -- Variables para estadisticas
    v_total_procesados NUMBER := 0;
    v_total_transferidos NUMBER := 0;
    v_valor_total NUMBER := 0;

    -- Cursor para prestamos vencidos
    CURSOR cur_prestamos_vencidos IS
        SELECT
            p.ID_PRESTAMO,
            p.ID_CLIENTE,
            p.ID_ARTICULO,
            p.MONTO,
            p.INTERES_GENERADO,
            NVL(p.MULTA, 0) AS MULTA,
            p.FECHA_VENCIMIENTO,
            TRUNC(SYSDATE - p.FECHA_VENCIMIENTO) AS DIAS_VENCIDO,
            a.TIPO_ARTICULO,
            a.DESCRIPCION_ARTICULO,
            a.VALOR_TASADO,
            a.ESTADO AS ESTADO_ARTICULO,
            per.NOMBRE_PERSONA AS NOMBRE_CLIENTE
        FROM PRESTAMO p
        JOIN ARTICULO a ON p.ID_ARTICULO = a.ID_ARTICULO
        JOIN PERSONA per ON p.ID_CLIENTE = per.ID_PERSONA
        WHERE p.ESTADO_PRESTAMO = 'VENCIDO'
          AND TRUNC(SYSDATE - p.FECHA_VENCIMIENTO) >= p_dias_minimos
          AND a.ESTADO != 'PROPIEDAD_CASA' -- No procesar articulos ya transferidos
        ORDER BY p.FECHA_VENCIMIENTO;

BEGIN
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('TRANSFERENCIA DE ARTICULOS VENCIDOS A PROPIEDAD DE LA CASA');
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('Fecha de ejecucion: ' || TO_CHAR(SYSDATE, 'DD/MM/YYYY HH24:MI:SS'));
    DBMS_OUTPUT.PUT_LINE('Criterio: Prestamos vencidos con mas de ' || p_dias_minimos || ' dias');
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('');

    -- Procesar cada prestamo vencido
    FOR prestamo_rec IN cur_prestamos_vencidos LOOP
        v_total_procesados := v_total_procesados + 1;

        BEGIN
            -- 1. Actualizar articulo a PROPIEDAD_CASA
            UPDATE ARTICULO
            SET ESTADO = 'PROPIEDAD_CASA',
                ID_CLIENTE_ORIGINAL = prestamo_rec.ID_CLIENTE,
                FECHA_TRANSFERENCIA = SYSDATE
            WHERE ID_ARTICULO = prestamo_rec.ID_ARTICULO;

            -- 2. Actualizar estado del prestamo
            UPDATE PRESTAMO
            SET ESTADO_PRESTAMO = 'ARTICULO_TRANSFERIDO'
            WHERE ID_PRESTAMO = prestamo_rec.ID_PRESTAMO;

            -- Registrar en log
            DBMS_OUTPUT.PUT_LINE('========================================');
            DBMS_OUTPUT.PUT_LINE('ARTICULO TRANSFERIDO');
            DBMS_OUTPUT.PUT_LINE('========================================');
            DBMS_OUTPUT.PUT_LINE('Prestamo ID: ' || prestamo_rec.ID_PRESTAMO);
            DBMS_OUTPUT.PUT_LINE('Cliente: ' || prestamo_rec.NOMBRE_CLIENTE);
            DBMS_OUTPUT.PUT_LINE('Dias vencido: ' || prestamo_rec.DIAS_VENCIDO);
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('Articulo ID: ' || prestamo_rec.ID_ARTICULO);
            DBMS_OUTPUT.PUT_LINE('Tipo: ' || prestamo_rec.TIPO_ARTICULO);
            DBMS_OUTPUT.PUT_LINE('Descripcion: ' || SUBSTR(prestamo_rec.DESCRIPCION_ARTICULO, 1, 50));
            DBMS_OUTPUT.PUT_LINE('Valor tasado: $' || TO_CHAR(prestamo_rec.VALOR_TASADO, '999,999,999.99'));
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('Deuda no pagada:');
            DBMS_OUTPUT.PUT_LINE('  Monto: $' || TO_CHAR(prestamo_rec.MONTO, '999,999,999.99'));
            DBMS_OUTPUT.PUT_LINE('  Interes: $' || TO_CHAR(prestamo_rec.INTERES_GENERADO, '999,999,999.99'));
            DBMS_OUTPUT.PUT_LINE('  Multa: $' || TO_CHAR(prestamo_rec.MULTA, '999,999,999.99'));
            DBMS_OUTPUT.PUT_LINE('  Total: $' || TO_CHAR(prestamo_rec.MONTO + prestamo_rec.INTERES_GENERADO + prestamo_rec.MULTA, '999,999,999.99'));
            DBMS_OUTPUT.PUT_LINE('========================================');
            DBMS_OUTPUT.PUT_LINE('');

            v_total_transferidos := v_total_transferidos + 1;
            v_valor_total := v_valor_total + prestamo_rec.VALOR_TASADO;

        EXCEPTION
            WHEN OTHERS THEN
                DBMS_OUTPUT.PUT_LINE('ERROR al transferir prestamo ' || prestamo_rec.ID_PRESTAMO || ': ' || SQLERRM);
                DBMS_OUTPUT.PUT_LINE('');
        END;

    END LOOP;

    -- Commit de cambios
    COMMIT;

    -- Resumen
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('RESUMEN DE TRANSFERENCIA DE ARTICULOS');
    DBMS_OUTPUT.PUT_LINE('============================================================================');
    DBMS_OUTPUT.PUT_LINE('Total prestamos procesados: ' || v_total_procesados);
    DBMS_OUTPUT.PUT_LINE('Total articulos transferidos: ' || v_total_transferidos);
    DBMS_OUTPUT.PUT_LINE('Valor total de articulos: $' || TO_CHAR(v_valor_total, '999,999,999.99'));
    DBMS_OUTPUT.PUT_LINE('============================================================================');

    IF v_total_procesados = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No hay prestamos vencidos que cumplan el criterio de ' || p_dias_minimos || ' dias.');
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('============================================================================');
        DBMS_OUTPUT.PUT_LINE('ERROR GENERAL EN LA TRANSFERENCIA');
        DBMS_OUTPUT.PUT_LINE('============================================================================');
        DBMS_OUTPUT.PUT_LINE('Codigo de error: ' || SQLCODE);
        DBMS_OUTPUT.PUT_LINE('Mensaje: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Se realizo ROLLBACK de todos los cambios');
        DBMS_OUTPUT.PUT_LINE('============================================================================');
        RAISE;
END SP_TRANSFERIR_ARTICULOS_VENCIDOS;
/

COMMENT ON PROCEDURE SP_TRANSFERIR_ARTICULOS_VENCIDOS IS
'Transfiere articulos de prestamos vencidos a propiedad de la casa de empeno.
Criterio: Prestamos vencidos con mas de N dias (default 30).
Actualiza estado de articulo a PROPIEDAD_CASA y prestamo a ARTICULO_TRANSFERIDO.
Incluye auditoria completa y manejo de errores.
Autor: Henersson Cobo';

PROMPT Procedimiento SP_TRANSFERIR_ARTICULOS_VENCIDOS creado exitosamente
PROMPT

-- ============================================================================
-- PASO 5: CREAR VISTA DE INVENTARIO DE ARTICULOS
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 5: Creando vista V_ARTICULOS_CASA_EMPENO...
PROMPT ----------------------------------------------------------------------------
PROMPT

CREATE OR REPLACE VIEW V_ARTICULOS_CASA_EMPENO AS
SELECT
    a.ID_ARTICULO,
    a.TIPO_ARTICULO,
    a.DESCRIPCION_ARTICULO,
    a.VALOR_TASADO,
    a.ESTADO,
    a.FECHA_TRANSFERENCIA,
    a.ID_CLIENTE_ORIGINAL,
    per.NOMBRE_PERSONA AS CLIENTE_ORIGINAL,
    TRUNC(SYSDATE - a.FECHA_TRANSFERENCIA) AS DIAS_EN_INVENTARIO,
    CASE
        WHEN a.ESTADO = 'PROPIEDAD_CASA' THEN 'Disponible para venta'
        ELSE 'En garantia'
    END AS ESTADO_INVENTARIO,
    -- Informacion del prestamo original (si existe)
    p.ID_PRESTAMO,
    p.MONTO AS MONTO_PRESTAMO,
    p.INTERES_GENERADO,
    NVL(p.MULTA, 0) AS MULTA,
    (p.MONTO + p.INTERES_GENERADO + NVL(p.MULTA, 0)) AS DEUDA_NO_PAGADA,
    p.FECHA_VENCIMIENTO,
    TRUNC(SYSDATE - p.FECHA_VENCIMIENTO) AS DIAS_VENCIDO
FROM ARTICULO a
LEFT JOIN PERSONA per ON a.ID_CLIENTE_ORIGINAL = per.ID_PERSONA
LEFT JOIN PRESTAMO p ON a.ID_ARTICULO = p.ID_ARTICULO
    AND p.ESTADO_PRESTAMO = 'ARTICULO_TRANSFERIDO'
WHERE a.ESTADO = 'PROPIEDAD_CASA'
ORDER BY a.FECHA_TRANSFERENCIA DESC;

COMMENT ON VIEW V_ARTICULOS_CASA_EMPENO IS
'Vista de articulos que son propiedad de la casa de empeno.
Incluye informacion del cliente original, deuda no pagada y dias en inventario.
Util para reportes de inventario y gestion de articulos transferidos.
Autor: Henersson Cobo';

PROMPT Vista V_ARTICULOS_CASA_EMPENO creada exitosamente
PROMPT

-- ============================================================================
-- PASO 6: CREAR VISTA DE PRESTAMOS CANDIDATOS A TRANSFERENCIA
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 6: Creando vista V_PRESTAMOS_CANDIDATOS_TRANSFERENCIA...
PROMPT ----------------------------------------------------------------------------
PROMPT

CREATE OR REPLACE VIEW V_PRESTAMOS_CANDIDATOS_TRANSFERENCIA AS
SELECT
    p.ID_PRESTAMO,
    p.ID_CLIENTE,
    per.NOMBRE_PERSONA AS CLIENTE,
    p.ID_ARTICULO,
    a.TIPO_ARTICULO,
    a.DESCRIPCION_ARTICULO,
    a.VALOR_TASADO,
    p.MONTO,
    p.INTERES_GENERADO,
    NVL(p.MULTA, 0) AS MULTA,
    (p.MONTO + p.INTERES_GENERADO + NVL(p.MULTA, 0)) AS DEUDA_TOTAL,
    p.FECHA_VENCIMIENTO,
    TRUNC(SYSDATE - p.FECHA_VENCIMIENTO) AS DIAS_VENCIDO,
    CASE
        WHEN TRUNC(SYSDATE - p.FECHA_VENCIMIENTO) >= 30 THEN 'LISTO PARA TRANSFERENCIA'
        WHEN TRUNC(SYSDATE - p.FECHA_VENCIMIENTO) >= 20 THEN 'PROXIMO A TRANSFERENCIA'
        ELSE 'EN PLAZO DE GRACIA'
    END AS ESTADO_TRANSFERENCIA
FROM PRESTAMO p
JOIN PERSONA per ON p.ID_CLIENTE = per.ID_PERSONA
JOIN ARTICULO a ON p.ID_ARTICULO = a.ID_ARTICULO
WHERE p.ESTADO_PRESTAMO = 'VENCIDO'
  AND a.ESTADO != 'PROPIEDAD_CASA'
ORDER BY DIAS_VENCIDO DESC;

COMMENT ON VIEW V_PRESTAMOS_CANDIDATOS_TRANSFERENCIA IS
'Vista de prestamos vencidos candidatos a transferencia de articulo.
Muestra dias vencidos y estado de transferencia.
Autor: Henersson Cobo';

PROMPT Vista V_PRESTAMOS_CANDIDATOS_TRANSFERENCIA creada exitosamente
PROMPT

-- ============================================================================
-- PRUEBAS DEL SISTEMA
-- ============================================================================

PROMPT ============================================================================
PROMPT EJECUTANDO PRUEBAS DEL SISTEMA DE TRANSFERENCIA
PROMPT ============================================================================
PROMPT

-- ----------------------------------------------------------------------------
-- PRUEBA 1: Verificar estados disponibles
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 1: Verificando nuevos estados agregados...
PROMPT

PROMPT Estados de ARTICULO:
SELECT constraint_name, search_condition
FROM user_constraints
WHERE table_name = 'ARTICULO'
  AND constraint_name LIKE '%ESTADO%';

PROMPT
PROMPT Estados de PRESTAMO:
SELECT constraint_name, search_condition
FROM user_constraints
WHERE table_name = 'PRESTAMO'
  AND constraint_name LIKE '%ESTADO%';

-- ----------------------------------------------------------------------------
-- PRUEBA 2: Verificar estructura actualizada
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 2: Verificando campos agregados a ARTICULO...
PROMPT

DESC ARTICULO;

-- ----------------------------------------------------------------------------
-- PRUEBA 3: Consultar prestamos candidatos a transferencia
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 3: Consultando prestamos candidatos a transferencia...
PROMPT

SELECT
    ID_PRESTAMO,
    SUBSTR(CLIENTE, 1, 25) AS CLIENTE,
    SUBSTR(TIPO_ARTICULO, 1, 15) AS TIPO,
    TO_CHAR(VALOR_TASADO, '$ 999,999,999') AS VALOR,
    DIAS_VENCIDO,
    ESTADO_TRANSFERENCIA
FROM V_PRESTAMOS_CANDIDATOS_TRANSFERENCIA
WHERE ROWNUM <= 10;

-- ----------------------------------------------------------------------------
-- PRUEBA 4: Crear prestamo de prueba vencido
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 4: Creando prestamo de prueba para transferencia...
PROMPT

DECLARE
    v_prestamo_id NUMBER;
    v_cliente_id NUMBER;
    v_asesor_id NUMBER;
    v_articulo_id NUMBER;
BEGIN
    -- Obtener IDs necesarios
    SELECT ID_PERSONA INTO v_cliente_id
    FROM PERSONA WHERE TIPO_PERSONA = 'CLIENTE' AND ROWNUM = 1;

    SELECT ID_PERSONA INTO v_asesor_id
    FROM PERSONA WHERE TIPO_PERSONA = 'ASESOR' AND ROWNUM = 1;

    SELECT ID_ARTICULO INTO v_articulo_id
    FROM ARTICULO WHERE ESTADO != 'DEFECTUOSO' AND ESTADO != 'PROPIEDAD_CASA' AND ROWNUM = 1;

    -- Obtener siguiente ID
    SELECT NVL(MAX(ID_PRESTAMO), 0) + 1 INTO v_prestamo_id FROM PRESTAMO;

    DBMS_OUTPUT.PUT_LINE('Creando prestamo de prueba ID: ' || v_prestamo_id);

    -- Crear prestamo vencido hace 35 dias
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
        2000000, -- 2 millones
        SYSDATE - 65, -- Hace 65 dias
        SYSDATE - 35, -- Vencido hace 35 dias
        0.10,
        'VENCIDO'
    );

    DBMS_OUTPUT.PUT_LINE('Prestamo creado con estado VENCIDO (35 dias vencido)');
    DBMS_OUTPUT.PUT_LINE('Articulo ID: ' || v_articulo_id);
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Ejecutando procedimiento de transferencia...');

    -- Ejecutar procedimiento (minimo 30 dias)
    SP_TRANSFERIR_ARTICULOS_VENCIDOS(30);

    -- Verificar transferencia
    DECLARE
        v_estado_articulo VARCHAR2(20);
        v_estado_prestamo VARCHAR2(30);
    BEGIN
        SELECT ESTADO INTO v_estado_articulo
        FROM ARTICULO WHERE ID_ARTICULO = v_articulo_id;

        SELECT ESTADO_PRESTAMO INTO v_estado_prestamo
        FROM PRESTAMO WHERE ID_PRESTAMO = v_prestamo_id;

        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('*** PRUEBA 4: VERIFICACION ***');
        DBMS_OUTPUT.PUT_LINE('Estado del articulo: ' || v_estado_articulo);
        DBMS_OUTPUT.PUT_LINE('Estado del prestamo: ' || v_estado_prestamo);

        IF v_estado_articulo = 'PROPIEDAD_CASA' AND v_estado_prestamo = 'ARTICULO_TRANSFERIDO' THEN
            DBMS_OUTPUT.PUT_LINE('*** PRUEBA 4: EXITOSA ***');
        ELSE
            DBMS_OUTPUT.PUT_LINE('*** PRUEBA 4: FALLIDA ***');
        END IF;
    END;

    -- Limpiar datos de prueba
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Datos de prueba eliminados (ROLLBACK)');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('*** PRUEBA 4: ERROR ***');
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
        ROLLBACK;
END;
/

-- ----------------------------------------------------------------------------
-- PRUEBA 5: Consultar inventario de articulos de la casa
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 5: Consultando inventario actual de articulos de la casa...
PROMPT

SELECT
    COUNT(*) AS TOTAL_ARTICULOS,
    TO_CHAR(SUM(VALOR_TASADO), '$ 999,999,999.99') AS VALOR_TOTAL_INVENTARIO
FROM V_ARTICULOS_CASA_EMPENO;

PROMPT
PROMPT Detalle de articulos (primeros 10):

SELECT
    ID_ARTICULO,
    SUBSTR(TIPO_ARTICULO, 1, 15) AS TIPO,
    TO_CHAR(VALOR_TASADO, '$ 999,999,999') AS VALOR,
    DIAS_EN_INVENTARIO,
    ESTADO_INVENTARIO
FROM V_ARTICULOS_CASA_EMPENO
WHERE ROWNUM <= 10;

-- ============================================================================
-- RESUMEN FINAL
-- ============================================================================

PROMPT
PROMPT ============================================================================
PROMPT INSTALACION DEL SISTEMA DE TRANSFERENCIA COMPLETADA
PROMPT ============================================================================
PROMPT
PROMPT Componentes instalados:
PROMPT   [+] Estado PROPIEDAD_CASA agregado a ARTICULO
PROMPT   [+] Estado ARTICULO_TRANSFERIDO agregado a PRESTAMO
PROMPT   [+] Campos de auditoria: ID_CLIENTE_ORIGINAL, FECHA_TRANSFERENCIA
PROMPT   [+] Procedimiento: SP_TRANSFERIR_ARTICULOS_VENCIDOS
PROMPT   [+] Vista: V_ARTICULOS_CASA_EMPENO
PROMPT   [+] Vista: V_PRESTAMOS_CANDIDATOS_TRANSFERENCIA
PROMPT
PROMPT Regla de negocio implementada:
PROMPT   "Si el cliente no paga la deuda en el plazo total,
PROMPT    el articulo pasa a ser propiedad de la casa de empeno"
PROMPT
PROMPT Criterio de transferencia:
PROMPT   - Prestamos en estado VENCIDO
PROMPT   - Con mas de 30 dias de vencimiento (configurable)
PROMPT   - Articulo aun no transferido
PROMPT
PROMPT Ejecucion manual:
PROMPT   EXECUTE SP_TRANSFERIR_ARTICULOS_VENCIDOS(30);
PROMPT
PROMPT Consultas utiles:
PROMPT   SELECT * FROM V_PRESTAMOS_CANDIDATOS_TRANSFERENCIA;
PROMPT   SELECT * FROM V_ARTICULOS_CASA_EMPENO;
PROMPT
PROMPT Estado de objetos:

SELECT object_name, object_type, status
FROM user_objects
WHERE object_name IN (
    'SP_TRANSFERIR_ARTICULOS_VENCIDOS',
    'V_ARTICULOS_CASA_EMPENO',
    'V_PRESTAMOS_CANDIDATOS_TRANSFERENCIA'
)
ORDER BY object_type, object_name;

PROMPT
PROMPT ============================================================================
PROMPT SCRIPT COMPLETADO
PROMPT ============================================================================

