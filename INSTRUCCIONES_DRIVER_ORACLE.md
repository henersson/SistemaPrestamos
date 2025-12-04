# 📦 Instalación del Driver JDBC de Oracle

## ❌ Problema Actual
El sistema no puede conectarse a la base de datos porque falta el driver JDBC de Oracle (ojdbc).

**Error:** `java.lang.ClassNotFoundException: oracle.jdbc.driver.OracleDriver`

---

## ✅ Solución - Instalar el Driver JDBC de Oracle

### Paso 1: Descargar el Driver

Tienes **3 opciones** para obtener el driver:

#### **Opción A: Descargar desde Oracle (Recomendado)**
1. Visita: https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html
2. Busca la sección **"Oracle Database 21c"**
3. Descarga el archivo: **`ojdbc8.jar`** o **`ojdbc11.jar`** (para Java 11+)
4. **Nota:** Puede requerir una cuenta Oracle gratuita

#### **Opción B: Descargar desde Maven Central**
1. Visita: https://mvnrepository.com/artifact/com.oracle.database.jdbc/ojdbc8
2. Selecciona la versión más reciente (ej: 21.9.0.0 o superior)
3. Descarga el archivo `.jar`

#### **Opción C: Usar Maven (Alternativa)**
Si prefieres usar Maven, puedes agregar esta dependencia:
```xml
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc8</artifactId>
    <version>21.9.0.0</version>
</dependency>
```

---

### Paso 2: Copiar el Driver a la Carpeta lib

1. Una vez descargado el archivo **`ojdbc8.jar`** (o `ojdbc11.jar`)
2. Cópialo a la carpeta:
   ```
   C:\Users\DELTA\Desktop\SistemaPrestamos\SistemaPrestamos\lib\
   ```

**Ejemplo de comando en PowerShell:**
```powershell
Copy-Item "C:\Users\DELTA\Downloads\ojdbc8.jar" -Destination "C:\Users\DELTA\Desktop\SistemaPrestamos\SistemaPrestamos\lib\"
```

---

### Paso 3: Configurar IntelliJ IDEA

#### **Método A: Agregar al Module Path**
1. Abre IntelliJ IDEA
2. Ve a **File → Project Structure** (o presiona `Ctrl + Alt + Shift + S`)
3. Selecciona **Modules** en el panel izquierdo
4. Selecciona tu módulo **SistemaPrestamos**
5. Ve a la pestaña **Dependencies**
6. Haz clic en el botón **`+`** (Add)
7. Selecciona **JARs or directories...**
8. Navega a: `C:\Users\DELTA\Desktop\SistemaPrestamos\SistemaPrestamos\lib`
9. Selecciona **`ojdbc8.jar`**
10. Haz clic en **OK** y luego en **Apply**

#### **Método B: Agregar como Library**
1. Abre IntelliJ IDEA
2. Ve a **File → Project Structure**
3. Selecciona **Libraries** en el panel izquierdo
4. Haz clic en el botón **`+`** (New Project Library)
5. Selecciona **Java**
6. Navega a: `C:\Users\DELTA\Desktop\SistemaPrestamos\SistemaPrestamos\lib`
7. Selecciona **`ojdbc8.jar`**
8. Dale un nombre: **Oracle JDBC Driver**
9. Haz clic en **OK** y luego en **Apply**

---

### Paso 4: Verificar la Instalación

Ejecuta el archivo **`TestConexion.java`** nuevamente:

1. En IntelliJ, abre `TestConexion.java`
2. Haz clic derecho → **Run 'TestConexion.main()'**
3. Deberías ver:
   ```
   ✓ Driver Oracle cargado correctamente
   ✓ Conexión establecida exitosamente con Oracle 21c XE
   ```

---

## 📋 Verificación Rápida

Después de instalar el driver, verifica que el archivo esté en la ubicación correcta:

```powershell
# Verificar que el archivo existe
Test-Path "C:\Users\DELTA\Desktop\SistemaPrestamos\SistemaPrestamos\lib\ojdbc8.jar"
```

Debe retornar: **True**

---

## ⚠️ Solución de Problemas

### Error: "Driver de Oracle no encontrado"
- **Causa:** El driver no está en el classpath
- **Solución:** Verifica que hayas agregado el JAR correctamente en IntelliJ (Paso 3)

### Error: "ORA-01017: invalid username/password"
- **Causa:** Credenciales incorrectas
- **Solución:** Verifica en `ConexionOracle.java`:
  - Usuario: `USR_PRESTAMOS`
  - Password: `Prestamos2025`

### Error: "IO Error: The Network Adapter could not establish the connection"
- **Causa:** Oracle no está corriendo o configuración incorrecta
- **Solución:** 
  - Verifica que Oracle 21c XE esté corriendo
  - Verifica la URL: `jdbc:oracle:thin:@localhost:1521/XEPDB1`

---

## 📚 Información Adicional

- **Versión de Java del proyecto:** Java 24
- **Base de datos:** Oracle 21c XE
- **URL de conexión:** `jdbc:oracle:thin:@localhost:1521/XEPDB1`
- **Usuario:** `USR_PRESTAMOS`
- **Schema/Pluggable DB:** XEPDB1

---

## ✨ Siguiente Paso

Una vez instalado el driver, podrás:
1. ✅ Ejecutar `TestConexion.java` sin errores
2. ✅ Ejecutar `Main.java` para iniciar la aplicación
3. ✅ Conectarte a la base de datos desde la interfaz gráfica

---

**¿Necesitas ayuda?**
Si tienes problemas, verifica:
1. ✓ El archivo `ojdbc8.jar` está en la carpeta `lib`
2. ✓ El JAR está agregado en IntelliJ Project Structure
3. ✓ Oracle 21c XE está corriendo
4. ✓ El usuario `USR_PRESTAMOS` existe en la base de datos

