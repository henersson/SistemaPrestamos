-- ============================================================================
-- TRIGGER: VALIDAR QUE ADMINISTRADOR NO PUEDA SER CLIENTE
-- Descripcion: Implementa la regla de negocio del universo del discurso:
--              "Un administrador no puede ser cliente"
--
-- Autor: Henersson Cobo
-- Proyecto: Sistema de Prestamos y Casa de Empeno
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
--
-- Eventos: BEFORE INSERT OR UPDATE ON CLIENTE
-- Nivel: FOR EACH ROW
-- ============================================================================

SET ECHO ON
SET SERVEROUTPUT ON SIZE UNLIMITED
SET LINESIZE 200

PROMPT ============================================================================
PROMPT CREACION DE TRIGGER - VALIDAR ADMIN NO PUEDE SER CLIENTE
PROMPT ============================================================================
PROMPT

-- ----------------------------------------------------------------------------
-- Crear trigger de validacion
-- ----------------------------------------------------------------------------

CREATE OR REPLACE TRIGGER TRG_VALIDAR_ADMIN_NO_CLIENTE
BEFORE INSERT OR UPDATE ON CLIENTE
FOR EACH ROW
DECLARE
    v_tipo_persona VARCHAR2(20);
    v_nombre_persona VARCHAR2(200);
BEGIN
    -- Consultar tipo de persona
    BEGIN
        SELECT TIPO_PERSONA, NOMBRE_PERSONA
        INTO v_tipo_persona, v_nombre_persona
        FROM PERSONA
        WHERE ID_PERSONA = :NEW.ID_PERSONA;

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20001,
                'ERROR: La persona con ID ' || :NEW.ID_PERSONA ||
                ' no existe en el sistema.' || CHR(10) ||
                'Por favor verifique el ID de la persona.');
    END;

    -- Validar que NO sea ADMINISTRADOR
    IF v_tipo_persona = 'ADMINISTRADOR' THEN
        RAISE_APPLICATION_ERROR(-20002,
            'ERROR: Un administrador NO puede ser registrado como cliente.' || CHR(10) ||
            'ID Persona: ' || :NEW.ID_PERSONA || CHR(10) ||
            'Nombre: ' || v_nombre_persona || CHR(10) ||
            'Tipo: ADMINISTRADOR' || CHR(10) ||
            'Razon: Segun el universo del discurso, un administrador no puede ser cliente.' || CHR(10) ||
            'Accion: Seleccione una persona de tipo CLIENTE o ASESOR.');
    END IF;

    -- Validar que sea un tipo valido para ser cliente
    IF v_tipo_persona NOT IN ('CLIENTE', 'ASESOR') THEN
        RAISE_APPLICATION_ERROR(-20003,
            'ERROR: Tipo de persona no valido para ser cliente.' || CHR(10) ||
            'ID Persona: ' || :NEW.ID_PERSONA || CHR(10) ||
            'Nombre: ' || v_nombre_persona || CHR(10) ||
            'Tipo actual: ' || v_tipo_persona || CHR(10) ||
            'Tipos permitidos: CLIENTE, ASESOR' || CHR(10) ||
            'Nota: Los asesores pueden ser clientes, pero los administradores NO.');
    END IF;

    -- Log de validacion exitosa
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('Validacion EXITOSA');
    DBMS_OUTPUT.PUT_LINE('ID Persona: ' || :NEW.ID_PERSONA);
    DBMS_OUTPUT.PUT_LINE('Nombre: ' || v_nombre_persona);
    DBMS_OUTPUT.PUT_LINE('Tipo: ' || v_tipo_persona);
    DBMS_OUTPUT.PUT_LINE('Accion: Registro como cliente PERMITIDO');
    DBMS_OUTPUT.PUT_LINE('========================================');

EXCEPTION
    WHEN OTHERS THEN
        -- Re-lanzar errores personalizados
        IF SQLCODE BETWEEN -20999 AND -20001 THEN
            RAISE;
        ELSE
            -- Error inesperado
            RAISE_APPLICATION_ERROR(-20099,
                'ERROR INESPERADO al validar tipo de persona: ' || SQLERRM);
        END IF;
END TRG_VALIDAR_ADMIN_NO_CLIENTE;
/

-- Agregar comentario de documentacion
COMMENT ON TRIGGER TRG_VALIDAR_ADMIN_NO_CLIENTE IS
'Valida que un administrador no pueda ser registrado como cliente.
Regla de negocio: "Un administrador no puede ser cliente"
Tipos permitidos: CLIENTE, ASESOR
Tipo NO permitido: ADMINISTRADOR
Se ejecuta antes de INSERT o UPDATE en CLIENTE.
Autor: Henersson Cobo';

PROMPT Trigger TRG_VALIDAR_ADMIN_NO_CLIENTE creado exitosamente
PROMPT

-- ============================================================================
-- PRUEBAS DEL TRIGGER
-- ============================================================================

PROMPT ============================================================================
PROMPT EJECUTANDO PRUEBAS DEL TRIGGER
PROMPT ============================================================================
PROMPT

-- ----------------------------------------------------------------------------
-- PRUEBA 1: Verificar tipos de personas en el sistema
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 1: Verificando tipos de personas disponibles...
PROMPT

SELECT
    TIPO_PERSONA,
    COUNT(*) AS CANTIDAD,
    LISTAGG(NOMBRE_PERSONA, ', ') WITHIN GROUP (ORDER BY NOMBRE_PERSONA) AS EJEMPLOS
FROM (
    SELECT TIPO_PERSONA, NOMBRE_PERSONA
    FROM PERSONA
    WHERE ROWNUM <= 3
)
GROUP BY TIPO_PERSONA
ORDER BY TIPO_PERSONA;

-- ----------------------------------------------------------------------------
-- PRUEBA 2: Intentar registrar un ASESOR como cliente (DEBE FUNCIONAR)
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 2: Intentando registrar un ASESOR como cliente...
PROMPT Esta operacion DEBE FUNCIONAR
PROMPT

DECLARE
    v_id_asesor NUMBER;
    v_nombre_asesor VARCHAR2(200);
    v_calificacion NUMBER := 7.5;
BEGIN
    -- Buscar un asesor que NO sea cliente aun
    BEGIN
        SELECT p.ID_PERSONA, p.NOMBRE_PERSONA
        INTO v_id_asesor, v_nombre_asesor
        FROM PERSONA p
        WHERE p.TIPO_PERSONA = 'ASESOR'
          AND NOT EXISTS (
              SELECT 1 FROM CLIENTE c WHERE c.ID_PERSONA = p.ID_PERSONA
          )
          AND ROWNUM = 1;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('*** PRUEBA 2: NO EJECUTADA ***');
            DBMS_OUTPUT.PUT_LINE('No hay asesores disponibles para probar (todos ya son clientes)');
            RETURN;
    END;

    DBMS_OUTPUT.PUT_LINE('Asesor seleccionado:');
    DBMS_OUTPUT.PUT_LINE('  ID: ' || v_id_asesor);
    DBMS_OUTPUT.PUT_LINE('  Nombre: ' || v_nombre_asesor);
    DBMS_OUTPUT.PUT_LINE('');

    -- Intentar registrar como cliente (DEBE FUNCIONAR)
    INSERT INTO CLIENTE (
        ID_PERSONA,
        CALIFICACION,
        ACTIVO
    ) VALUES (
        v_id_asesor,
        v_calificacion,
        'S'
    );

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('*** PRUEBA 2: EXITOSA ***');
    DBMS_OUTPUT.PUT_LINE('El asesor fue registrado como cliente exitosamente');
    DBMS_OUTPUT.PUT_LINE('El trigger permitio la operacion correctamente');

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
-- PRUEBA 3: Intentar registrar un ADMINISTRADOR como cliente (DEBE FALLAR)
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 3: Intentando registrar un ADMINISTRADOR como cliente...
PROMPT Esta operacion DEBE SER RECHAZADA por el trigger
PROMPT

DECLARE
    v_id_admin NUMBER;
    v_nombre_admin VARCHAR2(200);
    v_calificacion NUMBER := 8.0;
BEGIN
    -- Buscar un administrador
    BEGIN
        SELECT ID_PERSONA, NOMBRE_PERSONA
        INTO v_id_admin, v_nombre_admin
        FROM PERSONA
        WHERE TIPO_PERSONA = 'ADMINISTRADOR'
          AND ROWNUM = 1;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('*** PRUEBA 3: NO EJECUTADA ***');
            DBMS_OUTPUT.PUT_LINE('No hay administradores en el sistema para probar');
            RETURN;
    END;

    DBMS_OUTPUT.PUT_LINE('Administrador seleccionado:');
    DBMS_OUTPUT.PUT_LINE('  ID: ' || v_id_admin);
    DBMS_OUTPUT.PUT_LINE('  Nombre: ' || v_nombre_admin);
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Intentando registrar como cliente...');

    -- Intentar registrar como cliente (DEBE FALLAR)
    INSERT INTO CLIENTE (
        ID_PERSONA,
        CALIFICACION,
        ACTIVO
    ) VALUES (
        v_id_admin,
        v_calificacion,
        'S'
    );

    -- Si llega aqui, el trigger NO funciono
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('*** PRUEBA 3: FALLIDA ***');
    DBMS_OUTPUT.PUT_LINE('ERROR: El trigger NO rechazo al administrador!');
    DBMS_OUTPUT.PUT_LINE('El administrador NO deberia haberse registrado como cliente');

    ROLLBACK;

EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20002 THEN
            -- Error esperado del trigger
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('*** PRUEBA 3: EXITOSA ***');
            DBMS_OUTPUT.PUT_LINE('El trigger rechazo correctamente al administrador');
            DBMS_OUTPUT.PUT_LINE('');
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
-- PRUEBA 4: Crear persona de prueba ADMINISTRADOR e intentar registrar
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 4: Creando persona de prueba ADMINISTRADOR...
PROMPT

DECLARE
    v_id_persona_test NUMBER;
    v_calificacion NUMBER := 9.0;
BEGIN
    -- Crear persona de prueba tipo ADMINISTRADOR
    SELECT NVL(MAX(ID_PERSONA), 0) + 1 INTO v_id_persona_test FROM PERSONA;

    INSERT INTO PERSONA (
        ID_PERSONA,
        TIPO_PERSONA,
        NOMBRE_PERSONA,
        TIPO_ID,
        NUM_ID
    ) VALUES (
        v_id_persona_test,
        'ADMINISTRADOR',
        'Admin Test - NO debe ser cliente',
        'CC',
        '9999999999'
    );

    DBMS_OUTPUT.PUT_LINE('Persona de prueba creada:');
    DBMS_OUTPUT.PUT_LINE('  ID: ' || v_id_persona_test);
    DBMS_OUTPUT.PUT_LINE('  Tipo: ADMINISTRADOR');
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Intentando registrar como cliente...');

    -- Intentar registrar como cliente (DEBE FALLAR)
    INSERT INTO CLIENTE (
        ID_PERSONA,
        CALIFICACION,
        ACTIVO
    ) VALUES (
        v_id_persona_test,
        v_calificacion,
        'S'
    );

    -- Si llega aqui, fallo la validacion
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('*** PRUEBA 4: FALLIDA ***');
    DBMS_OUTPUT.PUT_LINE('El trigger deberia haber rechazado al administrador');

    ROLLBACK;

EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20002 THEN
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('*** PRUEBA 4: EXITOSA ***');
            DBMS_OUTPUT.PUT_LINE('El trigger funciona correctamente');
            DBMS_OUTPUT.PUT_LINE('Administrador rechazado como se esperaba');
        ELSE
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('*** PRUEBA 4: ERROR ***');
            DBMS_OUTPUT.PUT_LINE(SQLERRM);
        END IF;
        ROLLBACK; -- Limpiar datos de prueba
END;
/

-- ----------------------------------------------------------------------------
-- PRUEBA 5: Verificar clientes existentes y sus tipos
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 5: Verificando clientes actuales y sus tipos de persona...
PROMPT

SELECT
    c.ID_PERSONA,
    p.NOMBRE_PERSONA,
    p.TIPO_PERSONA,
    c.CALIFICACION,
    c.ACTIVO,
    CASE
        WHEN p.TIPO_PERSONA = 'ADMINISTRADOR' THEN 'INVALIDO!'
        WHEN p.TIPO_PERSONA IN ('CLIENTE', 'ASESOR') THEN 'Valido'
        ELSE 'Revisar'
    END AS VALIDACION
FROM CLIENTE c
JOIN PERSONA p ON c.ID_PERSONA = p.ID_PERSONA
ORDER BY p.TIPO_PERSONA, c.ID_PERSONA;

-- ============================================================================
-- RESUMEN DE PRUEBAS
-- ============================================================================

PROMPT
PROMPT ============================================================================
PROMPT RESUMEN DE PRUEBAS DEL TRIGGER
PROMPT ============================================================================
PROMPT
PROMPT El trigger TRG_VALIDAR_ADMIN_NO_CLIENTE implementa las siguientes reglas:
PROMPT
PROMPT   [1] Valida que la persona exista en el sistema
PROMPT   [2] RECHAZA si el tipo es ADMINISTRADOR (codigo error -20002)
PROMPT   [3] PERMITE si el tipo es CLIENTE o ASESOR
PROMPT   [4] Valida tipos permitidos
PROMPT   [5] Proporciona mensajes de error claros y detallados
PROMPT
PROMPT Regla de negocio:
PROMPT   "Un administrador NO puede ser cliente"
PROMPT
PROMPT Tipos permitidos como cliente:
PROMPT   - CLIENTE (tipo natural)
PROMPT   - ASESOR (puede ser cliente tambien)
PROMPT
PROMPT Tipo NO permitido como cliente:
PROMPT   - ADMINISTRADOR (rechazado por regla de negocio)
PROMPT
PROMPT Estado del trigger:

SELECT trigger_name, status, trigger_type, triggering_event
FROM user_triggers
WHERE trigger_name = 'TRG_VALIDAR_ADMIN_NO_CLIENTE';

-- ============================================================================
-- CASOS DE USO Y EJEMPLOS
-- ============================================================================

PROMPT
PROMPT ============================================================================
PROMPT CASOS DE USO
PROMPT ============================================================================
PROMPT
PROMPT Caso 1: Registrar persona tipo CLIENTE como cliente
PROMPT   Resultado: PERMITIDO (tipo natural)
PROMPT
PROMPT Caso 2: Registrar persona tipo ASESOR como cliente
PROMPT   Resultado: PERMITIDO (asesores pueden ser clientes)
PROMPT
PROMPT Caso 3: Registrar persona tipo ADMINISTRADOR como cliente
PROMPT   Resultado: RECHAZADO (administradores NO pueden ser clientes)
PROMPT   Error: -20002
PROMPT
PROMPT ============================================================================

PROMPT
PROMPT ============================================================================
PROMPT SCRIPT COMPLETADO
PROMPT ============================================================================

