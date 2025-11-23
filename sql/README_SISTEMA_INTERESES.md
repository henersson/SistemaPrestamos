# Sistema de Calculo Automatico de Intereses

## Descripcion General

Este sistema implementa el calculo automatico de intereses para prestamos basado en el tiempo de duracion del prestamo. Los intereses se calculan automaticamente al crear o modificar prestamos segun las tasas definidas en el universo del discurso.

## Autores

- **Henersson Cobo**
- **Jorge Mera**
- **Fabian Ome**

**Proyecto:** Sistema de Prestamos y Casa de Empeno  
**Curso:** Bases de Datos II  
**Fecha:** 2025-11-23

---

## Componentes del Sistema

### 1. Funcion: `FN_CALCULAR_INTERES`

**Proposito:** Calcula el interes de un prestamo basandose en el monto y el tiempo de duracion.

**Parametros:**
- `p_monto` (NUMBER): Monto del prestamo
- `p_fecha_inicio` (DATE): Fecha de inicio del prestamo
- `p_fecha_fin` (DATE): Fecha de vencimiento del prestamo

**Retorna:** NUMBER - Interes calculado

**Formula de Interes:**

```
Plazo menos de 3 meses:
  Interes = Monto * 5%

Plazo entre 3 y 6 meses:
  Interes = Monto * 10%

Plazo mas de 6 meses:
  Interes = Monto * 15%
```

**Ejemplo de uso:**
```sql
SELECT FN_CALCULAR_INTERES(1000000, SYSDATE, ADD_MONTHS(SYSDATE, 4)) FROM DUAL;
```

---

### 2. Trigger: `TRG_CALCULAR_INTERES_PRESTAMO`

**Proposito:** Calcula automaticamente el interes al crear un nuevo prestamo.

**Eventos:** 
- BEFORE INSERT ON PRESTAMO

**Nivel:** FOR EACH ROW

**Comportamiento:**
- Se ejecuta automaticamente antes de insertar un nuevo prestamo
- Calcula el interes usando la funcion FN_CALCULAR_INTERES
- Si no se especifica fecha de prestamo, usa la fecha actual (SYSDATE)
- Si no se especifica estado, lo establece como 'ACTIVO'

---

### 3. Trigger: `TRG_RECALCULAR_INTERES`

**Proposito:** Recalcula el interes si se modifica la fecha de vencimiento.

**Eventos:**
- BEFORE UPDATE OF FECHA_VENCIMIENTO ON PRESTAMO

**Nivel:** FOR EACH ROW

**Comportamiento:**
- Se ejecuta antes de actualizar la fecha de vencimiento
- Solo recalcula si la fecha de vencimiento realmente cambio
- Actualiza automaticamente el campo INTERES_GENERADO

---

### 4. Procedimiento: `SP_ACTUALIZAR_INTERESES_PRESTAMOS`

**Proposito:** Recalcula y actualiza los intereses de TODOS los prestamos activos.

**Uso:**
```sql
EXECUTE SP_ACTUALIZAR_INTERESES_PRESTAMOS;
```

**Caracteristicas:**
- Procesa prestamos con estado 'ACTIVO' o 'EN_MORA'
- Muestra el progreso en consola (DBMS_OUTPUT)
- Realiza COMMIT al finalizar exitosamente
- Realiza ROLLBACK en caso de error

---

### 5. Vista: `V_PRESTAMOS_DETALLE`

**Proposito:** Proporciona una vista completa de los prestamos con calculos incluidos.

**Columnas:**
- ID_PRESTAMO: Identificador del prestamo
- ID_CLIENTE, NOMBRE_CLIENTE: Informacion del cliente
- ID_ASESOR, NOMBRE_ASESOR: Informacion del asesor
- MONTO: Monto del prestamo
- INTERES_GENERADO: Interes calculado
- MULTA: Multa aplicada (si existe)
- TOTAL_DEUDA: Suma de monto + interes + multa
- FECHA_PRESTAMO, FECHA_VENCIMIENTO: Fechas del prestamo
- ESTADO_PRESTAMO: Estado actual
- TASA_INTERES: Tasa registrada
- MESES_PLAZO: Cantidad de meses del prestamo
- TASA_APLICADA: Tasa segun el plazo (5%, 10% o 15%)
- DIAS_VENCIDO: Dias transcurridos desde el vencimiento

**Ejemplo de uso:**
```sql
SELECT * FROM V_PRESTAMOS_DETALLE WHERE ESTADO_PRESTAMO = 'ACTIVO';
```

---

## Instalacion

### Opcion 1: Instalacion Automatica (Recomendada)

1. Conectarse a SQL*Plus o SQL Developer
2. Navegar al directorio `sql/`
3. Ejecutar el script maestro:

```sql
@11_instalar_sistema_intereses.sql
```

Este script ejecutara automaticamente todos los componentes en el orden correcto.

---

### Opcion 2: Instalacion Manual

Ejecutar los scripts en este orden:

```sql
-- 1. Crear la funcion
@05_funcion_calcular_interes.sql

-- 2. Crear trigger de calculo en INSERT
@06_trigger_calcular_interes.sql

-- 3. Crear trigger de recalculo en UPDATE
@07_trigger_recalcular_interes.sql

-- 4. Crear procedimiento de actualizacion
@08_procedimiento_actualizar_intereses.sql

-- 5. Crear vista detallada
@09_vista_prestamos_detalle.sql

-- 6. Actualizar prestamos existentes
EXECUTE SP_ACTUALIZAR_INTERESES_PRESTAMOS;
```

---

## Pruebas y Verificacion

Para verificar que el sistema funciona correctamente:

```sql
@10_pruebas_sistema_intereses.sql
```

Este script ejecutara:
- Prueba de la funcion con diferentes plazos
- Actualizacion de intereses de prestamos existentes
- Consulta de prestamos con la vista detallada
- Verificacion de tasas aplicadas por plazo

---

## Tasas de Interes

| Plazo | Tasa | Ejemplo (Monto $1,000,000) |
|-------|------|---------------------------|
| Menos de 3 meses | 5% | Interes: $50,000 |
| Entre 3 y 6 meses | 10% | Interes: $100,000 |
| Mas de 6 meses | 15% | Interes: $150,000 |

---

## Ejemplos de Calculo

### Prestamo de 2 meses - Monto $500,000:
- Plazo: 2 meses (menos de 3)
- Tasa aplicada: 5%
- Interes: $500,000 * 0.05 = $25,000
- **Total a pagar: $525,000**

### Prestamo de 4 meses - Monto $1,000,000:
- Plazo: 4 meses (entre 3 y 6)
- Tasa aplicada: 10%
- Interes: $1,000,000 * 0.10 = $100,000
- **Total a pagar: $1,100,000**

### Prestamo de 8 meses - Monto $2,000,000:
- Plazo: 8 meses (mas de 6)
- Tasa aplicada: 15%
- Interes: $2,000,000 * 0.15 = $300,000
- **Total a pagar: $2,300,000**

---

## Consultas Utiles

### Ver todos los prestamos con interes calculado:
```sql
SELECT 
    ID_PRESTAMO,
    NOMBRE_CLIENTE,
    TO_CHAR(MONTO, '$ 999,999,999.99') AS MONTO,
    TO_CHAR(INTERES_GENERADO, '$ 999,999,999.99') AS INTERES,
    MESES_PLAZO,
    TASA_APLICADA,
    ESTADO_PRESTAMO
FROM V_PRESTAMOS_DETALLE
ORDER BY ID_PRESTAMO;
```

### Ver resumen por tasa aplicada:
```sql
SELECT 
    TASA_APLICADA,
    COUNT(*) AS CANTIDAD,
    TO_CHAR(SUM(MONTO), '$ 999,999,999.99') AS TOTAL_MONTO,
    TO_CHAR(SUM(INTERES_GENERADO), '$ 999,999,999.99') AS TOTAL_INTERES
FROM V_PRESTAMOS_DETALLE
GROUP BY TASA_APLICADA
ORDER BY TASA_APLICADA;
```

### Insertar nuevo prestamo (interes se calcula automaticamente):
```sql
INSERT INTO PRESTAMO (
    ID_ASESOR, 
    ID_CLIENTE, 
    MONTO, 
    FECHA_VENCIMIENTO,
    TASA_INTERES
) VALUES (
    10,  -- ID del asesor
    100, -- ID del cliente
    500000, -- Monto
    ADD_MONTHS(SYSDATE, 4), -- 4 meses (aplicara 10%)
    0.10
);

COMMIT;
```

---

## Mantenimiento

### Recalcular intereses de todos los prestamos:
```sql
EXECUTE SP_ACTUALIZAR_INTERESES_PRESTAMOS;
```

### Verificar estado de los objetos:
```sql
SELECT object_name, object_type, status
FROM user_objects
WHERE object_name IN (
    'FN_CALCULAR_INTERES',
    'TRG_CALCULAR_INTERES_PRESTAMO',
    'TRG_RECALCULAR_INTERES',
    'SP_ACTUALIZAR_INTERESES_PRESTAMOS',
    'V_PRESTAMOS_DETALLE'
);
```

### Desinstalar el sistema (si es necesario):
```sql
DROP VIEW V_PRESTAMOS_DETALLE;
DROP PROCEDURE SP_ACTUALIZAR_INTERESES_PRESTAMOS;
DROP TRIGGER TRG_RECALCULAR_INTERES;
DROP TRIGGER TRG_CALCULAR_INTERES_PRESTAMO;
DROP FUNCTION FN_CALCULAR_INTERES;
```

---

## Notas Importantes

1. **Calculo Automatico**: Una vez instalado, los intereses se calculan automaticamente al crear prestamos. No es necesario especificar el campo INTERES_GENERADO manualmente.

2. **Recalculo Automatico**: Si se modifica la fecha de vencimiento de un prestamo existente, el interes se recalcula automaticamente.

3. **Prestamos Existentes**: Ejecutar el procedimiento `SP_ACTUALIZAR_INTERESES_PRESTAMOS` para actualizar los intereses de prestamos creados antes de instalar el sistema.

4. **Precision**: Los intereses se redondean a 2 decimales para precision monetaria.

5. **Estados de Prestamo**: El trigger solo actualiza prestamos con estado 'ACTIVO' o 'EN_MORA'.

---

## Integracion con Sistema de Calificaciones

Este sistema trabaja en conjunto con el sistema de calificacion automatica de clientes. Ambos sistemas son independientes pero complementarios:

- **Sistema de Intereses**: Calcula intereses de prestamos
- **Sistema de Calificaciones**: Evalua comportamiento de pago de clientes

Para instalar ambos sistemas:
```sql
@00_instalar_sistema_calificacion.sql
@11_instalar_sistema_intereses.sql
```

---

## Soporte y Contacto

Para preguntas o problemas con el sistema, contactar a:
- Henersson Cobo
- Jorge Mera
- Fabian Ome

**Proyecto:** Sistema de Prestamos y Casa de Empeno  
**Curso:** Bases de Datos II

---

**Version:** 1.0  
**Ultima actualizacion:** 2025-11-23
-- ============================================================================
-- FUNCION: FN_CALCULAR_INTERES
-- Descripcion: Calcula el interes de un prestamo segun el tiempo de duracion
--              aplicando tasas diferenciadas por plazo.
-- 
-- Autor: Henersson Cobo, Jorge Mera, Fabian Ome
-- Proyecto: Sistema de Prestamos y Casa de Empeno
-- Curso: Bases de Datos II
-- Fecha: 2025-11-23
-- 
-- Tasas de Interes:
--   - Menos de 3 meses: 5%
--   - Entre 3 y 6 meses: 10%
--   - Mas de 6 meses: 15%
-- ============================================================================

CREATE OR REPLACE FUNCTION FN_CALCULAR_INTERES(
    p_monto NUMBER,
    p_fecha_inicio DATE,
    p_fecha_fin DATE
) RETURN NUMBER
IS
    v_meses NUMBER;
    v_tasa NUMBER;
    v_interes NUMBER;
BEGIN
    -- Calcular cantidad de meses entre fechas
    v_meses := MONTHS_BETWEEN(p_fecha_fin, p_fecha_inicio);
    
    -- Determinar tasa segun universo del discurso:
    -- Menos de 3 meses -> 5%
    -- Entre 3 y 6 meses -> 10%
    -- Mas de 6 meses -> 15%
    
    IF v_meses < 3 THEN
        v_tasa := 0.05;
        DBMS_OUTPUT.PUT_LINE('Tasa aplicada: 5% (menos de 3 meses)');
    ELSIF v_meses <= 6 THEN
        v_tasa := 0.10;
        DBMS_OUTPUT.PUT_LINE('Tasa aplicada: 10% (entre 3 y 6 meses)');
    ELSE
        v_tasa := 0.15;
        DBMS_OUTPUT.PUT_LINE('Tasa aplicada: 15% (mas de 6 meses)');
    END IF;
    
    -- Calcular interes
    v_interes := p_monto * v_tasa;
    
    DBMS_OUTPUT.PUT_LINE('Monto: $' || TO_CHAR(p_monto, '999,999,999.99'));
    DBMS_OUTPUT.PUT_LINE('Meses: ' || ROUND(v_meses, 2));
    DBMS_OUTPUT.PUT_LINE('Interes calculado: $' || TO_CHAR(v_interes, '999,999,999.99'));
    
    RETURN ROUND(v_interes, 2);
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error en FN_CALCULAR_INTERES: ' || SQLERRM);
        RETURN 0;
END FN_CALCULAR_INTERES;
/

-- Agregar comentario de documentacion
COMMENT ON FUNCTION FN_CALCULAR_INTERES IS 
'Calcula el interes de un prestamo segun el tiempo:
- Menos de 3 meses: 5%
- Entre 3 y 6 meses: 10%
- Mas de 6 meses: 15%
Parametros: monto, fecha_inicio, fecha_fin
Retorna: interes calculado
Autores: Henersson Cobo, Jorge Mera, Fabian Ome';

-- Mostrar mensaje de confirmacion
PROMPT Funcion FN_CALCULAR_INTERES creada exitosamente

