-- Sistema de calculo de intereses
@sql/11_instalar_sistema_intereses.sql
## Informacion del Proyecto

#### 4. Configurar Driver JDBC:

- Ubicar: `C:\app\...\dbhomeXE\jdbc\lib\ojdbc8.jar`
- Copiar a: `SistemaPrestamos/lib/ojdbc8.jar`
- En IntelliJ: `File → Project Structure → Modules → Dependencies → + → JARs`
- Seleccionar el archivo ojdbc8.jar

#### 5. Configurar Conexion en el Codigo:
**Integrantes:**
Editar `src/com/prestamos/config/ConexionOracle.java`:

```java
private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
private static final String USUARIO = "USR_PRESTAMOS";
private static final String PASSWORD = "Prestamos2025";
- Fabian Ome Pena

#### 6. Compilar y Ejecutar:

En IntelliJ IDEA:
- `Build → Build Project` (Ctrl+F9)
- `Run 'FrmLogin.main()'` (Shift+F10)
- Administracion de prestamos con calculo automatico de intereses
---

## Funcionalidades Implementadas
- **IDE:** IntelliJ IDEA 2024
### 1. Gestion de Clientes:
- Listar todos los clientes con tabla profesional
- Agregar nuevos clientes con validaciones
- Editar clientes existentes
- Eliminar clientes (con validacion de dependencias)
- Buscar clientes (en desarrollo)
- Calificacion automatica basada en historial

### 2. Reportes Funcionales:
- **Prestamos Vencidos:** Detalle de prestamos vencidos con dias de mora
- **Clientes VIP:** Clientes con calificacion >= 8.0
- **Inventario de Articulos:** Resumen de articulos por tipo y estado
- Exportacion a archivo TXT
- Formato profesional con tablas y totales

### 3. Diccionario de Datos:
- Explorador de estructura de base de datos
- Visualizacion de tablas con columnas y tipos
- Informacion de paquetes PL/SQL
- Detalles de triggers
- Interfaz tipo arbol navegable

### 4. Autenticacion:
- Login con usuario y contrasena
- Validacion contra base de datos Oracle
- Gestion de sesion de usuario
- Cierre de sesion seguro

### 5. Calculos Automaticos:
- **Intereses:** Calculados automaticamente segun plazo
- **Calificaciones:** Actualizadas automaticamente segun pagos
- **Validaciones:** Reglas de negocio en triggers

---

## Usuarios de Prueba

Para probar el sistema, usar estos usuarios:

```
Usuario: admin
Contrasena: admin123
Tipo: ADMINISTRADOR

Usuario: asesor1
Contrasena: asesor123
Tipo: ASESOR
```

---

## Reglas de Negocio Implementadas

1. **Calificacion de Clientes:**
   - Calificacion base: 5.0
   - +0.3 por cada prestamo pagado (max +3.0)
   - +0.1 por cada prestamo activo (max +1.0)
   - -1.5 por cada prestamo en mora
   - -2.0 por cada prestamo vencido
   - Rango final: 1.0 a 10.0

2. **Intereses de Prestamos:**
   - Menos de 3 meses: 5%
   - Entre 3 y 6 meses: 10%
   - Mas de 6 meses: 15%
   - Calculo automatico al crear prestamo
   - Recalculo automatico si cambia fecha

3. **Validaciones:**
   - Un administrador no puede ser cliente
   - Campos obligatorios en formularios
   - Formato de datos validado
   - Integridad referencial en BD

---

## Arquitectura del Sistema
5. **ARTICULO** - Articulos empanados
### Patron MVC (Modelo-Vista-Controlador):

- **Modelo:** Clases en `com.prestamos.modelo`
  - Representan entidades de la BD
  - Encapsulan logica de negocio
  
- **Vista:** Clases en `com.prestamos.vista`
  - Interfaces graficas Swing
  - Presentacion de datos al usuario

- **Controlador:** Clases en `com.prestamos.controlador` y `dao`
  - Logica de control de flujo
  - Interaccion con base de datos
- **PKG_CLIENTES:** Paquete CRUD de clientes
### Patron DAO (Data Access Object):

- Separacion de logica de acceso a datos
- Clases DAO encapsulan operaciones SQL
- Facilita mantenimiento y pruebas
- **TRG_ACTUALIZAR_CALIFICACION_CLIENTE:** Actualizacion automatica de calificacion
### Patron Singleton:
- **SP_ACTUALIZAR_INTERESES_PRESTAMOS:** Recalcula intereses de prestamos activos
- `ConexionOracle.java` implementa Singleton
- Una sola instancia de conexion BD
- Gestion eficiente de recursos
---
---
SistemaPrestamos/
## Consultas SQL Utiles
│       │   └── ConexionOracle.java          # Singleton conexion BD
### Ver prestamos con detalles:
```sql
SELECT * FROM V_PRESTAMOS_DETALLE
WHERE ESTADO_PRESTAMO = 'ACTIVO'
ORDER BY FECHA_VENCIMIENTO;
```
│       │   ├── Persona.java                 # Clase base
### Ver clientes con calificacion:
```sql
SELECT 
    p.NOMBRE_PERSONA,
    c.CALIFICACION,
    CASE 
        WHEN c.CALIFICACION >= 8.0 THEN 'EXCELENTE'
        WHEN c.CALIFICACION >= 6.5 THEN 'BUENO'
        WHEN c.CALIFICACION >= 5.0 THEN 'REGULAR'
        ELSE 'MALO'
    END AS CATEGORIA
FROM CLIENTE c
JOIN PERSONA p ON c.ID_PERSONA = p.ID_PERSONA
WHERE c.ACTIVO = 'S'
ORDER BY c.CALIFICACION DESC;
```
│       │   ├── Articulo.java                # Modelo articulo
### Recalcular todas las calificaciones:
```sql
EXECUTE SP_ACTUALIZAR_TODAS_CALIFICACIONES;
```
│       │   ├── PanelClientes.java           # Gestion clientes
### Actualizar intereses de prestamos:
```sql
EXECUTE SP_ACTUALIZAR_INTERESES_PRESTAMOS;
```
│       │   ├── PanelReportes.java           # Reportes
---
│   ├── 02_trigger_actualizar_calificacion.sql
## Documentacion Adicional
│   ├── 05_funcion_calcular_interes.sql
Consultar los siguientes archivos para informacion detallada:
│   ├── 10_pruebas_sistema_intereses.sql
- `sql/README_SISTEMA_CALIFICACION.md` - Sistema de calificacion automatica
- `sql/README_SISTEMA_INTERESES.md` - Sistema de calculo de intereses
│   ├── README_SISTEMA_CALIFICACION.md
---
```
## Problemas Conocidos y Soluciones
## Configuracion e Instalacion
### Problema: Error de conexion a Oracle
**Solucion:** Verificar que Oracle Database este ejecutandose y que el puerto 1521 este disponible.
- Oracle Database 21c XE instalado y corriendo
### Problema: ClassNotFoundException ojdbc8
**Solucion:** Agregar ojdbc8.jar a las dependencias del proyecto en IntelliJ.

### Problema: Usuario sin permisos
**Solucion:** Ejecutar como SYSTEM los GRANT necesarios al usuario USR_PRESTAMOS.
#### 1. Clonar el repositorio:
---
git clone https://github.com/usuario/SistemaPrestamos.git
## Trabajo Futuro

Funcionalidades pendientes para futuras versiones:

1. Modulo de Gestion de Prestamos (crear, modificar, cancelar)
2. Modulo de Gestion de Articulos
3. Modulo de Gestion de Asesores
4. Reporte de Rendimiento de Asesores
5. Reporte de Estado Financiero
6. Funcionalidad de Busqueda avanzada de clientes
7. Notificaciones de prestamos proximos a vencer
8. Dashboard con graficos estadisticos
9. Exportacion de reportes a PDF
10. Logs de auditoria en interfaz
Conectar como usuario SYSTEM:

```sql
## Autores
```
**Henersson Estid Cobo Caicedo**  
- Desarrollo de interfaces graficas
- Implementacion de sistema de reportes
- Documentacion del proyecto

**Jorge Andres Mera Vasquez**  
- Diseno de base de datos
- Implementacion de funciones PL/SQL
- Sistema de calificacion automatica

**Fabian Ome Pena**  
- Desarrollo de DAOs
- Sistema de calculo de intereses
- Pruebas e integracion

---

## Licencia

Este proyecto es desarrollado con fines academicos para la asignatura de Bases de Datos II.

---

## Contacto

Para consultas sobre el proyecto:
- Email institucional: [correos de los integrantes]
- Universidad del Valle - Sede Palmira

---

**Proyecto:** Sistema de Prestamos y Casa de Empeno  
**Curso:** Bases de Datos II  
**Version:** 1.0  
**Fecha:** Noviembre 2025
Crear usuario del sistema:

```sql
CREATE USER USR_PRESTAMOS IDENTIFIED BY Prestamos2025;
GRANT CONNECT, RESOURCE TO USR_PRESTAMOS;
GRANT UNLIMITED TABLESPACE TO USR_PRESTAMOS;
GRANT CREATE VIEW TO USR_PRESTAMOS;
GRANT CREATE TRIGGER TO USR_PRESTAMOS;
GRANT CREATE PROCEDURE TO USR_PRESTAMOS;
```

Ejecutar script de creacion de tablas (proporcionado por el profesor).

#### 3. Instalar Sistemas Automaticos:

Conectar como USR_PRESTAMOS y ejecutar:

```sql
-- Sistema de calificacion automatica
@sql/00_instalar_sistema_calificacion.sql

# Ejecutar
java -cp "src;lib/ojdbc8.jar" Main
```

**Linux/Mac:**
```bash
# Compilar
javac -cp ".:lib/ojdbc8.jar" src/Main.java

# Ejecutar
java -cp "src:lib/ojdbc8.jar" Main
```

## ✨ Funcionalidades

### ✅ Implementadas
- **🔐 Sistema de Autenticación**
  - Login con validación contra base de datos
  - Roles: Administrador y Asesor
  - Gestión de sesiones

- **🏠 Dashboard Interactivo**
  - Estadísticas en tiempo real
  - Contadores de clientes, préstamos, artículos y asesores
  - Diseño con tarjetas visuales

- **👥 Gestión de Clientes (CRUD Completo)**
  - Crear nuevos clientes
  - Listar todos los clientes en tabla
  - Editar información de clientes
  - Eliminar clientes
  - Búsqueda y filtrado

- **📊 Sistema de Reportes**
  - Préstamos vencidos (con días de mora)
  - Clientes VIP (calificación >= 8.0)
  - Inventario de artículos empeñados
  - Exportación a archivos TXT

- **📚 Diccionario de Datos**
  - Explorador de estructura de base de datos
  - Visualización de tablas con columnas y tipos
  - Información de paquetes PL/SQL
  - Detalles de triggers
  - Conteo de registros por tabla

### ⏳ En Desarrollo
- **💰 Gestión de Préstamos**
  - Registro de nuevos préstamos
  - Seguimiento de pagos
  - Cálculo de intereses

- **📦 Gestión de Artículos**
  - Registro de artículos empeñados
  - Tasación de valores
  - Control de estados

- **👔 Gestión de Asesores**
  - Registro de asesores
  - Asignación de clientes
  - Evaluación de desempeño

## 📸 Capturas de Pantalla

### Login
- Autenticación con ID de persona
- Validación de roles (Administrador/Asesor)

### Dashboard
- 4 tarjetas con estadísticas principales
- Actualización automática desde BD

### Gestión de Clientes
- Tabla con información completa
- Botones para CRUD
- Diálogo modal para crear/editar

### Reportes
- Lista de reportes disponibles
- Vista previa en formato texto
- Exportación a archivos

### Diccionario de Datos
- Árbol de navegación
- Detalles técnicos de objetos
- Información de metadatos

## 🔒 Seguridad
- Validación de credenciales en base de datos
- Control de acceso por roles
- Manejo seguro de sesiones
- Cierre automático de recursos BD

## 🎨 Diseño
- **Paleta de colores:**
  - Azul principal: `#34495e` (52, 73, 94)
  - Azul hover: `#3498db` (52, 152, 219)
  - Verde: `#2ecc71` (46, 204, 113)
  - Rojo: `#e74c3c` (231, 76, 60)
  - Gris fondo: `#ecf0f1` (236, 240, 241)

- **Tipografía:**
  - Arial (interfaz general)
  - Courier New (reportes y datos técnicos)

## 📝 Patrones de Diseño Utilizados
- **Singleton:** ConexionOracle (gestión de conexión única)
- **DAO (Data Access Object):** Separación de lógica de acceso a datos
- **MVC (Model-View-Controller):** Organización de capas
- **SwingWorker:** Procesamiento asíncrono para no bloquear UI

## 🧪 Pruebas

### Test de Conexión
Ejecutar `TestConexion.java` para verificar:
- Conexión a base de datos
- Conteo de registros en tablas principales
- Estado de la conexión

### Usuarios de Prueba
Crear usuarios en la tabla PERSONA con `TIPO_PERSONA` = 'ADMINISTRADOR' o 'ASESOR' para acceder al sistema.

## 📚 Documentación Adicional
- JavaDoc completo en todas las clases
- Comentarios descriptivos en código
- Logs informativos en consola

## 🐛 Solución de Problemas

### Error de conexión a BD
- Verificar que Oracle 21c XE esté ejecutándose
- Verificar credenciales (usuario: USR_PRESTAMOS)
- Verificar URL de conexión (puerto 1521)

### Driver Oracle no encontrado
- Descargar `ojdbc8.jar`
- Agregarlo al classpath del proyecto

### Errores de compilación
- Verificar versión de Java (24)
- Verificar estructura de paquetes
- Limpiar y recompilar proyecto

## 🤝 Contribuciones
Sistema desarrollado como proyecto académico/profesional.

## 📄 Licencia
Proyecto académico - Todos los derechos reservados

## 👨‍💻 Autor
**@henersson**

## 📅 Fecha de Desarrollo
Noviembre 2025

## 📞 Contacto
Para soporte o consultas sobre el sistema, contactar al desarrollador.

---

**Sistema de Préstamos y Casa de Empeño v1.0** - Desarrollado con ❤️ en Java

