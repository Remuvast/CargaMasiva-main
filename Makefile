# Nombre del contenedor (de docker-compose.yml)
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
