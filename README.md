# 📥 Carga Masiva con Botón - Proyecto Spring Boot

Este proyecto permite realizar una **carga masiva de datos** desde un archivo Excel mediante un botón en la interfaz web. El backend está desarrollado en **Spring Boot** y utiliza **Apache POI** para leer archivos `.xlsx`, facilitando la inserción y validación de datos en la base de datos.

---

## 🚀 Tecnologías Usadas

- **Java 17+**
- **Spring Boot**
- **Apache POI** (para manejar archivos Excel)
- **PostgreSQL** (u otra base de datos SQL)
- **Docker** y **Docker Compose**
- **Make** (para automatización)
- **Maven** (para la gestión de dependencias y construcción)
- (Frontend: **Thymeleaf** o **Angular**, según la implementación)

---

## 📁 Estructura del Proyecto

La estructura del proyecto es similar a la siguiente:

```
.
├── src
│   ├── main
│   │   ├── java
│   │   └── resources
│   │       ├── application.yml
│   │       ├── static
│   │       └── templates
├── Dockerfile
├── docker-compose.yml
├── Makefile
└── README.md
```

---

## ⚙️ Instalación y Ejecución

### 1. Clonar el Repositorio

```bash
git clone https://github.com/usuario/carga-masiva-boton.git
cd carga-masiva-boton
```

### 2. Configurar la Base de Datos

Edita el archivo `application.yml` (o `application.properties`) ubicado en `src/main/resources` para configurar los datos de conexión a tu base de datos. Por ejemplo, para PostgreSQL:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tu_basededatos
    username: tu_usuario
    password: tu_contraseña
```

### 3. Levantar el Proyecto

Utiliza el `Makefile` incluido para automatizar el proceso. Ejecuta el siguiente comando:

```bash
make up
```

Este comando:
- Verifica que el puerto `8083` esté disponible.
- Construye la imagen Docker usando Maven.
- Levanta el contenedor con Docker Compose.

---

## 🖱️ Uso del Sistema

1. Accede a la aplicación en: `http://localhost:8083/carga`
2. Selecciona un archivo Excel (`.xlsx`) que contenga la información a cargar.
3. Haz clic en **"Subir y procesar"**.
4. El backend valida e inserta (o actualiza) los datos en la base de datos.
5. Se muestran mensajes de confirmación o error tanto en la interfaz web como en los logs del contenedor.

---

## 📄 Formato Esperado del Archivo Excel

El archivo Excel debe tener un formato similar al siguiente:

| Cédula        | Nombre Completo     | Rubro               | Presupuesto |
|---------------|---------------------|---------------------|-------------|
| 0928228196    | Juan Pérez          | MANUTENCION BECARIO | 798.31      |
| 0951576784    | María Estrella      | MATRÍCULA           | 500.00      |

Asegúrate de que:
- Los campos obligatorios estén completos.
- Los datos sigan el formato definido (especialmente números y fechas).

---

## 🛠️ Makefile: Automatización y Gestión con Docker

El proyecto incluye un `Makefile` para simplificar tareas comunes durante el desarrollo y despliegue.

### Contenido del Makefile

```makefile
# Nombre del contenedor (según docker-compose.yml)
SERVICE=springboot-excel

# Verifica si el puerto 8083 está ocupado
check-port:
	@echo "🔎 Verificando puerto 8083..."
	@if lsof -i :8083 >/dev/null 2>&1; then \
		echo "❌ El puerto 8083 ya está en uso. Detén el proceso o cambia el puerto."; \
		exit 1; \
	else \
		echo "✅ Puerto 8083 disponible."; \
	fi

build:
	@echo "🔨 Construyendo imagen..."
	docker-compose build
	docker run --rm -e MAVEN_OPTS="-Dhttps.protocols=TLSv1.2" -v $(PWD):/app -w /app maven:3.9.4-eclipse-temurin-17 mvn clean package -DskipTests

# Levanta el contenedor con build
up: check-port
	@echo "🚀 Levantando el contenedor..."
	docker-compose up --build
	mvn clean install

# Baja el contenedor
down:
	@echo "🛑 Deteniendo contenedor..."
	docker-compose down

# Reinicia el contenedor (con build)
restart: down up

# Muestra logs en tiempo real
logs:
	docker-compose logs -f $(SERVICE)

# Estado de los contenedores
ps:
	docker-compose ps

# Elimina todo rastro del contenedor e imagen
clean:
	@echo "🧹 Limpiando contenedores e imágenes..."
	docker-compose down --volumes --remove-orphans
	docker rmi -f $$(docker images -q) || true

# Ayuda
help:
	@echo "Comandos disponibles:"
	@echo "  make build     → Construye la imagen"
	@echo "  make up        → Verifica puerto y levanta contenedor"
	@echo "  make down      → Detiene el contenedor"
	@echo "  make restart   → Reinicia todo con build"
	@echo "  make logs      → Muestra los logs en tiempo real"
	@echo "  make ps        → Muestra estado de los servicios"
	@echo "  make clean     → Elimina contenedor, volúmenes e imágenes"
```

### Comandos Principales

- **make build**: Construye la imagen Docker y compila el proyecto con Maven.
- **make up**: Verifica la disponibilidad del puerto 8083, levanta el contenedor y ejecuta la aplicación.
- **make down**: Detiene el contenedor.
- **make restart**: Reinicia el contenedor, realizando primero un down y luego un up.
- **make logs**: Muestra los logs en tiempo real del servicio.
- **make ps**: Muestra el estado de los contenedores.
- **make clean**: Elimina contenedores, volúmenes e imágenes para una limpieza completa.
- **make help**: Muestra la lista de comandos disponibles.

---

## 🧾 Logs y Manejo de Errores

- Los mensajes de validación y errores se muestran en la interfaz de usuario.
- Se pueden revisar en la consola Docker usando el comando `make logs`.
- Los logs del backend facilitan la trazabilidad de cada proceso de carga.

---

---

## 📝 Licencia

Este proyecto se distribuye bajo la licencia MIT.
"# CargaMasiva-main" 
