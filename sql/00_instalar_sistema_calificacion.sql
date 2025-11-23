-- ============================================================================
-- SCRIPT MAESTRO DE INSTALACIÓN
-- Descripción: Ejecuta todos los scripts del sistema de calificación automática
--              en el orden correcto.
--
-- Autor: Henersson Cobo, Jorge Mera, Fabián Ome
-- Proyecto: Sistema de Préstamos y Casa de Empeño
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
--
-- INSTRUCCIONES DE USO:
-- 1. Conectarse a SQL*Plus o SQL Developer con el usuario apropiado
-- 2. Ejecutar este script: @00_instalar_sistema_calificacion.sql
-- 3. Verificar que no haya errores en la salida
-- ============================================================================

SET ECHO ON
SET SERVEROUTPUT ON SIZE UNLIMITED
SET LINESIZE 200
SET PAGESIZE 100

PROMPT ============================================================================
PROMPT INSTALACIÓN DEL SISTEMA DE CALIFICACIÓN AUTOMÁTICA DE CLIENTES
PROMPT ============================================================================
PROMPT Proyecto: Sistema de Préstamos y Casa de Empeño
PROMPT Curso: Bases de Datos II
PROMPT Autores: Henersson Cobo, Jorge Mera, Fabián Ome
PROMPT Fecha: 2025-11-23
PROMPT ============================================================================
PROMPT

PROMPT ----------------------------------------------------------------------------
PROMPT PASO 1/4: Creando función FN_CALCULAR_CALIFICACION_CLIENTE...
PROMPT ----------------------------------------------------------------------------
@@01_funcion_calcular_calificacion.sql
PROMPT

PROMPT ----------------------------------------------------------------------------
PROMPT PASO 2/4: Creando trigger TRG_ACTUALIZAR_CALIFICACION_CLIENTE...
PROMPT ----------------------------------------------------------------------------
@@02_trigger_actualizar_calificacion.sql
PROMPT

PROMPT ----------------------------------------------------------------------------
PROMPT PASO 3/4: Creando procedimiento SP_ACTUALIZAR_TODAS_CALIFICACIONES...
PROMPT ----------------------------------------------------------------------------
@@03_procedimiento_actualizar_calificaciones.sql
PROMPT

PROMPT ----------------------------------------------------------------------------
PROMPT PASO 4/4: Ejecutando actualización inicial de calificaciones...
PROMPT ----------------------------------------------------------------------------
EXECUTE SP_ACTUALIZAR_TODAS_CALIFICACIONES;
PROMPT

PROMPT ============================================================================
PROMPT INSTALACIÓN COMPLETADA EXITOSAMENTE
PROMPT ============================================================================
PROMPT
PROMPT El sistema de calificación automática ha sido instalado correctamente.
PROMPT
PROMPT Componentes instalados:
PROMPT   ✓ FN_CALCULAR_CALIFICACION_CLIENTE - Función de cálculo
PROMPT   ✓ TRG_ACTUALIZAR_CALIFICACION_CLIENTE - Trigger automático
PROMPT   ✓ SP_ACTUALIZAR_TODAS_CALIFICACIONES - Procedimiento de actualización
PROMPT
PROMPT Características:
PROMPT   • Las calificaciones se actualizan automáticamente al cambiar préstamos
PROMPT   • Calificación basada en historial real del cliente
PROMPT   • Rango de calificación: 1.0 a 10.0
PROMPT
PROMPT Para ejecutar pruebas, ejecute: @04_pruebas_sistema_calificacion.sql
PROMPT
PROMPT ============================================================================

