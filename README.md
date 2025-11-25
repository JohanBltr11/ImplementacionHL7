# Servidor FHIR HAPI - Implementación HL7

Proyecto completo de servidor FHIR usando HAPI FHIR con interfaz gráfica para gestión de recursos. Implementación académica que demuestra los conceptos fundamentales de interoperabilidad en salud mediante el estándar HL7 FHIR R4.

## 📋 Características

- ✅ **Servidor FHIR embebido** (Jetty) con soporte R4
- ✅ **Operaciones CRUD completas** para Patient y Observation
- ✅ **Validación automática** de recursos FHIR R4 usando el validador integrado de HAPI
- ✅ **Sistema de seguridad robusto** con autenticación HTTP Basic y control de acceso basado en roles (RBAC)
- ✅ **Control de acceso granular** basado en ownership (usuarios solo ven sus propios recursos)
- ✅ **Trazabilidad completa** mediante logging de todas las operaciones
- ✅ **Interfaz gráfica (GUI)** con Swing para gestión intuitiva de recursos
- ✅ **Sistema de login/signup** con persistencia en archivo JSON
- ✅ **Almacenamiento en memoria** thread-safe usando ConcurrentHashMap
- ✅ **Validación de IDs únicos** para evitar duplicados
- ✅ **Soporte para IDs personalizados** o generación automática

## 🛠️ Tecnologías Utilizadas

- **Java 23**: Lenguaje de programación
- **HAPI FHIR 7.2.0**: Framework para implementación FHIR
  - `hapi-fhir-base`: API base de HAPI FHIR
  - `hapi-fhir-structures-r4`: Estructuras de datos FHIR R4
  - `hapi-fhir-server`: Servidor RESTful FHIR
  - `hapi-fhir-client`: Cliente FHIR para la GUI
  - `hapi-fhir-validation`: Validador de recursos FHIR
- **Jetty 11.0.20**: Servidor web embebido
- **Swing**: Interfaz gráfica de usuario
- **Maven 3.9+**: Gestión de dependencias y construcción
- **Jackson**: Serialización/deserialización JSON para persistencia de usuarios
- **SLF4J + Logback**: Sistema de logging

## 📦 Requisitos

- **Java 23** (JDK 23)
- **Maven 3.6+** (recomendado 3.9+)
- **Sistema operativo**: Windows, Linux o macOS

## 🚀 Instalación y Compilación

### 1. Clonar o descargar el proyecto

```bash
# Si tienes el proyecto en un repositorio
git clone <url-del-repositorio>
cd ImplementacionHL7
```

### 2. Compilar el proyecto

```bash
mvn clean install
```

Esto compilará el proyecto y generará el JAR en `target/implementacion-hl7-1.0-SNAPSHOT.jar`.

## ▶️ Ejecución

### Opción 1: Usando scripts batch (Windows)

#### Servidor FHIR

```bash
run-server.bat
```

O usando Maven:

```bash
mvn exec:java@server
```

El servidor estará disponible en: **`http://localhost:8080/fhir/`**

#### Interfaz Gráfica (Cliente)

```bash
run-gui.bat
```

O usando Maven:

```bash
mvn exec:java@gui
```

### Opción 2: Ejecución directa con Java

#### Servidor FHIR

```bash
java -cp target/implementacion-hl7-1.0-SNAPSHOT.jar com.example.fhir.FhirServerMain
```

#### Interfaz Gráfica

```bash
java -cp target/implementacion-hl7-1.0-SNAPSHOT.jar com.example.fhir.gui.FhirClientGUI
```

## 🔐 Sistema de Autenticación

El sistema incluye un sistema completo de autenticación y autorización:

### Credenciales por Defecto

- **Administrador**:
  - Usuario: `admin`
  - Contraseña: `admin123`
  - Permisos: **CRUD completo** en todos los recursos

- **Usuario Regular**:
  - Usuario: `user`
  - Contraseña: `user123`
  - Permisos: **Solo lectura** (GET) de sus propios recursos

### Registro de Nuevos Usuarios

La interfaz gráfica permite registrar nuevos usuarios mediante el botón "Registrarse" en la ventana de login. Los usuarios se guardan en el archivo `users.json` en el directorio raíz del proyecto.

### Roles y Permisos

- **`admin`**: 
  - Puede crear, leer, actualizar y eliminar todos los recursos
  - Acceso completo al sistema

- **`user`**:
  - Solo puede leer (GET) sus propios recursos
  - No puede crear, actualizar ni eliminar recursos
  - Los recursos se filtran automáticamente por `patientId` asociado al usuario

## 🖥️ Uso de la Interfaz Gráfica

### 1. Inicio de Sesión

1. Ejecuta el cliente GUI (`run-gui.bat` o `mvn exec:java@gui`)
2. Se abrirá la ventana de login
3. Ingresa tus credenciales o haz clic en "Registrarse" para crear una cuenta nueva
4. Selecciona el servidor (por defecto: `http://localhost:8080/fhir/`)
5. Haz clic en "Iniciar Sesión"

### 2. Conectar al Servidor

1. En la ventana principal, ingresa la URL del servidor (por defecto: `http://localhost:8080/fhir/`)
2. Haz clic en "Conectar"
3. Verifica que aparezca "✓ Conexión exitosa al servidor FHIR" en el área de logs

### 3. Gestionar Patients

**Pestaña "Patient"**:

- **Campos disponibles**:
  - **ID**: Opcional. Si lo dejas vacío, se generará automáticamente. Si lo ingresas, debe ser único.
  - **Apellido** (Family Name): Requerido
  - **Nombre** (Given Name): Requerido
  - **Género**: Selecciona de la lista (male, female, other, unknown)
  - **Fecha de Nacimiento**: Formato YYYY-MM-DD (ej: 1990-01-15)

- **Operaciones disponibles** (según rol):
  - **Crear**: Crea un nuevo Patient
  - **Leer**: Lee un Patient por ID
  - **Actualizar**: Actualiza un Patient existente (solo admin)
  - **Eliminar**: Elimina un Patient (solo admin)
  - **Buscar Todos**: Lista todos los Patients (filtrados por rol)

### 4. Gestionar Observations

**Pestaña "Observation"**:

- **Campos disponibles**:
  - **ID**: Opcional. Si lo dejas vacío, se generará automáticamente. Si lo ingresas, debe ser único.
  - **Patient ID**: ID del Patient asociado
  - **Código**: Código LOINC o personalizado
  - **Valor**: Valor de la observación
  - **Tipo**: Quantity o String
  - **Unidad**: Unidad de medida (si es Quantity)

- **Operaciones disponibles** (según rol):
  - **Crear**: Crea una nueva Observation
  - **Leer**: Lee una Observation por ID
  - **Actualizar**: Actualiza una Observation existente (solo admin)
  - **Eliminar**: Elimina una Observation (solo admin)
  - **Buscar Todas**: Lista todas las Observations (filtradas por rol)

### 5. Ver Logs

- El área inferior de la ventana muestra todas las operaciones realizadas en tiempo real
- Los logs también se guardan en el archivo `fhir-server.log` en el directorio raíz

### 6. Cerrar Sesión

- Haz clic en el botón "Cerrar Sesión" para volver a la ventana de login

## 🌐 Endpoints del Servidor FHIR

### Base URL
```
http://localhost:8080/fhir/
```

### Patient

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| `GET` | `/fhir/Patient/{id}` | Obtener un paciente por ID | Requerida |
| `POST` | `/fhir/Patient` | Crear un nuevo paciente | Solo admin |
| `PUT` | `/fhir/Patient/{id}` | Actualizar un paciente | Solo admin |
| `DELETE` | `/fhir/Patient/{id}` | Eliminar un paciente | Solo admin |
| `GET` | `/fhir/Patient` | Buscar todos los pacientes | Requerida (filtrado por rol) |

### Observation

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| `GET` | `/fhir/Observation/{id}` | Obtener una observación por ID | Requerida |
| `POST` | `/fhir/Observation` | Crear una nueva observación | Solo admin |
| `PUT` | `/fhir/Observation/{id}` | Actualizar una observación | Solo admin |
| `DELETE` | `/fhir/Observation/{id}` | Eliminar una observación | Solo admin |
| `GET` | `/fhir/Observation` | Buscar todas las observaciones | Requerida (filtrado por rol) |

### Metadata

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| `GET` | `/fhir/metadata` | Obtener CapabilityStatement | No requerida |

## 📝 Ejemplos de Uso con cURL

### Crear un Patient (requiere autenticación admin)

```bash
curl -X POST http://localhost:8080/fhir/Patient \
  -H "Content-Type: application/fhir+json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -d '{
    "resourceType": "Patient",
    "id": "paciente-001",
    "name": [{
      "family": "García",
      "given": ["Juan"]
    }],
    "gender": "male",
    "birthDate": "1990-01-15"
  }'
```

### Leer un Patient (requiere autenticación)

```bash
curl -X GET http://localhost:8080/fhir/Patient/paciente-001 \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

### Crear una Observation (requiere autenticación admin)

```bash
curl -X POST http://localhost:8080/fhir/Observation \
  -H "Content-Type: application/fhir+json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -d '{
    "resourceType": "Observation",
    "id": "obs-001",
    "status": "final",
    "code": {
      "coding": [{
        "system": "http://loinc.org",
        "code": "29463-7",
        "display": "Body Weight"
      }]
    },
    "subject": {
      "reference": "Patient/paciente-001"
    },
    "valueQuantity": {
      "value": 75.5,
      "unit": "kg",
      "system": "http://unitsofmeasure.org",
      "code": "kg"
    }
  }'
```

### Buscar todos los Patients (requiere autenticación)

```bash
curl -X GET http://localhost:8080/fhir/Patient \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

**Nota**: Los valores de Authorization son Base64 de `usuario:contraseña`
- `admin:admin123` = `YWRtaW46YWRtaW4xMjM=`
- `user:user123` = `dXNlcjp1c2VyMTIz`

Para generar tu propio token Base64:
```bash
# En Linux/Mac
echo -n "usuario:contraseña" | base64

# En Windows PowerShell
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("usuario:contraseña"))
```

## 📁 Estructura del Proyecto

```
ImplementacionHL7/
├── src/main/java/com/example/fhir/
│   ├── FhirServerMain.java              # Servidor principal (RestfulServer)
│   ├── provider/
│   │   ├── PatientProvider.java         # CRUD para Patient
│   │   └── ObservationProvider.java     # CRUD para Observation
│   ├── storage/
│   │   ├── InMemoryStorage.java         # Almacenamiento en memoria (thread-safe)
│   │   └── UserStorage.java            # Gestión de usuarios (JSON)
│   ├── interceptor/
│   │   ├── SecurityInterceptor.java     # Autenticación y autorización
│   │   └── LoggingInterceptor.java     # Trazabilidad y logging
│   ├── model/
│   │   └── User.java                    # Modelo de usuario
│   ├── util/
│   │   ├── ValidationUtil.java         # Validación de recursos FHIR
│   │   └── AuthContext.java            # Contexto de autenticación (ThreadLocal)
│   └── gui/
│       ├── FhirClientGUI.java          # Interfaz gráfica principal
│       ├── LoginWindow.java            # Ventana de login/signup
│       ├── PatientPanel.java           # Panel para gestión de Patients
│       └── ObservationPanel.java      # Panel para gestión de Observations
├── src/main/resources/
│   └── logback.xml                     # Configuración de logging
├── pom.xml                             # Configuración Maven
├── users.json                          # Base de datos de usuarios (generado)
├── fhir-server.log                     # Archivo de logs (generado)
├── run-server.bat                      # Script para ejecutar servidor
└── run-gui.bat                         # Script para ejecutar GUI
```

## 🔒 Seguridad

### Autenticación

- **Método**: HTTP Basic Authentication
- **Interceptor**: `SecurityInterceptor` se ejecuta en cada petición
- **Validación**: Credenciales se validan contra `UserStorage` (archivo JSON)

### Autorización

- **Control de acceso basado en roles (RBAC)**:
  - Administradores tienen acceso completo
  - Usuarios regulares solo pueden leer sus propios recursos

- **Control de acceso basado en ownership**:
  - Los usuarios regulares solo pueden acceder a recursos asociados a su `patientId`
  - Los administradores pueden acceder a todos los recursos

### Thread Safety

- El almacenamiento usa `ConcurrentHashMap` para garantizar thread-safety
- El contexto de autenticación usa `ThreadLocal` para evitar condiciones de carrera

## 📊 Logging y Trazabilidad

### Archivos de Log

- **`fhir-server.log`**: Archivo de logs en el directorio raíz
- **Consola**: Salida estándar con información detallada

### Información Registrada

Cada operación registra:
- Timestamp
- Tipo de operación (CREATE, READ, UPDATE, DELETE, SEARCH)
- Método HTTP
- Ruta del recurso
- Usuario autenticado
- Rol del usuario
- Estado de la respuesta

## ⚙️ Configuración

### Puerto del Servidor

Por defecto, el servidor corre en el puerto **8080**. Para cambiarlo, modifica `FhirServerMain.java`:

```java
server.setPort(8080); // Cambia este valor
```

### Almacenamiento

- **Actual**: Almacenamiento en memoria (se pierde al reiniciar)
- **Persistencia de usuarios**: Archivo `users.json` (se mantiene entre reinicios)

### Validación

La validación de recursos FHIR se realiza automáticamente usando el validador integrado de HAPI FHIR. Si un recurso no cumple con el estándar R4, se devuelve un error con detalles.

## 🐛 Solución de Problemas

### Error: "Usuario no autenticado"

- Verifica que el servidor esté corriendo
- Verifica que las credenciales sean correctas
- Revisa los logs del servidor para más detalles

### Error: "ID ya existe"

- Los IDs deben ser únicos. Si intentas crear un recurso con un ID que ya existe, recibirás este error.
- Usa un ID diferente o deja el campo vacío para generar uno automáticamente.

### Error: "No tiene permisos"

- Verifica que estés usando una cuenta de administrador para operaciones de escritura
- Los usuarios regulares solo pueden leer sus propios recursos

### El servidor no inicia

- Verifica que el puerto 8080 no esté en uso
- Revisa los logs para ver el error específico
- Asegúrate de tener Java 23 instalado

## 📚 Recursos Adicionales

- [Documentación HAPI FHIR](https://hapifhir.io/)
- [Especificación FHIR R4](https://www.hl7.org/fhir/R4/)
- [HL7 International](https://www.hl7.org/)

## ⚠️ Notas Importantes

- **Almacenamiento en memoria**: Los datos se pierden al reiniciar el servidor. Para producción, considere usar una base de datos real (PostgreSQL, MongoDB, etc.)
- **Seguridad básica**: La autenticación HTTP Basic es solo para demostración. En producción, use OAuth2, JWT o certificados SSL/TLS
- **Validación**: La validación actual es básica. Para producción, considere validación más estricta y perfiles FHIR específicos
- **Escalabilidad**: Este servidor está diseñado para uso académico/demostración. Para producción, considere clustering y balanceo de carga

## 📄 Licencia

Este es un proyecto académico de demostración para fines educativos.

## 👥 Contribuciones

Este proyecto fue desarrollado como parte de un proyecto académico sobre interoperabilidad en salud usando el estándar HL7 FHIR.

---

**Versión**: 1.0-SNAPSHOT  
**Última actualización**: 2025-11-24
