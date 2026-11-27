
# Carga Masiva de Información – Plataforma Informática PUSAK

Aplicación desarrollada con **Spring Boot** para realizar la carga y el rechazo masivo de postulaciones mediante archivos Excel en el módulo de Postulación de Becas de la **Plataforma Informática PUSAK**.

El sistema permite:

- Cargar archivos Excel (`.xlsx`, `.xls`).
- Validar la extensión y el tamaño de los archivos.
- Procesar y validar la información en el backend.
- Registrar la información en la base de datos PostgreSQL.
- Ejecutar procesos de rechazo masivo de postulaciones.
- Mostrar mensajes informativos sobre los resultados del procesamiento.

---

## 1. Descripción General

La aplicación expone una interfaz web desarrollada con **Thymeleaf**, desde donde el usuario puede:

- Ingresar al menú principal.
- Acceder al módulo de **Carga Masiva**.
- Acceder al módulo de **Rechazo Masivo**.
- Seleccionar y procesar archivos Excel.

La aplicación está preparada para ejecutarse en tres ambientes:

- **Local:** desarrollo y pruebas locales.
- **Preproducción (PRE):** validación funcional y técnica previa al despliegue.
- **Producción (PRO):** operación institucional.

La separación de ambientes se realiza mediante perfiles de configuración de Spring Boot y archivos Docker Compose independientes.

---

## 2. Tecnologías Utilizadas

- Java 17
- Spring Boot 3.4.5
- Spring Data JPA
- Spring Web
- Thymeleaf
- Apache POI
- PostgreSQL
- Docker
- Docker Compose
- Maven

---

## 3. Estructura General del Proyecto

```text
CargaMasiva-main/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   │       ├── static/
│   │       │   └── images/
│   │       ├── templates/
│   │       │   └── fragments/
│   │       └── application.properties
│   │
│   └── test/
│
├── Dockerfile
├── pom.xml
├── Makefile
└── README.md
```

**Nota:** Los archivos de configuración específicos de PRE y PRO se administran externamente al repositorio, para evitar publicar parámetros internos y credenciales de conexión.

---

## 4. Configuración de Ambientes

La aplicación utiliza perfiles de Spring Boot para seleccionar la configuración correspondiente a cada ambiente.

### 4.1. Ambiente Local

Perfil: `default`

Archivo base:

```text
src/main/resources/application.properties
```

Configuración principal:

- Puerto de aplicación: 8083.
- Conexión a base de datos local.
- Directorio local para almacenamiento de archivos.
- URL de acceso local.

### 4.2. Ambiente de Preproducción (PRE)

Perfil: `pre`

Archivo de configuración externo:

```text
application-pre.properties
```

Activación del perfil:

```bash
SPRING_PROFILES_ACTIVE=pre
```

Configuración principal:

- Conexión a la base de datos de preproducción.
- Directorio Linux para almacenamiento de archivos.
- URL de acceso al ambiente PUSAK TEST.
- Parámetros de autenticación correspondientes al ambiente de pruebas.

El archivo de configuración debe estar disponible en la ubicación definida para el despliegue.

### 4.3. Ambiente de Producción (PRO)

Perfil: `prod`

Archivo de configuración externo:

```text
application-prod.properties
```

Activación del perfil:

```bash
SPRING_PROFILES_ACTIVE=prod
```

Configuración principal:

- Conexión a la base de datos de Producción.
- Directorio Linux para almacenamiento de archivos.
- URL de acceso al ambiente de Producción de Servicios PUSAK.
- Parámetros de autenticación correspondientes al ambiente productivo.

El archivo `application-prod.properties` debe estar disponible en el servidor de Producción y mantenerse fuera del repositorio.

**Importante:** Las credenciales, contraseñas, tokens y secretos deben administrarse mediante mecanismos seguros y no deben incorporarse al código fuente ni publicarse en Git.

---

## 5. Compilación y Ejecución

### 5.1. Ejecución en Ambiente Local

**1. Compilar el proyecto**

```bash
mvn clean package -DskipTests
```

**2. Construir y levantar el contenedor**

```bash
docker compose up --build -d
```

**3. Acceder al sistema**

Menú principal:

```text
http://localhost:8083/menu
```

Carga masiva:

```text
http://localhost:8083/carga
```

**4. Consultar los registros**

```bash
docker compose logs -f
```

**5. Detener el contenedor**

```bash
docker compose down
```

### 5.2. Ejecución en Preproducción (PRE)

Utilizar el archivo Docker Compose correspondiente al ambiente:

```bash
docker compose -f docker-compose.pre.yml up --build -d
```

Este despliegue activa el perfil:

```bash
SPRING_PROFILES_ACTIVE=pre
```

El servidor debe disponer del archivo externo `application-pre.properties` y de los parámetros de conexión correspondientes a PRE.

### 5.3. Ejecución en Producción (PRO)

Utilizar el archivo Docker Compose correspondiente al ambiente:

```bash
docker compose -f docker-compose.prod.yml up --build -d
```

Este despliegue activa el perfil:

```bash
SPRING_PROFILES_ACTIVE=prod
```

El servidor debe disponer del archivo externo `application-prod.properties`, con los parámetros correspondientes a Producción.

La ubicación del archivo de configuración debe coincidir con la ruta definida en el volumen de `docker-compose.prod.yml`.

**Nota:** La ejecución del despliegue en Producción debe realizarse por personal autorizado, previa aprobación del procedimiento técnico correspondiente.

---

## 6. Uso del Sistema

### 6.1. Menú Principal

Desde la pantalla inicial, el usuario puede seleccionar:

- Carga Masiva.
- Rechazo Masivo.

### 6.2. Carga Masiva

Flujo de uso:

1. Ingresar al módulo de Carga Masiva.
2. Seleccionar el archivo Excel.
3. Presionar el botón **Subir y Procesar**.
4. Esperar la validación y el procesamiento de la información.
5. Revisar el resultado mostrado en pantalla.

### 6.3. Rechazo Masivo

Flujo de uso:

1. Ingresar al módulo de Rechazo Masivo.
2. Seleccionar el archivo correspondiente.
3. Ejecutar el proceso de rechazo.
4. Revisar el resultado del procesamiento.

---

## 7. Validaciones de Archivos

El sistema contempla las siguientes validaciones:

**Extensiones permitidas:**

- `.xlsx`
- `.xls`

**Tamaño máximo permitido:**

- 20 MB.

Si el archivo no cumple las condiciones establecidas, se bloquea el envío y se muestra el mensaje de validación correspondiente.

Las validaciones del procesamiento se realizan en el backend.

Si se identifican errores durante la validación de los registros, el sistema muestra los errores en pantalla y no guarda los registros del archivo procesado.

---

## 8. Ubicación de Archivos Subidos

Los archivos procesados se almacenan físicamente en el servidor, mediante volúmenes Docker.

### Ambiente Local

Directorio dentro del contenedor:

```text
/app/data/archivos_subidos_carga_masiva
```

La ruta física del equipo local se define mediante el volumen configurado en Docker Compose.

### Ambiente de Preproducción

Directorio:

```text
/data/archivos_subidos_carga_masiva
```

### Ambiente de Producción

Directorio:

```text
/data/archivos_subidos_carga_masiva
```

En PRE y PRO se utiliza el volumen Docker correspondiente para mantener los archivos almacenados fuera del ciclo de vida del contenedor.

Se debe verificar que el usuario que ejecuta la aplicación tenga permisos de escritura en el directorio correspondiente.

---

## 9. Recursos Visuales

Las imágenes institucionales se encuentran en:

```text
src/main/resources/static/images/
```

Archivos utilizados:

- `logoizquierda.jpg`
- `logoderecha.jpg`
- `piederecha.png`

Se utilizan en los siguientes fragmentos:

```text
templates/fragments/header.html
templates/fragments/footer.html
```

---

## 10. Docker

### 10.1. Dockerfile

El proyecto utiliza Docker para construir y ejecutar la aplicación.

La construcción se realiza mediante dos etapas:

**Etapa 1: Construcción**

- Compilación del proyecto con Maven.
- Generación del archivo ejecutable JAR.

**Etapa 2: Ejecución**

- Ejecución de la aplicación con Java 17.
- Exposición del puerto 8083.

### 10.2. Docker Compose

Se utilizan archivos independientes para configurar cada ambiente:

| Ambiente | Archivo | Perfil |
|---|---|---|
| Local | `docker-compose.yml` | default |
| Preproducción | `docker-compose.pre.yml` | pre |
| Producción | `docker-compose.prod.yml` | prod |

Los archivos de configuración específicos de cada ambiente deben mantenerse fuera del repositorio.

### 10.3. Comandos útiles

**Consultar contenedores activos:**

```bash
docker ps
```

**Consultar registros de la aplicación:**

```bash
docker logs -f springboot-excel
```

**Reconstruir y levantar el ambiente local:**

```bash
docker compose up --build -d
```

**Detener los servicios locales:**

```bash
docker compose down
```

---

## 11. Makefile

El proyecto incluye automatización básica mediante Makefile.

Comandos disponibles:

| Comando | Descripción |
|---|---|
| `make build` | Construcción del proyecto |
| `make up` | Levantar el contenedor |
| `make down` | Detener el contenedor |
| `make restart` | Reiniciar el servicio |
| `make logs` | Consultar registros |
| `make ps` | Consultar estado de contenedores |
| `make clean` | Limpieza de recursos |

---

## 12. Base de Datos

**Motor utilizado:**

PostgreSQL.

La conexión se configura mediante las siguientes propiedades:

```properties
spring.datasource.url
spring.datasource.username
spring.datasource.password
```

Los parámetros de conexión deben corresponder al ambiente en el que se ejecuta la aplicación.

Las credenciales deben administrarse de manera segura y no deben publicarse en el repositorio.

---

## 13. Observaciones Técnicas

- El frontend está desarrollado con Thymeleaf.
- Los archivos Excel son procesados mediante Apache POI.
- La aplicación utiliza Spring Boot para la gestión de solicitudes y carga de archivos.
- La persistencia de información se realiza mediante Spring Data JPA.
- La base de datos utilizada es PostgreSQL.
- La aplicación soporta separación de configuraciones mediante perfiles de Spring Boot.
- Los archivos cargados se almacenan físicamente en el servidor mediante volúmenes Docker.
- La ejecución se realiza mediante contenedores Docker.

---

## 14. Recomendaciones de Despliegue

Antes de ejecutar un despliegue en cualquiera de los ambientes, se recomienda verificar:

- Disponibilidad del servidor.
- Disponibilidad del puerto 8083.
- Correcta configuración del perfil Spring Boot.
- Existencia y validez del archivo de configuración correspondiente.
- Acceso a la base de datos.
- Correcta resolución de la URL de Servicios PUSAK.
- Configuración de autenticación e integración.
- Permisos de lectura y escritura en los directorios correspondientes.
- Disponibilidad de espacio en disco.
- Correcta construcción y ejecución del contenedor Docker.
- Respaldo de la versión anterior.
- Disponibilidad del procedimiento de reversa (rollback).

Para el ambiente de Producción, el despliegue debe ejecutarse conforme al Estándar de Ejecución aprobado y bajo la responsabilidad del personal autorizado.
