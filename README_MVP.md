# 🏦 Sistema de Casa de Empeño - MVP (Producto Mínimo Viable)

## 📋 Descripción

Este es el **Producto Mínimo Viable (MVP)** del Sistema de Gestión de Casa de Empeño que implementa la funcionalidad completa para gestionar **artículos** y **préstamos**, cumpliendo con todas las reglas de negocio especificadas.

---

## ✅ Componentes Implementados

### 1. **ValidacionesNegocio.java** ✓
**Ubicación:** `src/com/prestamos/util/ValidacionesNegocio.java`

Clase utilitaria que centraliza todas las validaciones de reglas de negocio:

#### Validaciones Implementadas:
- ✅ **validarArticuloParaPrestamo()** - Verifica que el artículo no sea defectuoso
- ✅ **validarMontoPrestamo()** - Asegura que el monto no exceda el 80% del valor tasado
- ✅ **validarCalificacionCliente()** - Requiere calificación mínima de 3.0
- ✅ **validarFechasPrestamo()** - Valida fechas coherentes y plazo máximo de 12 meses

#### Cálculos Implementados:
- ✅ **calcularMontoMaximo()** - 80% del valor tasado
- ✅ **calcularTasaInteres()** - Basado en el plazo:
  - Menos de 3 meses → 5%
  - Entre 3 y 6 meses → 10%
  - Más de 6 meses → 15%
- ✅ **calcularInteresGenerado()** - Monto × Tasa
- ✅ **calcularMultaPorMora()** - 2% sobre el saldo pendiente
- ✅ **calcularTotalAPagar()** - Monto + Interés + Multa

---

### 2. **ArticuloDAO.java** ✓
**Ubicación:** `src/com/prestamos/dao/ArticuloDAO.java`

Gestión completa de artículos con todas las operaciones CRUD:

#### Métodos Implementados:
- ✅ **insertarArticulo()** - Crea un nuevo artículo (valida que NO sea defectuoso)
- ✅ **obtenerArticuloPorId()** - Busca un artículo específico
- ✅ **listarTodosArticulos()** - Lista todos los artículos
- ✅ **listarArticulosDisponibles()** - Solo artículos ÓPTIMO/FUNCIONABLE
- ✅ **actualizarArticulo()** - Modifica datos del artículo
- ✅ **eliminarArticulo()** - Elimina artículo (valida préstamos activos)
- ✅ **obtenerSiguienteId()** - Obtiene el próximo ID disponible

#### Reglas de Negocio Implementadas:
- 🚫 No se pueden insertar artículos DEFECTUOSOS
- 🚫 No se pueden eliminar artículos con préstamos activos
- ✅ Manejo de transacciones ACID
- ✅ Logging detallado de operaciones

---

### 3. **PrestamoDAO.java** ✓
**Ubicación:** `src/com/prestamos/dao/PrestamoDAO.java`

Gestión completa de préstamos con validaciones de negocio:

#### Métodos Implementados:
- ✅ **crearPrestamo()** - Crea préstamo con todas las validaciones
- ✅ **obtenerPrestamoPorId()** - Busca préstamo específico
- ✅ **listarPrestamosPorCliente()** - Préstamos de un cliente
- ✅ **listarPrestamosPorEstado()** - Filtra por estado (ACTIVO, EN_MORA, etc.)
- ✅ **listarTodosPrestamos()** - Lista completa
- ✅ **aplicarMulta()** - Aplica multa por mora (2%)
- ✅ **actualizarEstadoPrestamo()** - Cambia estado del préstamo
- ✅ **obtenerSiguienteId()** - Próximo ID disponible

#### Validaciones Automáticas al Crear Préstamo:
1. ✅ El artículo NO puede estar DEFECTUOSO
2. ✅ El monto NO puede exceder el 80% del valor tasado
3. ✅ El cliente debe tener calificación >= 3.0
4. ✅ Cálculo automático de tasa de interés según plazo
5. ✅ Cálculo automático de interés generado
6. ✅ Estado inicial: ACTIVO

---

## 🧪 Programa de Prueba

### **TestMVPPrestamos.java**
**Ubicación:** `src/TestMVPPrestamos.java`

Programa completo de demostración que prueba todas las funcionalidades:

#### Pruebas Incluidas:
1. **Gestión de Artículos:**
   - Crear artículo de prueba
   - Listar artículos disponibles
   - Validar artículos

2. **Validaciones de Negocio:**
   - Validar artículo para préstamo
   - Calcular monto máximo permitido
   - Calcular tasa de interés según plazo
   - Calcular interés generado

3. **Gestión de Préstamos:**
   - Crear préstamo con validaciones
   - Listar préstamos por cliente
   - Verificar estado del préstamo

4. **Estadísticas:**
   - Total de artículos y préstamos
   - Capital prestado
   - Intereses generados

---

## 🚀 Cómo Ejecutar el MVP

### Opción 1: Desde la Terminal
```bash
cd C:\Users\DELTA\Desktop\SistemaPrestamos\SistemaPrestamos\src
javac -cp . TestMVPPrestamos.java
java TestMVPPrestamos
```

### Opción 2: Desde el IDE (IntelliJ IDEA)
1. Abrir el proyecto en IntelliJ
2. Navegar a `src/TestMVPPrestamos.java`
3. Click derecho → Run 'TestMVPPrestamos.main()'

---

## 📊 Estructura de Base de Datos Requerida

El MVP asume que ya existen las siguientes tablas:

### Tabla ARTICULO
```sql
- ID_ARTICULO (PK)
- TIPO_ARTICULO
- DESCRIPCION_ARTICULO
- VALOR_TASADO
- ESTADO (OPTIMO, FUNCIONABLE, DEFECTUOSO, PROPIEDAD_CASA)
- PRECIO_MERCADO_BASE
- PORCENTAJE_TASACION
- FECHA_AVALUO
```

### Tabla PRESTAMO
```sql
- ID_PRESTAMO (PK)
- ID_CLIENTE (FK)
- ID_ARTICULO (FK)
- ID_ASESOR (FK)
- ESTADO_PRESTAMO (ACTIVO, EN_MORA, PAGADO, VENCIDO)
- MONTO
- INTERES_GENERADO
- FECHA_PRESTAMO
- FECHA_VENCIMIENTO
- TASA_INTERES
- MULTA
```

### Tabla CLIENTE
```sql
- ID_CLIENTE (PK)
- ID_PERSONA (FK)
- FECHA_REGISTRO
- CALIFICACION
- ACTIVO
```

---

## 🔐 Reglas de Negocio Implementadas

### ✅ Reglas Cumplidas

1. **Artículos:**
   - ✅ Los artículos defectuosos NO se aceptan como garantía
   - ✅ El valor se calcula según precio de mercado vigente
   - ✅ Cada artículo tiene fecha de avalúo

2. **Préstamos:**
   - ✅ Un cliente NO puede solicitar préstamo sin artículo evaluado
   - ✅ El monto máximo es el 80% del valor tasado
   - ✅ La tasa de interés varía según el plazo:
     - Menos de 3 meses → 5%
     - Entre 3 y 6 meses → 10%
     - Más de 6 meses → 15%
   - ✅ Se aplica multa del 2% por mora
   - ✅ El préstamo pasa a "EN_MORA" cuando hay multa

3. **Clientes:**
   - ✅ Se requiere calificación mínima de 3.0 para préstamos
   - ✅ Los administradores NO pueden ser clientes (validación en BD)

4. **Validaciones de Integridad:**
   - ✅ No se pueden eliminar artículos con préstamos activos
   - ✅ Manejo de transacciones ACID
   - ✅ Rollback automático en caso de error

---

## 📈 Funcionalidades del MVP

### Gestión de Artículos ✓
- [x] Crear artículo (validando que no sea defectuoso)
- [x] Listar todos los artículos
- [x] Listar solo artículos disponibles (ÓPTIMO/FUNCIONABLE)
- [x] Actualizar información del artículo
- [x] Eliminar artículo (con validaciones)
- [x] Buscar artículo por ID

### Gestión de Préstamos ✓
- [x] Crear préstamo con todas las validaciones
- [x] Listar préstamos por cliente
- [x] Listar préstamos por estado
- [x] Calcular automáticamente tasa de interés
- [x] Calcular automáticamente interés generado
- [x] Aplicar multas por mora
- [x] Actualizar estado del préstamo

### Validaciones y Cálculos ✓
- [x] Validar artículo para préstamo
- [x] Validar monto del préstamo (máximo 80%)
- [x] Validar calificación del cliente (mínimo 3.0)
- [x] Validar fechas del préstamo
- [x] Calcular interés según plazo
- [x] Calcular multas por mora
- [x] Calcular total a pagar

---

## 🎯 Próximos Pasos (Fuera del MVP)

### Interfaces Gráficas (Opcional)
- [ ] PanelArticulos.java - Interfaz Swing para gestión visual
- [ ] DialogoArticulo.java - Formulario de artículos
- [ ] PanelPrestamos.java - Interfaz de préstamos
- [ ] DialogoPrestamo.java - Formulario de préstamos

### Funcionalidades Adicionales
- [ ] Registro de pagos parciales
- [ ] Historial de transacciones
- [ ] Transferencia de artículos a propiedad de la casa
- [ ] Reportes financieros
- [ ] Sistema de búsqueda avanzada

---

## 📝 Notas Importantes

### Manejo de Errores
- ✅ Todos los métodos tienen try-catch-finally
- ✅ Logging detallado en consola
- ✅ Rollback automático en caso de error
- ✅ Mensajes descriptivos de error

### Transacciones
- ✅ Uso de `conn.setAutoCommit(false)`
- ✅ COMMIT explícito en operaciones exitosas
- ✅ ROLLBACK en caso de error o validación fallida
- ✅ Restauración de auto-commit en finally

### Logging
```
→ Indica inicio de operación
✓ Indica éxito
✗ Indica error
⚠ Indica advertencia
```

---

## 💡 Ejemplo de Uso

```java
// 1. Crear un artículo
ArticuloDAO articuloDAO = new ArticuloDAO();
Articulo articulo = new Articulo();
articulo.setIdArticulo(articuloDAO.obtenerSiguienteId());
articulo.setTipoArticulo("ELECTRODOMESTICO");
articulo.setEstado("OPTIMO");
articulo.setValorTasado(8000.0);
articuloDAO.insertarArticulo(articulo);

// 2. Crear un préstamo
PrestamoDAO prestamoDAO = new PrestamoDAO();
Prestamo prestamo = new Prestamo();
prestamo.setIdPrestamo(prestamoDAO.obtenerSiguienteId());
prestamo.setIdCliente(1);
prestamo.setIdArticulo(articulo.getIdArticulo());
prestamo.setMonto(6000.0); // Máximo 80% de 8000
prestamoDAO.crearPrestamo(prestamo);

// 3. Aplicar multa
prestamoDAO.aplicarMulta(prestamo.getIdPrestamo(), 120.0);
```

---

## 🏆 Resumen

Este MVP implementa **TODAS las funcionalidades esenciales** para gestionar artículos y préstamos en una casa de empeño, cumpliendo con:

✅ **100% de las reglas de negocio especificadas**  
✅ **Validaciones completas**  
✅ **Manejo robusto de errores**  
✅ **Transacciones ACID**  
✅ **Código profesional y documentado**  

El sistema está listo para ser usado y extendido con interfaces gráficas u otras funcionalidades adicionales.

---

## 👨‍💻 Autor
Sistema de Préstamos - Casa de Empeño  
Fecha: 2025-11-23  
Versión: 1.0 (MVP)

