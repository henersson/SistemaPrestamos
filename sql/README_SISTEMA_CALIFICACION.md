# Sistema de Calificación Automática de Clientes

## Descripción General

Este sistema implementa la calificación automática de clientes basada en su historial de préstamos en la base de datos Oracle. La calificación se actualiza automáticamente mediante triggers cuando cambia el estado de los préstamos.

## Autores

- **Henersson Cobo**

**Proyecto:** Sistema de Préstamos y Casa de Empeño  
**Curso:** Bases de Datos II  
**Fecha:** 2025-11-23

---

## Componentes del Sistema

### 1. Función: `FN_CALCULAR_CALIFICACION_CLIENTE`

**Propósito:** Calcula la calificación de un cliente basándose en su historial de préstamos.

**Parámetros:**
- `p_id_cliente` (NUMBER): ID del cliente a evaluar

**Retorna:** NUMBER - Calificación entre 1.0 y 10.0

**Fórmula de Calificación:**

```
Calificación Base = 5.0

+ 0.3 puntos por cada préstamo CANCELADO (máximo +3.0)
+ 0.1 puntos por cada préstamo ACTIVO (máximo +1.0)
- 1.5 puntos por cada préstamo EN_MORA
- 2.0 puntos por cada préstamo VENCIDO

Rango final: 1.0 - 10.0
```

**Ejemplo de uso:**
```sql
SELECT FN_CALCULAR_CALIFICACION_CLIENTE(12345) FROM DUAL;
```

---

### 2. Trigger: `TRG_ACTUALIZAR_CALIFICACION_CLIENTE`

**Propósito:** Actualiza automáticamente la calificación del cliente cuando cambia el estado de un préstamo.

**Eventos:** 
- AFTER INSERT ON PRESTAMO
- AFTER UPDATE OF ESTADO_PRESTAMO ON PRESTAMO

**Nivel:** FOR EACH ROW

**Comportamiento:**
- Se ejecuta automáticamente cada vez que se inserta un nuevo préstamo
- Se ejecuta cuando cambia el estado de un préstamo existente
- Recalcula y actualiza la calificación en la tabla CLIENTE

---

### 3. Procedimiento: `SP_ACTUALIZAR_TODAS_CALIFICACIONES`

**Propósito:** Recalcula y actualiza las calificaciones de TODOS los clientes activos.

**Uso:**
```sql
EXECUTE SP_ACTUALIZAR_TODAS_CALIFICACIONES;
```

**Características:**
- Procesa todos los clientes con ACTIVO = 'S'
- Muestra el progreso en consola (DBMS_OUTPUT)
- Realiza COMMIT al finalizar exitosamente
- Realiza ROLLBACK en caso de error

---

## Instalación

### Opción 1: Instalación Automática (Recomendada)

1. Conectarse a SQL*Plus o SQL Developer
2. Navegar al directorio `sql/`
3. Ejecutar el script maestro:

```sql
@00_instalar_sistema_calificacion.sql
```

Este script ejecutará automáticamente todos los componentes en el orden correcto.

---

### Opción 2: Instalación Manual

Ejecutar los scripts en este orden:

```sql
-- 1. Crear la función
@01_funcion_calcular_calificacion.sql

-- 2. Crear el trigger
@02_trigger_actualizar_calificacion.sql

-- 3. Crear el procedimiento
@03_procedimiento_actualizar_calificaciones.sql

-- 4. Actualizar calificaciones existentes
EXECUTE SP_ACTUALIZAR_TODAS_CALIFICACIONES;
```

---

## Pruebas y Verificación

Para verificar que el sistema funciona correctamente:

```sql
@04_pruebas_sistema_calificacion.sql
```

Este script ejecutará:
- Comparación de calificaciones actuales vs calculadas
- Actualización de todas las calificaciones
- Verificación post-actualización
- Estadísticas generales
- Distribución por categorías

---

## Categorías de Calificación

| Rango | Categoría | Descripción |
|-------|-----------|-------------|
| 8.0 - 10.0 | EXCELENTE | Cliente con historial impecable |
| 6.5 - 7.9 | BUENO | Cliente confiable con buen historial |
| 5.0 - 6.4 | REGULAR | Cliente promedio |
| 3.0 - 4.9 | MALO | Cliente con problemas de pago |
| 1.0 - 2.9 | MUY MALO | Cliente con historial negativo |

---

## Ejemplos de Calificación

### Cliente Nuevo (sin préstamos):
- Calificación: **5.0** (base)

### Cliente con 5 préstamos pagados:
- Base: 5.0
- Préstamos cancelados: 5 × 0.3 = +1.5
- **Total: 6.5** (BUENO)

### Cliente con 10 préstamos pagados y 1 activo:
- Base: 5.0
- Préstamos cancelados: 10 × 0.3 = +3.0 (máximo)
- Préstamo activo: 1 × 0.1 = +0.1
- **Total: 8.1** (EXCELENTE)

### Cliente con 3 pagados y 1 en mora:
- Base: 5.0
- Préstamos cancelados: 3 × 0.3 = +0.9
- Préstamo en mora: 1 × 1.5 = -1.5
- **Total: 4.4** (MALO)

---

## Consultas Útiles

### Ver calificaciones de todos los clientes:
```sql
SELECT 
    p.NOMBRE_PERSONA,
    c.CALIFICACION,
    (SELECT COUNT(*) FROM PRESTAMO WHERE ID_CLIENTE = p.ID_PERSONA) AS NUM_PRESTAMOS
FROM PERSONA p
JOIN CLIENTE c ON p.ID_PERSONA = c.ID_PERSONA
WHERE p.TIPO_PERSONA = 'CLIENTE'
ORDER BY c.CALIFICACION DESC;
```

### Ver detalle de préstamos de un cliente:
```sql
SELECT 
    ID_PRESTAMO,
    ESTADO_PRESTAMO,
    MONTO_PRESTADO,
    FECHA_PRESTAMO,
    FECHA_VENCIMIENTO
FROM PRESTAMO
WHERE ID_CLIENTE = :id_cliente
ORDER BY FECHA_PRESTAMO DESC;
```

### Ver estadísticas generales:
```sql
SELECT 
    ROUND(AVG(CALIFICACION), 2) AS PROMEDIO,
    MAX(CALIFICACION) AS MAXIMA,
    MIN(CALIFICACION) AS MINIMA,
    COUNT(*) AS TOTAL_CLIENTES
FROM CLIENTE
WHERE ACTIVO = 'S';
```

---

## Mantenimiento

### Recalcular todas las calificaciones:
```sql
EXECUTE SP_ACTUALIZAR_TODAS_CALIFICACIONES;
```

### Verificar estado de los objetos:
```sql
SELECT object_name, object_type, status
FROM user_objects
WHERE object_name IN (
    'FN_CALCULAR_CALIFICACION_CLIENTE',
    'TRG_ACTUALIZAR_CALIFICACION_CLIENTE',
    'SP_ACTUALIZAR_TODAS_CALIFICACIONES'
);
```

### Desinstalar el sistema (si es necesario):
```sql
DROP TRIGGER TRG_ACTUALIZAR_CALIFICACION_CLIENTE;
DROP PROCEDURE SP_ACTUALIZAR_TODAS_CALIFICACIONES;
DROP FUNCTION FN_CALCULAR_CALIFICACION_CLIENTE;
```

---

## Notas Importantes

1. **Actualización Automática**: Una vez instalado, las calificaciones se actualizan automáticamente. No es necesario ejecutar manualmente el procedimiento salvo que se requiera una recalculación completa.

2. **Rendimiento**: El trigger se ejecuta por cada fila (FOR EACH ROW), por lo que es eficiente incluso con muchas actualizaciones.

3. **Transacciones**: El procedimiento `SP_ACTUALIZAR_TODAS_CALIFICACIONES` utiliza COMMIT/ROLLBACK para garantizar la integridad de los datos.

4. **Errores**: El trigger está diseñado para no interrumpir transacciones principales en caso de error, registrando el error en DBMS_OUTPUT.

5. **Estados de Préstamo**: El sistema reconoce los siguientes estados:
   - `ACTIVO`: Préstamo en curso, sin problemas
   - `CANCELADO`: Préstamo pagado completamente
   - `EN_MORA`: Préstamo con pagos atrasados
   - `VENCIDO`: Préstamo no pagado después del vencimiento

---

## Soporte y Contacto

Para preguntas o problemas con el sistema, contactar a:
- Henersson Cobo

**Proyecto:** Sistema de Préstamos y Casa de Empeño  
**Curso:** Bases de Datos II  
**Universidad:** [Nombre de la Universidad]

---

**Versión:** 1.0  
**Última actualización:** 2025-11-23
-- ============================================================================
-- TRIGGER: TRG_ACTUALIZAR_CALIFICACION_CLIENTE
-- Descripción: Actualiza automáticamente la calificación del cliente cuando
--              se inserta o actualiza el estado de un préstamo.
-- 
-- Autor: Henersson Cobo
-- Proyecto: Sistema de Préstamos y Casa de Empeño
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
-- 
-- Eventos: AFTER INSERT OR UPDATE OF ESTADO_PRESTAMO ON PRESTAMO
-- Nivel: FOR EACH ROW
-- ============================================================================

CREATE OR REPLACE TRIGGER TRG_ACTUALIZAR_CALIFICACION_CLIENTE
AFTER INSERT OR UPDATE OF ESTADO_PRESTAMO ON PRESTAMO
FOR EACH ROW
DECLARE
    v_nueva_calificacion NUMBER;
BEGIN
    -- Calcular nueva calificación usando la función
    v_nueva_calificacion := FN_CALCULAR_CALIFICACION_CLIENTE(:NEW.ID_CLIENTE);
    
    -- Actualizar en tabla CLIENTE
    UPDATE CLIENTE
    SET CALIFICACION = v_nueva_calificacion
    WHERE ID_PERSONA = :NEW.ID_CLIENTE;
    
    DBMS_OUTPUT.PUT_LINE('✓ Calificación actualizada para cliente ' || 
                         :NEW.ID_CLIENTE || ': ' || v_nueva_calificacion);
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ Error actualizando calificación: ' || SQLERRM);
        -- No lanzar error para no interrumpir la transacción principal
END TRG_ACTUALIZAR_CALIFICACION_CLIENTE;
/

-- Agregar comentario de documentación
COMMENT ON TRIGGER TRG_ACTUALIZAR_CALIFICACION_CLIENTE IS
'Actualiza automáticamente la calificación del cliente cuando cambia el estado de un préstamo.
Se ejecuta después de INSERT o UPDATE en la tabla PRESTAMO.
Autor: Henersson Cobo
Proyecto: Casa de Empeño - Bases de Datos II';

-- Mostrar mensaje de confirmación
PROMPT ✓ Trigger TRG_ACTUALIZAR_CALIFICACION_CLIENTE creado exitosamente
