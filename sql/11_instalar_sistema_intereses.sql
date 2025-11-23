-- ============================================================================
-- SCRIPT MAESTRO DE INSTALACION - SISTEMA DE INTERESES
-- Descripcion: Ejecuta todos los scripts del sistema de calculo de intereses
--              en el orden correcto.
--
-- Autor: Henersson Cobo
-- Proyecto: Sistema de Prestamos y Casa de Empeno
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
--
-- INSTRUCCIONES DE USO:
-- 1. Conectarse a SQL*Plus o SQL Developer con el usuario apropiado
-- 2. Ejecutar este script: @11_instalar_sistema_intereses.sql
-- 3. Verificar que no haya errores en la salida
-- ============================================================================

SET ECHO ON
SET SERVEROUTPUT ON SIZE UNLIMITED
SET LINESIZE 200
SET PAGESIZE 100

PROMPT ============================================================================
PROMPT INSTALACION DEL SISTEMA DE CALCULO AUTOMATICO DE INTERESES
PROMPT ============================================================================
PROMPT Proyecto: Sistema de Prestamos y Casa de Empeno
PROMPT Curso: Bases de Datos II
PROMPT Autores: Henersson Cobo
PROMPT Fecha: 2025-11-23
PROMPT ============================================================================
PROMPT

PROMPT ----------------------------------------------------------------------------
PROMPT PASO 1/5: Creando funcion FN_CALCULAR_INTERES...
PROMPT ----------------------------------------------------------------------------
@@05_funcion_calcular_interes.sql
PROMPT

PROMPT ----------------------------------------------------------------------------
PROMPT PASO 2/5: Creando trigger TRG_CALCULAR_INTERES_PRESTAMO...
PROMPT ----------------------------------------------------------------------------
@@06_trigger_calcular_interes.sql
PROMPT

PROMPT ----------------------------------------------------------------------------
PROMPT PASO 3/5: Creando trigger TRG_RECALCULAR_INTERES...
PROMPT ----------------------------------------------------------------------------
@@07_trigger_recalcular_interes.sql
PROMPT

PROMPT ----------------------------------------------------------------------------
PROMPT PASO 4/5: Creando procedimiento SP_ACTUALIZAR_INTERESES_PRESTAMOS...
PROMPT ----------------------------------------------------------------------------
@@08_procedimiento_actualizar_intereses.sql
PROMPT

PROMPT ----------------------------------------------------------------------------
PROMPT PASO 5/5: Creando vista V_PRESTAMOS_DETALLE...
PROMPT ----------------------------------------------------------------------------
@@09_vista_prestamos_detalle.sql
PROMPT

PROMPT ----------------------------------------------------------------------------
PROMPT PASO 6/6: Actualizando intereses de prestamos existentes...
PROMPT ----------------------------------------------------------------------------
EXECUTE SP_ACTUALIZAR_INTERESES_PRESTAMOS;
PROMPT

PROMPT ============================================================================
PROMPT INSTALACION COMPLETADA EXITOSAMENTE
PROMPT ============================================================================
PROMPT
PROMPT El sistema de calculo automatico de intereses ha sido instalado correctamente.
PROMPT
PROMPT Componentes instalados:
PROMPT   - FN_CALCULAR_INTERES - Funcion de calculo
PROMPT   - TRG_CALCULAR_INTERES_PRESTAMO - Trigger automatico en INSERT
PROMPT   - TRG_RECALCULAR_INTERES - Trigger automatico en UPDATE
PROMPT   - SP_ACTUALIZAR_INTERESES_PRESTAMOS - Procedimiento de actualizacion
PROMPT   - V_PRESTAMOS_DETALLE - Vista con informacion completa
PROMPT
PROMPT Tasas de interes aplicadas:
PROMPT   - Menos de 3 meses: 5%
PROMPT   - Entre 3 y 6 meses: 10%
PROMPT   - Mas de 6 meses: 15%
PROMPT
PROMPT Para ejecutar pruebas, ejecute: @10_pruebas_sistema_intereses.sql
PROMPT
PROMPT ============================================================================
