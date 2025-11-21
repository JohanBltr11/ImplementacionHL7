# Servidor FHIR HAPI - Implementación HL7

Proyecto completo de servidor FHIR usando HAPI FHIR con interfaz gráfica para gestión de recursos.

## Características

- ✅ Servidor FHIR embebido (Jetty)
- ✅ Operaciones CRUD para Patient y Observation
- ✅ Validación automática de recursos FHIR R4
- ✅ Seguridad con autenticación HTTP Basic y control de roles
- ✅ Trazabilidad mediante logging de operaciones
- ✅ Interfaz gráfica (GUI) para gestión de recursos
- ✅ Almacenamiento en memoria (thread-safe)

## Requisitos

- Java 23
- Maven 3.6+

## Compilación

```bash
mvn clean install
```

## Ejecución

### Servidor FHIR

```bash
# Opción 1: Usando Maven
mvn exec:java

# Opción 2: Directamente
java -cp target/implementacion-hl7-1.0-SNAPSHOT.jar com.example.fhir.FhirServerMain
```

El servidor estará disponible en: `http://localhost:8080/fhir/`

### Interfaz Gráfica (Cliente)

```bash
# Opción 1: Usando Maven
mvn exec:java@gui

# Opción 2: Directamente
java -cp target/implementacion-hl7-1.0-SNAPSHOT.jar com.example.fhir.gui.FhirClientGUI
```

## Credenciales

- **Admin**: `admin` / `admin123` (permisos completos - CRUD)
- **User**: `user` / `user123` (solo lectura - GET)

## Uso de la Interfaz Gráfica

1. **Conectar al servidor**:
   - Ingrese la URL del servidor (por defecto: `http://localhost:8080/fhir/`)
   - Ingrese usuario y contraseña
   - Seleccione el rol
   - Haga clic en "Conectar"

2. **Gestionar Patients**:
   - Pestaña "Patient"
   - Complete los campos (ID, Apellido, Nombre, Género, Fecha de Nacimiento)
   - Use los botones: Crear, Leer, Actualizar, Eliminar, Buscar Todos

3. **Gestionar Observations**:
   - Pestaña "Observation"
   - Complete los campos (ID, Patient ID, Código, Valor, Tipo, Unidad)
   - Use los botones: Crear, Leer, Actualizar, Eliminar, Buscar Todas

4. **Ver Logs**:
   - El área inferior muestra todas las operaciones realizadas
   - También se guardan en el archivo `fhir-server.log`

## Endpoints del Servidor

### Patient
- `GET /fhir/Patient/{id}` - Obtener paciente
- `POST /fhir/Patient` - Crear paciente
- `PUT /fhir/Patient/{id}` - Actualizar paciente
- `DELETE /fhir/Patient/{id}` - Eliminar paciente
- `GET /fhir/Patient` - Buscar todos los pacientes

### Observation
- `GET /fhir/Observation/{id}` - Obtener observación
- `POST /fhir/Observation` - Crear observación
- `PUT /fhir/Observation/{id}` - Actualizar observación
- `DELETE /fhir/Observation/{id}` - Eliminar observación
- `GET /fhir/Observation` - Buscar todas las observaciones

## Ejemplo de Uso con cURL

### Crear un Patient (requiere autenticación admin)

```bash
curl -X POST http://localhost:8080/fhir/Patient \
  -H "Content-Type: application/fhir+json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -d '{
    "resourceType": "Patient",
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
curl -X GET http://localhost:8080/fhir/Patient/Patient-1234567890 \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

**Nota**: Los valores de Authorization son Base64 de `usuario:contraseña`
- `admin:admin123` = `YWRtaW46YWRtaW4xMjM=`
- `user:user123` = `dXNlcjp1c2VyMTIz`

## Estructura del Proyecto

```
src/main/java/com/example/fhir/
├── FhirServerMain.java          # Servidor principal
├── provider/
│   ├── PatientProvider.java     # CRUD para Patient
│   └── ObservationProvider.java # CRUD para Observation
├── storage/
│   └── InMemoryStorage.java     # Almacenamiento en memoria
├── interceptor/
│   ├── SecurityInterceptor.java  # Autenticación y autorización
│   └── LoggingInterceptor.java  # Trazabilidad
├── util/
│   └── ValidationUtil.java      # Validación de recursos
└── gui/
    ├── FhirClientGUI.java       # Interfaz gráfica principal
    ├── PatientPanel.java        # Panel para Patient
    └── ObservationPanel.java    # Panel para Observation
```

## Logs

Los logs se guardan en:
- Archivo: `fhir-server.log` (en el directorio raíz)
- Consola: Salida estándar

## Notas

- El almacenamiento es en memoria, por lo que los datos se pierden al reiniciar el servidor
- Para producción, considere usar una base de datos real
- La seguridad básica es solo para demostración; en producción use OAuth2 o JWT

## Licencia

Este es un proyecto académico de demostración.

