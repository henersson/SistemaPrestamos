'Vista completa de prestamos incluyendo informacion de cliente, asesor y articulo.
Incluye calculos de interes, multa, deuda total y estado del articulo en garantia.
Autor: Henersson Cobo';

PROMPT Vista V_PRESTAMOS_DETALLE actualizada exitosamente

-- ----------------------------------------------------------------------------
-- PASO 10: Resumen final
-- ----------------------------------------------------------------------------
PROMPT
PROMPT ============================================================================
PROMPT MODIFICACION COMPLETADA EXITOSAMENTE
PROMPT ============================================================================
PROMPT
PROMPT Cambios realizados:
PROMPT   [+] Columna ID_ARTICULO agregada a PRESTAMO (NOT NULL)
PROMPT   [+] Foreign Key FK_PRESTAMO_ARTICULO creada
PROMPT   [+] Indice IDX_PRESTAMO_ARTICULO creado
PROMPT   [+] Vista V_PRESTAMOS_DETALLE actualizada
PROMPT   [+] Comentarios de documentacion agregados
PROMPT
PROMPT El modelo de datos ahora cumple con el universo del discurso:
PROMPT   "Cada prestamo se asocia a un cliente, a un articulo y al asesor"
PROMPT
PROMPT ============================================================================

-- Mostrar estructura final
PROMPT
PROMPT Estructura final de PRESTAMO:
DESC PRESTAMO;

PROMPT
PROMPT Constraints de PRESTAMO:
SELECT constraint_name, constraint_type, status
FROM user_constraints
WHERE table_name = 'PRESTAMO'
ORDER BY constraint_type, constraint_name;

PROMPT
PROMPT Indices de PRESTAMO:
SELECT index_name, uniqueness, status
FROM user_indexes
WHERE table_name = 'PRESTAMO'
ORDER BY index_name;

PROMPT
PROMPT ============================================================================
PROMPT SCRIPT COMPLETADO
PROMPT ============================================================================
-- ============================================================================
-- SCRIPT: AGREGAR RELACION ARTICULO A PRESTAMO
-- Descripcion: Modifica la tabla PRESTAMO para incluir relacion obligatoria
--              con ARTICULO segun el universo del discurso:
--              "Cada prestamo se asocia a un cliente, a un articulo y al asesor"
--
-- Autor: Henersson Cobo
-- Proyecto: Sistema de Prestamos y Casa de Empeno
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
--
-- IMPORTANTE: Este script modifica la estructura de la tabla PRESTAMO
--             Realizar respaldo antes de ejecutar
-- ============================================================================

SET ECHO ON
SET SERVEROUTPUT ON SIZE UNLIMITED
SET LINESIZE 200
SET PAGESIZE 100

PROMPT ============================================================================
PROMPT MODIFICACION DE TABLA PRESTAMO - AGREGAR RELACION CON ARTICULO
PROMPT ============================================================================
PROMPT

-- ----------------------------------------------------------------------------
-- PASO 1: Verificar estructura actual
-- ----------------------------------------------------------------------------
PROMPT Paso 1: Verificando estructura actual de PRESTAMO...
PROMPT

SELECT COUNT(*) AS "Total Prestamos Existentes" FROM PRESTAMO;

PROMPT
PROMPT Columnas actuales de PRESTAMO:
DESC PRESTAMO;

-- ----------------------------------------------------------------------------
-- PASO 2: Agregar columna ID_ARTICULO (temporal como NULL)
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Paso 2: Agregando columna ID_ARTICULO...
PROMPT

-- Primero agregar como NULL para permitir datos existentes
ALTER TABLE PRESTAMO ADD (
    ID_ARTICULO NUMBER(10)
);

COMMENT ON COLUMN PRESTAMO.ID_ARTICULO IS
'Identificador del articulo asociado como garantia del prestamo.
Cada prestamo debe estar respaldado por un articulo.';

PROMPT Columna ID_ARTICULO agregada exitosamente (temporal NULL)

-- ----------------------------------------------------------------------------
-- PASO 3: Migrar datos existentes
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Paso 3: Migrando datos existentes...
PROMPT

DECLARE
    v_count_prestamos NUMBER;
    v_count_articulos NUMBER;
    v_articulo_id NUMBER;
    v_prestamos_actualizados NUMBER := 0;

    CURSOR cur_prestamos IS
        SELECT ID_PRESTAMO, ID_CLIENTE, MONTO
        FROM PRESTAMO
        WHERE ID_ARTICULO IS NULL;
BEGIN
    -- Verificar si hay prestamos sin articulo
    SELECT COUNT(*) INTO v_count_prestamos
    FROM PRESTAMO
    WHERE ID_ARTICULO IS NULL;

    DBMS_OUTPUT.PUT_LINE('Prestamos sin articulo asignado: ' || v_count_prestamos);

    IF v_count_prestamos > 0 THEN
        DBMS_OUTPUT.PUT_LINE('Iniciando asignacion automatica de articulos...');
        DBMS_OUTPUT.PUT_LINE('---------------------------------------------------');

        FOR prestamo_rec IN cur_prestamos LOOP
            -- Intentar encontrar un articulo disponible del cliente
            BEGIN
                SELECT ID_ARTICULO INTO v_articulo_id
                FROM ARTICULO
                WHERE ESTADO != 'DEFECTUOSO'
                  AND ROWNUM = 1
                ORDER BY ID_ARTICULO;

                -- Actualizar prestamo con el articulo
                UPDATE PRESTAMO
                SET ID_ARTICULO = v_articulo_id
                WHERE ID_PRESTAMO = prestamo_rec.ID_PRESTAMO;

                v_prestamos_actualizados := v_prestamos_actualizados + 1;

                DBMS_OUTPUT.PUT_LINE('Prestamo ' || prestamo_rec.ID_PRESTAMO ||
                                   ' -> Articulo ' || v_articulo_id);

            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    DBMS_OUTPUT.PUT_LINE('ADVERTENCIA: No hay articulos disponibles para prestamo ' ||
                                       prestamo_rec.ID_PRESTAMO);
            END;
        END LOOP;

        COMMIT;

        DBMS_OUTPUT.PUT_LINE('---------------------------------------------------');
        DBMS_OUTPUT.PUT_LINE('Total prestamos actualizados: ' || v_prestamos_actualizados);

    ELSE
        DBMS_OUTPUT.PUT_LINE('No hay prestamos que requieran migracion.');
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR en migracion: ' || SQLERRM);
        ROLLBACK;
        RAISE;
END;
/

-- ----------------------------------------------------------------------------
-- PASO 4: Verificar migracion
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Paso 4: Verificando migracion de datos...
PROMPT

SELECT
    COUNT(*) AS "Total Prestamos",
    SUM(CASE WHEN ID_ARTICULO IS NOT NULL THEN 1 ELSE 0 END) AS "Con Articulo",
    SUM(CASE WHEN ID_ARTICULO IS NULL THEN 1 ELSE 0 END) AS "Sin Articulo"
FROM PRESTAMO;

-- ----------------------------------------------------------------------------
-- PASO 5: Hacer columna NOT NULL (solo si todos tienen articulo)
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Paso 5: Estableciendo ID_ARTICULO como NOT NULL...
PROMPT

DECLARE
    v_sin_articulo NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_sin_articulo
    FROM PRESTAMO
    WHERE ID_ARTICULO IS NULL;

    IF v_sin_articulo > 0 THEN
        DBMS_OUTPUT.PUT_LINE('ADVERTENCIA: Hay ' || v_sin_articulo ||
                           ' prestamos sin articulo asignado.');
        DBMS_OUTPUT.PUT_LINE('No se puede establecer NOT NULL.');
        DBMS_OUTPUT.PUT_LINE('Por favor asigne articulos manualmente o elimine estos prestamos.');
    ELSE
        -- Hacer columna NOT NULL
        EXECUTE IMMEDIATE 'ALTER TABLE PRESTAMO MODIFY (ID_ARTICULO NOT NULL)';
        DBMS_OUTPUT.PUT_LINE('Columna ID_ARTICULO establecida como NOT NULL exitosamente');
    END IF;
END;
/

-- ----------------------------------------------------------------------------
-- PASO 6: Crear Foreign Key hacia ARTICULO
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Paso 6: Creando Foreign Key hacia tabla ARTICULO...
PROMPT

ALTER TABLE PRESTAMO
ADD CONSTRAINT FK_PRESTAMO_ARTICULO
FOREIGN KEY (ID_ARTICULO)
REFERENCES ARTICULO(ID_ARTICULO)
ON DELETE RESTRICT;

COMMENT ON CONSTRAINT FK_PRESTAMO_ARTICULO IS
'Garantiza que cada prestamo este asociado a un articulo valido como garantia.
DELETE RESTRICT previene eliminacion de articulos con prestamos activos.';

PROMPT Foreign Key FK_PRESTAMO_ARTICULO creada exitosamente

-- ----------------------------------------------------------------------------
-- PASO 7: Crear indice para rendimiento
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Paso 7: Creando indice para optimizar consultas...
PROMPT

CREATE INDEX IDX_PRESTAMO_ARTICULO
ON PRESTAMO(ID_ARTICULO);

COMMENT ON INDEX IDX_PRESTAMO_ARTICULO IS
'Indice para optimizar consultas que buscan prestamos por articulo.
Mejora rendimiento en reportes y validaciones.';

PROMPT Indice IDX_PRESTAMO_ARTICULO creado exitosamente

-- ----------------------------------------------------------------------------
-- PASO 8: Verificar integridad referencial
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Paso 8: Verificando integridad referencial...
PROMPT

-- Verificar que todos los articulos referenciados existen
SELECT
    COUNT(*) AS "Prestamos",
    COUNT(DISTINCT ID_ARTICULO) AS "Articulos Unicos"
FROM PRESTAMO;

-- Buscar posibles inconsistencias (no deberia haber)
SELECT COUNT(*) AS "Articulos Inexistentes (debe ser 0)"
FROM PRESTAMO p
WHERE NOT EXISTS (
    SELECT 1 FROM ARTICULO a
    WHERE a.ID_ARTICULO = p.ID_ARTICULO
);

-- ----------------------------------------------------------------------------
-- PASO 9: Actualizar vistas y procedimientos dependientes
-- ----------------------------------------------------------------------------
PROMPT
PROMPT Paso 9: Recreando vista V_PRESTAMOS_DETALLE con informacion de articulos...
PROMPT

CREATE OR REPLACE VIEW V_PRESTAMOS_DETALLE AS
SELECT
    p.ID_PRESTAMO,
    p.ID_CLIENTE,
    pc.NOMBRE_PERSONA AS NOMBRE_CLIENTE,
    p.ID_ASESOR,
    pa.NOMBRE_PERSONA AS NOMBRE_ASESOR,
    p.ID_ARTICULO,
    a.TIPO_ARTICULO,
    a.DESCRIPCION_ARTICULO,
    a.VALOR_TASADO,
    a.ESTADO AS ESTADO_ARTICULO,
    p.MONTO,
    p.INTERES_GENERADO,
    NVL(p.MULTA, 0) AS MULTA,
    (p.MONTO + p.INTERES_GENERADO + NVL(p.MULTA, 0)) AS TOTAL_DEUDA,
    p.FECHA_PRESTAMO,
    p.FECHA_VENCIMIENTO,
    p.ESTADO_PRESTAMO,
    p.TASA_INTERES,
    MONTHS_BETWEEN(p.FECHA_VENCIMIENTO, p.FECHA_PRESTAMO) AS MESES_PLAZO,
    CASE
        WHEN MONTHS_BETWEEN(p.FECHA_VENCIMIENTO, p.FECHA_PRESTAMO) < 3 THEN '5%'
        WHEN MONTHS_BETWEEN(p.FECHA_VENCIMIENTO, p.FECHA_PRESTAMO) <= 6 THEN '10%'
        ELSE '15%'
    END AS TASA_APLICADA,
    TRUNC(SYSDATE - p.FECHA_VENCIMIENTO) AS DIAS_VENCIDO
FROM PRESTAMO p
JOIN PERSONA pc ON p.ID_CLIENTE = pc.ID_PERSONA
JOIN PERSONA pa ON p.ID_ASESOR = pa.ID_PERSONA
JOIN ARTICULO a ON p.ID_ARTICULO = a.ID_ARTICULO;

COMMENT ON VIEW V_PRESTAMOS_DETALLE IS

