# Carga Masiva de Informaci¨®n ¨C Plataforma Inform¨¢tica PUSAK

Aplicaci¨®n desarrollada en **Spring Boot** para realizar la **carga masiva de informaci¨®n desde archivos Excel** en el m¨®dulo de **Postulaci¨®n de Becas** de la **Plataforma Inform¨¢tica PUSAK**.

El sistema permite:

- Cargar archivos Excel (`.xlsx`, `.xls`)
- Validar extensi¨®n y tama?o del archivo
- Procesar la informaci¨®n en backend
- Registrar los datos en base de datos PostgreSQL
- Mostrar mensajes de resultado en interfaz web

---

## Descripci¨®n General

La aplicaci¨®n expone una interfaz web construida con **Thymeleaf**, desde donde el usuario puede:

- Ingresar al men¨² principal
- Acceder al m¨®dulo de **Carga Masiva**
- Acceder al m¨®dulo de **Rechazo Masivo**
- Subir archivos Excel para procesamiento

El sistema est¨¢ preparado para ejecutarse en:

- **Ambiente local**
- **Ambiente de preproducci¨®n**

mediante perfiles de configuraci¨®n de **Spring Boot**.

---

## Tecnolog¨ªas Utilizadas

- **Java 17**
- **Spring Boot 3.4.5**
- **Spring Data JPA**
- **Spring Web**
- **Thymeleaf**
- **Apache POI**
- **PostgreSQL**
- **Docker**
- **Docker Compose**
- **Maven**

---

## Estructura General del Proyecto

CargaMasiva-main/
©À©¤©¤ src/
©¦   ©À©¤©¤ main/
©¦   ©¦   ©À©¤©¤ java/
©¦   ©¦   ©¸©¤©¤ resources/
©¦   ©¦       ©À©¤©¤ static/
©¦   ©¦       ©¦   ©¸©¤©¤ images/
©¦   ©¦       ©À©¤©¤ templates/
©¦   ©¦       ©¦   ©À©¤©¤ fragments/
©¦   ©¦       ©¦   ©À©¤©¤ CargaInformacion.html
©¦   ©¦       ©¦   ©À©¤©¤ IndexCargaInformacion.html
©¦   ©¦       ©¦   ©¸©¤©¤ RechazoInformacion.html
©¦   ©¦       ©À©¤©¤ application.properties
©¦   ©¦       ©¸©¤©¤ application-pre.properties
©¦   ©¸©¤©¤ test/
©À©¤©¤ Dockerfile
©À©¤©¤ docker-compose.yml
©À©¤©¤ docker-compose.pre.yml
©À©¤©¤ Makefile
©¸©¤©¤ pom.xml



## Configuraci¨®n de Ambientes
## Ambiente Local

Archivo:

src/main/resources/application.properties

**Configuraci¨®n principal:**

Puerto: 8083
Base de datos local
Directorio de carga local
URL de login local


## Ambiente Preproducci¨®n

Archivo:

src/main/resources/application-pre.properties

Se activa mediante:

SPRING_PROFILES_ACTIVE=pre

**Configuraci¨®n principal:**

Base de datos de preproducci¨®n
Ruta Linux para archivos cargados
URL de login de ambiente PUSAK TEST
Ejecuci¨®n en Ambiente Local
**1. Compilar el proyecto
mvn clean package -DskipTests
**2. Ejecutar con Docker
docker-compose up --build
**3. Acceder al sistema

Men¨² principal:

http://localhost:8083/menu

Carga masiva:

http://localhost:8083/carga

**Ejecuci¨®n en Preproducci¨®n**

Utilizar:

docker-compose -f docker-compose.pre.yml up --build

Este despliegue utiliza autom¨¢ticamente el perfil:

pre

**Uso del Sistema**
Men¨² principal

Desde la pantalla inicial el usuario puede seleccionar:

Carga Masiva
Rechazo Masivo
Carga Masiva

Flujo de uso:

Ingresar al m¨®dulo
Seleccionar archivo Excel
Presionar Subir y Procesar
Esperar validaci¨®n y procesamiento
Revisar resultado mostrado en pantalla
Validaciones de Archivo

El sistema valida en frontend:

Extensiones permitidas
.xlsx
.xls
Tama?o m¨¢ximo
20 MB

Si el archivo no cumple las reglas, se bloquea el env¨ªo y se muestra mensaje de validaci¨®n.

**Ubicaci¨®n de Archivos Subidos**
Local
/app/data/archivos_subidos_carga_masiva

Mapeado desde Docker hacia la carpeta local definida en:

docker-compose.yml
Preproducci¨®n
/data/archivos_subidos_carga_masiva

**Recursos Visuales**

Las im¨¢genes institucionales se encuentran en:

src/main/resources/static/images/

Archivos actuales:

logoizquierda.jpg
logoderecha.jpg
piederecha.png

Se utilizan en los fragmentos:

templates/fragments/header.html
templates/fragments/footer.html

## Docker
**Dockerfile**

El proyecto usa construcci¨®n por etapas:

Etapa 1

Compilaci¨®n con Maven

Etapa 2

Ejecuci¨®n con Java 17

Puerto expuesto:

8083

**Docker Compose**
**Local**

Archivo:

docker-compose.yml

Contenedor:

springboot-excel
Preproducci¨®n

Archivo:

docker-compose.pre.yml

Contenedor:

springboot-excel-pre
Comandos ¨²tiles
Levantar contenedor local
docker-compose up --build
Detener contenedor
docker-compose down
Ver logs
docker-compose logs -f

**Levantar preproducci¨®n**
docker-compose -f docker-compose.pre.yml up --build
Makefile

El proyecto incluye automatizaci¨®n b¨¢sica mediante Makefile.

Comandos disponibles
Construcci¨®n
make build
Levantar contenedor
make up
Detener contenedor
make down
Reiniciar
make restart
Ver logs
make logs
Estado de contenedores
make ps
Limpieza
make clean

## Base de Datos
**Motor utilizado:**

PostgreSQL

**Configurado mediante:**

spring.datasource.url
spring.datasource.username
spring.datasource.password
Observaciones T¨¦cnicas
El frontend est¨¢ construido con Thymeleaf
Los archivos Excel son procesados con Apache POI
El sistema utiliza Spring Boot multipart upload
El proyecto soporta separaci¨®n de configuraci¨®n por perfiles
Las cargas quedan persistidas f¨ªsicamente en disco
Recomendaciones de Despliegue

Antes de pasar a preproducci¨®n se recomienda verificar:

Acceso a base de datos
Permisos de escritura en /data
Disponibilidad del puerto 8083
Correcta resoluci¨®n de URL del sistema PUSAK
