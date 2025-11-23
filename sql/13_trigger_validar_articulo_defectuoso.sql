    WHERE TIPO_PERSONA = 'ASESOR'
      AND ROWNUM = 1;

    -- Obtener siguiente ID
    SELECT NVL(MAX(ID_PRESTAMO), 0) + 1 INTO v_prestamo_id FROM PRESTAMO;

    -- Intentar insertar (DEBE FUNCIONAR)
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
        v_id_asesor,
        v_id_cliente,
        v_id_articulo_optimo,
        500000,
        SYSDATE,
        ADD_MONTHS(SYSDATE, 3),
        0.10,
        'ACTIVO'
    );

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('*** PRUEBA 2: EXITOSA ***');
    DBMS_OUTPUT.PUT_LINE('Prestamo ' || v_prestamo_id || ' creado con articulo ' || v_id_articulo_optimo);
    DBMS_OUTPUT.PUT_LINE('El trigger permitio el prestamo con articulo OPTIMO');

    ROLLBACK; -- Deshacer para no afectar datos

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('*** PRUEBA 2: FALLIDA (no deberia fallar) ***');
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
        ROLLBACK;
END;
/

-- ----------------------------------------------------------------------------
-- PRUEBA 3: Intentar insertar prestamo con articulo DEFECTUOSO (debe fallar)
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 3: Intentando insertar prestamo con articulo DEFECTUOSO...
PROMPT Esta operacion DEBE SER RECHAZADA por el trigger
PROMPT

DECLARE
    v_id_articulo_defectuoso NUMBER;
    v_id_cliente NUMBER;
    v_id_asesor NUMBER;
    v_prestamo_id NUMBER;
BEGIN
    -- Intentar obtener un articulo defectuoso
    BEGIN
        SELECT ID_ARTICULO INTO v_id_articulo_defectuoso
        FROM ARTICULO
        WHERE ESTADO = 'DEFECTUOSO'
          AND ROWNUM = 1;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('*** PRUEBA 3: NO EJECUTADA ***');
            DBMS_OUTPUT.PUT_LINE('No hay articulos defectuosos en el sistema para probar');
            RETURN;
    END;

    -- Obtener cliente y asesor
    SELECT ID_PERSONA INTO v_id_cliente
    FROM PERSONA
    WHERE TIPO_PERSONA = 'CLIENTE'
      AND ROWNUM = 1;

    SELECT ID_PERSONA INTO v_id_asesor
    FROM PERSONA
    WHERE TIPO_PERSONA = 'ASESOR'
      AND ROWNUM = 1;

    -- Obtener siguiente ID
    SELECT NVL(MAX(ID_PRESTAMO), 0) + 1 INTO v_prestamo_id FROM PRESTAMO;

    -- Intentar insertar (DEBE FALLAR)
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
        v_id_asesor,
        v_id_cliente,
        v_id_articulo_defectuoso,
        500000,
        SYSDATE,
        ADD_MONTHS(SYSDATE, 3),
        0.10,
        'ACTIVO'
    );

    -- Si llega aqui, el trigger NO funciono
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('*** PRUEBA 3: FALLIDA ***');
    DBMS_OUTPUT.PUT_LINE('ERROR: El trigger NO rechazo el articulo defectuoso!');
    DBMS_OUTPUT.PUT_LINE('El prestamo NO deberia haberse creado');

    ROLLBACK;

EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20002 THEN
            -- Error esperado del trigger
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('*** PRUEBA 3: EXITOSA ***');
            DBMS_OUTPUT.PUT_LINE('El trigger rechazo correctamente el articulo defectuoso');
            DBMS_OUTPUT.PUT_LINE('Mensaje del trigger:');
            DBMS_OUTPUT.PUT_LINE(SQLERRM);
        ELSE
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('*** PRUEBA 3: ERROR INESPERADO ***');
            DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
        END IF;
        ROLLBACK;
END;
/

-- ----------------------------------------------------------------------------
-- PRUEBA 4: Crear articulo de prueba DEFECTUOSO y probar
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 4: Creando articulo de prueba DEFECTUOSO...
PROMPT

DECLARE
    v_id_articulo_test NUMBER;
    v_id_cliente NUMBER;
    v_id_asesor NUMBER;
    v_prestamo_id NUMBER;
BEGIN
    -- Crear articulo de prueba DEFECTUOSO
    SELECT NVL(MAX(ID_ARTICULO), 0) + 1 INTO v_id_articulo_test FROM ARTICULO;

    INSERT INTO ARTICULO (
        ID_ARTICULO,
        TIPO_ARTICULO,
        DESCRIPCION_ARTICULO,
        VALOR_TASADO,
        ESTADO
    ) VALUES (
        v_id_articulo_test,
        'ELECTRONICO',
        'Laptop de prueba - DEFECTUOSA para testing',
        100000,
        'DEFECTUOSO'
    );

    DBMS_OUTPUT.PUT_LINE('Articulo de prueba ' || v_id_articulo_test || ' creado (DEFECTUOSO)');

    -- Obtener cliente y asesor
    SELECT ID_PERSONA INTO v_id_cliente
    FROM PERSONA
    WHERE TIPO_PERSONA = 'CLIENTE'
      AND ROWNUM = 1;

    SELECT ID_PERSONA INTO v_id_asesor
    FROM PERSONA
    WHERE TIPO_PERSONA = 'ASESOR'
      AND ROWNUM = 1;

    -- Obtener siguiente ID de prestamo
    SELECT NVL(MAX(ID_PRESTAMO), 0) + 1 INTO v_prestamo_id FROM PRESTAMO;

    DBMS_OUTPUT.PUT_LINE('Intentando crear prestamo con articulo defectuoso...');

    -- Intentar crear prestamo (DEBE FALLAR)
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
        v_id_asesor,
        v_id_cliente,
        v_id_articulo_test,
        750000,
        SYSDATE,
        ADD_MONTHS(SYSDATE, 4),
        0.10,
        'ACTIVO'
    );

    -- Si llega aqui, fallo la validacion
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('*** PRUEBA 4: FALLIDA ***');
    DBMS_OUTPUT.PUT_LINE('El trigger deberia haber rechazado el articulo defectuoso');

    ROLLBACK;

EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20002 THEN
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('*** PRUEBA 4: EXITOSA ***');
            DBMS_OUTPUT.PUT_LINE('El trigger funciona correctamente');
            DBMS_OUTPUT.PUT_LINE('Articulo defectuoso rechazado como se esperaba');
        ELSE
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('*** PRUEBA 4: ERROR ***');
            DBMS_OUTPUT.PUT_LINE(SQLERRM);
        END IF;
        ROLLBACK; -- Limpiar datos de prueba
END;
/

-- ============================================================================
-- RESUMEN DE PRUEBAS
-- ============================================================================

PROMPT
PROMPT ============================================================================
PROMPT RESUMEN DE PRUEBAS DEL TRIGGER
PROMPT ============================================================================
PROMPT
PROMPT El trigger TRG_VALIDAR_ARTICULO_PRESTAMO implementa las siguientes reglas:
PROMPT
PROMPT   [1] Valida que el articulo exista en el sistema
PROMPT   [2] Rechaza articulos en estado DEFECTUOSO
PROMPT   [3] Previene uso del mismo articulo en multiples prestamos activos
PROMPT   [4] Proporciona mensajes de error claros y detallados
PROMPT
PROMPT Estado del trigger:
SELECT trigger_name, status, trigger_type
FROM user_triggers
WHERE trigger_name = 'TRG_VALIDAR_ARTICULO_PRESTAMO';

PROMPT
PROMPT ============================================================================
PROMPT SCRIPT COMPLETADO
PROMPT ============================================================================
-- ============================================================================
-- TRIGGER: VALIDAR ARTICULOS DEFECTUOSOS EN PRESTAMOS
-- Descripcion: Implementa la regla de negocio del universo del discurso:
--              "Los articulos defectuosos no se aceptan como garantia"
--
-- Autor: Henersson Cobo
-- Proyecto: Sistema de Prestamos y Casa de Empeno
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
--
-- Eventos: BEFORE INSERT OR UPDATE ON PRESTAMO
-- Nivel: FOR EACH ROW
-- ============================================================================

SET ECHO ON
SET SERVEROUTPUT ON SIZE UNLIMITED
SET LINESIZE 200

PROMPT ============================================================================
PROMPT CREACION DE TRIGGER - VALIDAR ARTICULOS DEFECTUOSOS
PROMPT ============================================================================
PROMPT

-- ----------------------------------------------------------------------------
-- Crear trigger de validacion
-- ----------------------------------------------------------------------------

CREATE OR REPLACE TRIGGER TRG_VALIDAR_ARTICULO_PRESTAMO
BEFORE INSERT OR UPDATE OF ID_ARTICULO ON PRESTAMO
FOR EACH ROW
DECLARE
    v_estado_articulo VARCHAR2(20);
    v_tipo_articulo VARCHAR2(50);
    v_descripcion VARCHAR2(200);
BEGIN
    -- Consultar informacion del articulo
    BEGIN
        SELECT ESTADO, TIPO_ARTICULO, DESCRIPCION_ARTICULO
        INTO v_estado_articulo, v_tipo_articulo, v_descripcion
        FROM ARTICULO
        WHERE ID_ARTICULO = :NEW.ID_ARTICULO;

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20001,
                'ERROR: El articulo ID ' || :NEW.ID_ARTICULO ||
                ' no existe en el sistema. Por favor verifique el ID del articulo.');
    END;

    -- Validar que el articulo NO sea DEFECTUOSO
    IF v_estado_articulo = 'DEFECTUOSO' THEN
        RAISE_APPLICATION_ERROR(-20002,
            'ERROR: Articulo NO ACEPTADO como garantia.' || CHR(10) ||
            'Razon: Los articulos defectuosos no se aceptan como garantia.' || CHR(10) ||
            'ID Articulo: ' || :NEW.ID_ARTICULO || CHR(10) ||
            'Tipo: ' || v_tipo_articulo || CHR(10) ||
            'Descripcion: ' || v_descripcion || CHR(10) ||
            'Estado: DEFECTUOSO' || CHR(10) ||
            'Accion: Seleccione un articulo en estado OPTIMO o BUENO.');
    END IF;

    -- Validar que el articulo no este ya asociado a otro prestamo activo
    DECLARE
        v_prestamo_existente NUMBER;
    BEGIN
        SELECT ID_PRESTAMO INTO v_prestamo_existente
        FROM PRESTAMO
        WHERE ID_ARTICULO = :NEW.ID_ARTICULO
          AND ESTADO_PRESTAMO IN ('ACTIVO', 'EN_MORA')
          AND ID_PRESTAMO != NVL(:NEW.ID_PRESTAMO, -1)
          AND ROWNUM = 1;

        -- Si llega aqui, el articulo ya esta en uso
        RAISE_APPLICATION_ERROR(-20003,
            'ERROR: Articulo ya asociado a otro prestamo.' || CHR(10) ||
            'ID Articulo: ' || :NEW.ID_ARTICULO || CHR(10) ||
            'Prestamo Activo: ' || v_prestamo_existente || CHR(10) ||
            'Accion: Seleccione otro articulo disponible.');

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            -- Articulo disponible, continuar
            NULL;
    END;

    -- Registrar validacion exitosa en log
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('Validacion de articulo EXITOSA');
    DBMS_OUTPUT.PUT_LINE('ID Articulo: ' || :NEW.ID_ARTICULO);
    DBMS_OUTPUT.PUT_LINE('Tipo: ' || v_tipo_articulo);
    DBMS_OUTPUT.PUT_LINE('Estado: ' || v_estado_articulo);
    DBMS_OUTPUT.PUT_LINE('Prestamo: ' || NVL(TO_CHAR(:NEW.ID_PRESTAMO), 'NUEVO'));
    DBMS_OUTPUT.PUT_LINE('========================================');

EXCEPTION
    WHEN OTHERS THEN
        -- Re-lanzar errores personalizados
        IF SQLCODE BETWEEN -20999 AND -20001 THEN
            RAISE;
        ELSE
            -- Error inesperado
            RAISE_APPLICATION_ERROR(-20099,
                'ERROR INESPERADO al validar articulo: ' || SQLERRM);
        END IF;
END TRG_VALIDAR_ARTICULO_PRESTAMO;
/

-- Agregar comentario de documentacion
COMMENT ON TRIGGER TRG_VALIDAR_ARTICULO_PRESTAMO IS
'Valida que los articulos asociados a prestamos cumplan las reglas de negocio:
1. El articulo debe existir
2. El articulo NO debe estar en estado DEFECTUOSO
3. El articulo no debe estar asociado a otro prestamo activo
Se ejecuta antes de INSERT o UPDATE en PRESTAMO.
Autor: Henersson Cobo';

PROMPT Trigger TRG_VALIDAR_ARTICULO_PRESTAMO creado exitosamente
PROMPT

-- ============================================================================
-- PRUEBAS DEL TRIGGER
-- ============================================================================

PROMPT ============================================================================
PROMPT EJECUTANDO PRUEBAS DEL TRIGGER
PROMPT ============================================================================
PROMPT

-- ----------------------------------------------------------------------------
-- PRUEBA 1: Verificar articulos disponibles
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 1: Verificando articulos disponibles en el sistema...
PROMPT

SELECT
    ID_ARTICULO,
    TIPO_ARTICULO,
    ESTADO,
    VALOR_TASADO,
    CASE
        WHEN ESTADO = 'DEFECTUOSO' THEN 'NO ACEPTADO'
        ELSE 'ACEPTADO'
    END AS VALIDACION
FROM ARTICULO
ORDER BY ESTADO, ID_ARTICULO;

-- ----------------------------------------------------------------------------
-- PRUEBA 2: Intentar insertar prestamo con articulo OPTIMO (debe funcionar)
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 2: Insertando prestamo con articulo en estado OPTIMO...
PROMPT

DECLARE
    v_id_articulo_optimo NUMBER;
    v_id_cliente NUMBER;
    v_id_asesor NUMBER;
    v_prestamo_id NUMBER;
BEGIN
    -- Buscar un articulo optimo
    SELECT ID_ARTICULO INTO v_id_articulo_optimo
    FROM ARTICULO
    WHERE ESTADO = 'OPTIMO'
      AND ROWNUM = 1;

    -- Obtener cliente y asesor
    SELECT ID_PERSONA INTO v_id_cliente
    FROM PERSONA
    WHERE TIPO_PERSONA = 'CLIENTE'
      AND ROWNUM = 1;

    SELECT ID_PERSONA INTO v_id_asesor
    FROM PERSONA

