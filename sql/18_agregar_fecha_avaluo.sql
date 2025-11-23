-- ============================================================================
-- AGREGAR CAMPOS DE AVALUO Y PRECIO DE MERCADO
-- Descripcion: Implementa campos faltantes segun el universo del discurso:
--              "valor tasado, fecha de avaluo" y
--              "valor se calcula segun precio de mercado vigente"
--
-- Autor: Henersson Cobo
-- Proyecto: Sistema de Prestamos y Casa de Empeno
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
--
-- CAMBIOS:
--   - FECHA_AVALUO (DATE, default SYSDATE)
--   - PRECIO_MERCADO_BASE (NUMBER)
--   - Comentarios de documentacion
--   - Migracion de datos existentes
-- ============================================================================

SET ECHO ON
SET SERVEROUTPUT ON SIZE UNLIMITED
SET LINESIZE 200
SET PAGESIZE 100

PROMPT ============================================================================
PROMPT AGREGAR CAMPOS DE AVALUO Y PRECIO DE MERCADO A ARTICULO
PROMPT ============================================================================
PROMPT

-- ============================================================================
-- PASO 1: VERIFICAR ESTRUCTURA ACTUAL
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 1: Verificando estructura actual de ARTICULO...
PROMPT ----------------------------------------------------------------------------
PROMPT

SELECT COUNT(*) AS "Total Articulos Existentes" FROM ARTICULO;

PROMPT
PROMPT Estructura actual:
DESC ARTICULO;

-- ============================================================================
-- PASO 2: AGREGAR CAMPO FECHA_AVALUO
-- ============================================================================

PROMPT
PROMPT ----------------------------------------------------------------------------
PROMPT Paso 2: Agregando campo FECHA_AVALUO...
PROMPT ----------------------------------------------------------------------------
PROMPT

-- Agregar columna FECHA_AVALUO con default SYSDATE
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE ARTICULO ADD (FECHA_AVALUO DATE DEFAULT SYSDATE)';
    DBMS_OUTPUT.PUT_LINE('Campo FECHA_AVALUO agregado exitosamente');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -1430 THEN
            DBMS_OUTPUT.PUT_LINE('Campo FECHA_AVALUO ya existe');
        ELSE
            DBMS_OUTPUT.PUT_LINE('Error al agregar FECHA_AVALUO: ' || SQLERRM);
            RAISE;
        END IF;
END;
/

-- Agregar comentario
COMMENT ON COLUMN ARTICULO.FECHA_AVALUO IS
'Fecha en que se realizo el avaluo del articulo.
Permite rastrear la vigencia de la tasacion.
Default: SYSDATE al momento de registro.';

PROMPT Columna FECHA_AVALUO configurada
PROMPT

-- ============================================================================
-- PASO 3: AGREGAR CAMPO PRECIO_MERCADO_BASE
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 3: Agregando campo PRECIO_MERCADO_BASE...
PROMPT ----------------------------------------------------------------------------
PROMPT

-- Agregar columna PRECIO_MERCADO_BASE
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE ARTICULO ADD (PRECIO_MERCADO_BASE NUMBER(12,2))';
    DBMS_OUTPUT.PUT_LINE('Campo PRECIO_MERCADO_BASE agregado exitosamente');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -1430 THEN
            DBMS_OUTPUT.PUT_LINE('Campo PRECIO_MERCADO_BASE ya existe');
        ELSE
            DBMS_OUTPUT.PUT_LINE('Error al agregar PRECIO_MERCADO_BASE: ' || SQLERRM);
            RAISE;
        END IF;
END;
/

-- Agregar comentario
COMMENT ON COLUMN ARTICULO.PRECIO_MERCADO_BASE IS
'Precio de mercado vigente del articulo al momento del avaluo.
El VALOR_TASADO se calcula como un porcentaje de este precio de mercado.
Permite ajustar tasaciones segun condiciones del mercado.';

PROMPT Columna PRECIO_MERCADO_BASE configurada
PROMPT

-- ============================================================================
-- PASO 4: AGREGAR CAMPO PORCENTAJE_TASACION
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 4: Agregando campo PORCENTAJE_TASACION (calculo)...
PROMPT ----------------------------------------------------------------------------
PROMPT

-- Agregar columna para almacenar el porcentaje aplicado
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE ARTICULO ADD (PORCENTAJE_TASACION NUMBER(5,2) DEFAULT 70)';
    DBMS_OUTPUT.PUT_LINE('Campo PORCENTAJE_TASACION agregado exitosamente');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -1430 THEN
            DBMS_OUTPUT.PUT_LINE('Campo PORCENTAJE_TASACION ya existe');
        ELSE
            DBMS_OUTPUT.PUT_LINE('Error al agregar PORCENTAJE_TASACION: ' || SQLERRM);
            RAISE;
        END IF;
END;
/

-- Agregar comentario
COMMENT ON COLUMN ARTICULO.PORCENTAJE_TASACION IS
'Porcentaje del precio de mercado aplicado para calcular el valor tasado.
Tipicamente entre 60-80% segun estado del articulo.
Formula: VALOR_TASADO = PRECIO_MERCADO_BASE * (PORCENTAJE_TASACION / 100)
Default: 70%';

PROMPT Columna PORCENTAJE_TASACION configurada
PROMPT

-- ============================================================================
-- PASO 5: MIGRAR DATOS EXISTENTES
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 5: Migrando datos existentes...
PROMPT ----------------------------------------------------------------------------
PROMPT

DECLARE
    v_total_articulos NUMBER := 0;
    v_articulos_actualizados NUMBER := 0;
    v_precio_mercado NUMBER;
    v_porcentaje NUMBER;

    CURSOR cur_articulos IS
        SELECT ID_ARTICULO, VALOR_TASADO, ESTADO, FECHA_AVALUO, PRECIO_MERCADO_BASE
        FROM ARTICULO;

BEGIN
    DBMS_OUTPUT.PUT_LINE('Iniciando migracion de datos...');
    DBMS_OUTPUT.PUT_LINE('');

    FOR articulo_rec IN cur_articulos LOOP
        v_total_articulos := v_total_articulos + 1;

        -- Si no tiene fecha de avaluo, asignar fecha actual
        IF articulo_rec.FECHA_AVALUO IS NULL THEN
            UPDATE ARTICULO
            SET FECHA_AVALUO = SYSDATE
            WHERE ID_ARTICULO = articulo_rec.ID_ARTICULO;
        END IF;

        -- Si no tiene precio de mercado, calcularlo desde el valor tasado
        IF articulo_rec.PRECIO_MERCADO_BASE IS NULL AND articulo_rec.VALOR_TASADO IS NOT NULL THEN

            -- Determinar porcentaje segun estado
            CASE articulo_rec.ESTADO
                WHEN 'OPTIMO' THEN v_porcentaje := 80;
                WHEN 'BUENO' THEN v_porcentaje := 70;
                WHEN 'REGULAR' THEN v_porcentaje := 60;
                WHEN 'DEFECTUOSO' THEN v_porcentaje := 40;
                WHEN 'PROPIEDAD_CASA' THEN v_porcentaje := 70;
                ELSE v_porcentaje := 70;
            END CASE;

            -- Calcular precio de mercado base (inverso)
            -- VALOR_TASADO = PRECIO_MERCADO * (PORCENTAJE / 100)
            -- PRECIO_MERCADO = VALOR_TASADO / (PORCENTAJE / 100)
            v_precio_mercado := ROUND(articulo_rec.VALOR_TASADO / (v_porcentaje / 100), 2);

            UPDATE ARTICULO
            SET PRECIO_MERCADO_BASE = v_precio_mercado,
                PORCENTAJE_TASACION = v_porcentaje
            WHERE ID_ARTICULO = articulo_rec.ID_ARTICULO;

            v_articulos_actualizados := v_articulos_actualizados + 1;
        END IF;

    END LOOP;

    COMMIT;

    DBMS_OUTPUT.PUT_LINE('Migracion completada:');
    DBMS_OUTPUT.PUT_LINE('  Total articulos: ' || v_total_articulos);
    DBMS_OUTPUT.PUT_LINE('  Articulos actualizados: ' || v_articulos_actualizados);
    DBMS_OUTPUT.PUT_LINE('');

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('ERROR en migracion: ' || SQLERRM);
        RAISE;
END;
/

-- ============================================================================
-- PASO 6: CREAR FUNCION DE CALCULO DE VALOR TASADO
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 6: Creando funcion FN_CALCULAR_VALOR_TASADO...
PROMPT ----------------------------------------------------------------------------
PROMPT

CREATE OR REPLACE FUNCTION FN_CALCULAR_VALOR_TASADO(
    p_precio_mercado NUMBER,
    p_estado_articulo VARCHAR2
) RETURN NUMBER
IS
    v_porcentaje NUMBER;
    v_valor_tasado NUMBER;
BEGIN
    -- Determinar porcentaje segun estado del articulo
    CASE p_estado_articulo
        WHEN 'OPTIMO' THEN v_porcentaje := 80;      -- 80% del precio de mercado
        WHEN 'BUENO' THEN v_porcentaje := 70;       -- 70% del precio de mercado
        WHEN 'REGULAR' THEN v_porcentaje := 60;     -- 60% del precio de mercado
        WHEN 'DEFECTUOSO' THEN v_porcentaje := 40;  -- 40% del precio de mercado
        WHEN 'PROPIEDAD_CASA' THEN v_porcentaje := 70;
        ELSE v_porcentaje := 70; -- Default 70%
    END CASE;

    -- Calcular valor tasado
    v_valor_tasado := p_precio_mercado * (v_porcentaje / 100);

    RETURN ROUND(v_valor_tasado, 2);

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error en FN_CALCULAR_VALOR_TASADO: ' || SQLERRM);
        RETURN 0;
END FN_CALCULAR_VALOR_TASADO;
/

COMMENT ON FUNCTION FN_CALCULAR_VALOR_TASADO IS
'Calcula el valor tasado de un articulo segun su precio de mercado y estado.
Porcentajes: OPTIMO=80%, BUENO=70%, REGULAR=60%, DEFECTUOSO=40%
Parametros: precio_mercado, estado_articulo
Retorna: valor_tasado calculado
Autor: Henersson Cobo';

PROMPT Funcion FN_CALCULAR_VALOR_TASADO creada exitosamente
PROMPT

-- ============================================================================
-- PASO 7: CREAR TRIGGER DE CALCULO AUTOMATICO
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 7: Creando trigger TRG_CALCULAR_VALOR_TASADO...
PROMPT ----------------------------------------------------------------------------
PROMPT

CREATE OR REPLACE TRIGGER TRG_CALCULAR_VALOR_TASADO
BEFORE INSERT OR UPDATE OF PRECIO_MERCADO_BASE, ESTADO ON ARTICULO
FOR EACH ROW
BEGIN
    -- Si se proporciona precio de mercado, calcular valor tasado automaticamente
    IF :NEW.PRECIO_MERCADO_BASE IS NOT NULL THEN

        -- Determinar porcentaje segun estado
        IF :NEW.ESTADO = 'OPTIMO' THEN
            :NEW.PORCENTAJE_TASACION := 80;
        ELSIF :NEW.ESTADO = 'BUENO' THEN
            :NEW.PORCENTAJE_TASACION := 70;
        ELSIF :NEW.ESTADO = 'REGULAR' THEN
            :NEW.PORCENTAJE_TASACION := 60;
        ELSIF :NEW.ESTADO = 'DEFECTUOSO' THEN
            :NEW.PORCENTAJE_TASACION := 40;
        ELSIF :NEW.ESTADO = 'PROPIEDAD_CASA' THEN
            :NEW.PORCENTAJE_TASACION := 70;
        ELSE
            :NEW.PORCENTAJE_TASACION := 70; -- Default
        END IF;

        -- Calcular valor tasado
        :NEW.VALOR_TASADO := ROUND(:NEW.PRECIO_MERCADO_BASE * (:NEW.PORCENTAJE_TASACION / 100), 2);

        DBMS_OUTPUT.PUT_LINE('Valor tasado calculado: $' ||
                           TO_CHAR(:NEW.VALOR_TASADO, '999,999,999.99') ||
                           ' (' || :NEW.PORCENTAJE_TASACION || '% de $' ||
                           TO_CHAR(:NEW.PRECIO_MERCADO_BASE, '999,999,999.99') || ')');
    END IF;

    -- Asegurar fecha de avaluo
    IF :NEW.FECHA_AVALUO IS NULL THEN
        :NEW.FECHA_AVALUO := SYSDATE;
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error en trigger calcular valor tasado: ' || SQLERRM);
        RAISE;
END TRG_CALCULAR_VALOR_TASADO;
/

COMMENT ON TRIGGER TRG_CALCULAR_VALOR_TASADO IS
'Calcula automaticamente el valor tasado cuando se proporciona el precio de mercado.
Aplica porcentajes segun estado del articulo (OPTIMO=80%, BUENO=70%, etc).
Se ejecuta BEFORE INSERT OR UPDATE en ARTICULO.
Autor: Henersson Cobo';

PROMPT Trigger TRG_CALCULAR_VALOR_TASADO creado exitosamente
PROMPT

-- ============================================================================
-- PASO 8: CREAR VISTA EXTENDIDA DE ARTICULOS
-- ============================================================================

PROMPT ----------------------------------------------------------------------------
PROMPT Paso 8: Creando vista V_ARTICULOS_AVALUO_COMPLETO...
PROMPT ----------------------------------------------------------------------------
PROMPT

CREATE OR REPLACE VIEW V_ARTICULOS_AVALUO_COMPLETO AS
SELECT
    a.ID_ARTICULO,
    a.TIPO_ARTICULO,
    a.DESCRIPCION_ARTICULO,
    a.ESTADO,
    a.PRECIO_MERCADO_BASE,
    a.PORCENTAJE_TASACION,
    a.VALOR_TASADO,
    a.FECHA_AVALUO,
    TRUNC(SYSDATE - a.FECHA_AVALUO) AS DIAS_DESDE_AVALUO,
    CASE
        WHEN TRUNC(SYSDATE - a.FECHA_AVALUO) > 180 THEN 'REQUIERE REVALUACION'
        WHEN TRUNC(SYSDATE - a.FECHA_AVALUO) > 90 THEN 'AVALUO PROXIMO A VENCER'
        ELSE 'AVALUO VIGENTE'
    END AS ESTADO_AVALUO,
    -- Informacion de prestamo si existe
    p.ID_PRESTAMO,
    p.ESTADO_PRESTAMO,
    p.MONTO AS MONTO_PRESTADO,
    -- Calculos
    ROUND((a.VALOR_TASADO / NULLIF(a.PRECIO_MERCADO_BASE, 0)) * 100, 2) AS PORCENTAJE_REAL,
    a.PRECIO_MERCADO_BASE - a.VALOR_TASADO AS MARGEN_SEGURIDAD
FROM ARTICULO a
LEFT JOIN PRESTAMO p ON a.ID_ARTICULO = p.ID_ARTICULO
    AND p.ESTADO_PRESTAMO IN ('ACTIVO', 'EN_MORA', 'VENCIDO')
ORDER BY a.FECHA_AVALUO DESC;

COMMENT ON VIEW V_ARTICULOS_AVALUO_COMPLETO IS
'Vista completa de articulos con informacion de avaluo y precio de mercado.
Incluye calculos de vigencia, porcentajes y margen de seguridad.
Autor: Henersson Cobo';

PROMPT Vista V_ARTICULOS_AVALUO_COMPLETO creada exitosamente
PROMPT

-- ============================================================================
-- PRUEBAS DEL SISTEMA
-- ============================================================================

PROMPT ============================================================================
PROMPT EJECUTANDO PRUEBAS DEL SISTEMA
PROMPT ============================================================================
PROMPT

-- ----------------------------------------------------------------------------
-- PRUEBA 1: Verificar estructura actualizada
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 1: Verificando nueva estructura de ARTICULO...
PROMPT

DESC ARTICULO;

-- ----------------------------------------------------------------------------
-- PRUEBA 2: Verificar datos migrados
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 2: Verificando datos migrados...
PROMPT

SELECT
    COUNT(*) AS TOTAL,
    SUM(CASE WHEN FECHA_AVALUO IS NOT NULL THEN 1 ELSE 0 END) AS CON_FECHA_AVALUO,
    SUM(CASE WHEN PRECIO_MERCADO_BASE IS NOT NULL THEN 1 ELSE 0 END) AS CON_PRECIO_MERCADO,
    SUM(CASE WHEN PORCENTAJE_TASACION IS NOT NULL THEN 1 ELSE 0 END) AS CON_PORCENTAJE
FROM ARTICULO;

-- ----------------------------------------------------------------------------
-- PRUEBA 3: Insertar articulo de prueba
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 3: Insertando articulo de prueba con precio de mercado...
PROMPT

DECLARE
    v_id_articulo NUMBER;
    v_valor_tasado NUMBER;
BEGIN
    SELECT NVL(MAX(ID_ARTICULO), 0) + 1 INTO v_id_articulo FROM ARTICULO;

    DBMS_OUTPUT.PUT_LINE('Insertando articulo con precio de mercado: $1,000,000');
    DBMS_OUTPUT.PUT_LINE('Estado: OPTIMO (deberia aplicar 80%)');

    INSERT INTO ARTICULO (
        ID_ARTICULO,
        TIPO_ARTICULO,
        DESCRIPCION_ARTICULO,
        PRECIO_MERCADO_BASE,
        ESTADO
    ) VALUES (
        v_id_articulo,
        'ELECTRONICO',
        'Laptop de prueba - calculo automatico',
        1000000, -- 1 millon
        'OPTIMO'
    );

    -- Verificar calculo
    SELECT VALOR_TASADO INTO v_valor_tasado
    FROM ARTICULO WHERE ID_ARTICULO = v_id_articulo;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('*** PRUEBA 3: RESULTADO ***');
    DBMS_OUTPUT.PUT_LINE('Valor tasado calculado: $' || TO_CHAR(v_valor_tasado, '999,999,999.99'));
    DBMS_OUTPUT.PUT_LINE('Esperado: $800,000.00 (80% de $1,000,000)');

    IF v_valor_tasado = 800000 THEN
        DBMS_OUTPUT.PUT_LINE('*** PRUEBA 3: EXITOSA ***');
    ELSE
        DBMS_OUTPUT.PUT_LINE('*** PRUEBA 3: FALLIDA ***');
    END IF;

    ROLLBACK;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('*** PRUEBA 3: ERROR ***');
        DBMS_OUTPUT.PUT_LINE(SQLERRM);
        ROLLBACK;
END;
/

-- ----------------------------------------------------------------------------
-- PRUEBA 4: Consultar vista de avaluos
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 4: Consultando vista de avaluos completa...
PROMPT

SELECT
    ID_ARTICULO,
    SUBSTR(TIPO_ARTICULO, 1, 12) AS TIPO,
    TO_CHAR(PRECIO_MERCADO_BASE, '$ 999,999,999') AS PRECIO_MERCADO,
    PORCENTAJE_TASACION || '%' AS "% TASACION",
    TO_CHAR(VALOR_TASADO, '$ 999,999,999') AS VALOR_TASADO,
    DIAS_DESDE_AVALUO,
    ESTADO_AVALUO
FROM V_ARTICULOS_AVALUO_COMPLETO
WHERE ROWNUM <= 10;

-- ----------------------------------------------------------------------------
-- PRUEBA 5: Probar funcion de calculo
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Prueba 5: Probando funcion FN_CALCULAR_VALOR_TASADO...
PROMPT

SELECT
    'OPTIMO (80%)' AS ESTADO,
    TO_CHAR(FN_CALCULAR_VALOR_TASADO(1000000, 'OPTIMO'), '$ 999,999,999.99') AS VALOR_TASADO
FROM DUAL
UNION ALL
SELECT
    'BUENO (70%)' AS ESTADO,
    TO_CHAR(FN_CALCULAR_VALOR_TASADO(1000000, 'BUENO'), '$ 999,999,999.99') AS VALOR_TASADO
FROM DUAL
UNION ALL
SELECT
    'REGULAR (60%)' AS ESTADO,
    TO_CHAR(FN_CALCULAR_VALOR_TASADO(1000000, 'REGULAR'), '$ 999,999,999.99') AS VALOR_TASADO
FROM DUAL
UNION ALL
SELECT
    'DEFECTUOSO (40%)' AS ESTADO,
    TO_CHAR(FN_CALCULAR_VALOR_TASADO(1000000, 'DEFECTUOSO'), '$ 999,999,999.99') AS VALOR_TASADO
FROM DUAL;

-- ============================================================================
-- RESUMEN FINAL
-- ============================================================================

PROMPT
PROMPT ============================================================================
PROMPT MODIFICACION DE TABLA ARTICULO COMPLETADA
PROMPT ============================================================================
PROMPT
PROMPT Campos agregados:
PROMPT   [+] FECHA_AVALUO (DATE, default SYSDATE)
PROMPT   [+] PRECIO_MERCADO_BASE (NUMBER(12,2))
PROMPT   [+] PORCENTAJE_TASACION (NUMBER(5,2), default 70)
PROMPT
PROMPT Componentes creados:
PROMPT   [+] Funcion: FN_CALCULAR_VALOR_TASADO
PROMPT   [+] Trigger: TRG_CALCULAR_VALOR_TASADO
PROMPT   [+] Vista: V_ARTICULOS_AVALUO_COMPLETO
PROMPT
PROMPT Logica de calculo:
PROMPT   VALOR_TASADO = PRECIO_MERCADO_BASE * (PORCENTAJE / 100)
PROMPT
PROMPT Porcentajes segun estado:
PROMPT   - OPTIMO: 80%
PROMPT   - BUENO: 70%
PROMPT   - REGULAR: 60%
PROMPT   - DEFECTUOSO: 40%
PROMPT
PROMPT Datos existentes:
PROMPT   - Migrados automaticamente
PROMPT   - Fechas de avaluo asignadas
PROMPT   - Precios de mercado calculados (inverso)
PROMPT
PROMPT Consultas utiles:
PROMPT   SELECT * FROM V_ARTICULOS_AVALUO_COMPLETO;
PROMPT   SELECT FN_CALCULAR_VALOR_TASADO(precio, estado) FROM DUAL;
PROMPT
PROMPT Estado de objetos:

SELECT object_name, object_type, status
FROM user_objects
WHERE object_name IN (
    'FN_CALCULAR_VALOR_TASADO',
    'TRG_CALCULAR_VALOR_TASADO',
    'V_ARTICULOS_AVALUO_COMPLETO'
)
ORDER BY object_type, object_name;

PROMPT
PROMPT Estructura final de ARTICULO:
DESC ARTICULO;

PROMPT
PROMPT ============================================================================
PROMPT SCRIPT COMPLETADO
PROMPT ============================================================================

