# ITPCARGO — Sistema de Gestión de Despachos
 
Sistema de gestión de despachos desarrollado con arquitectura de microservicios, desplegado en AWS mediante contenedores Docker y automatizado con GitHub Actions.
 
---
 
## Tecnologías utilizadas
 
- **Frontend:** React + Vite + Tailwind CSS + Nginx
- **Backend Ventas:** Spring Boot 3 + Java 17 + JPA/Hibernate
- **Backend Despachos:** Spring Boot 3 + Java 17 + JPA/Hibernate
- **Base de datos:** MySQL 8 (Docker)
- **Infraestructura:** AWS (ECS Fargate, ECR, EC2, VPC)
- **CI/CD:** GitHub Actions
- **Contenedorización:** Docker + Docker Compose
 
---
 
## Arquitectura
Internet ↓ Internet Gateway ↓ Subredes Públicas (us-east-1a / us-east-1b) → ECS Fargate (Frontend + Backend Ventas + Backend Despachos) → NAT Gateway ↓ Subred Privada (us-east-1a) → EC2 MySQL (db_ventas + db_despachos)

 
El frontend actúa como proxy hacia los backends mediante Nginx:
- `/api/ventas/*` → Backend Ventas (puerto 8080)
- `/api/despachos/*` → Backend Despachos (puerto 8081)
 
---
 
## Requisitos previos
 
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Node.js 20](https://nodejs.org/)
- [Java 17](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/)
- [AWS CLI](https://aws.amazon.com/cli/)
- [Terraform](https://www.terraform.io/)
 
---
 
## Ejecución local con Docker Compose
 
### 1. Clonar el repositorio
 
```bash
git clone <url-del-repositorio>
cd <nombre-del-repositorio>
```
 
### 2. Configurar variables de entorno
 
Crea un archivo `.env` en la raíz del proyecto:
 
```env
MYSQL_ROOT_PASSWORD=secreto123
MYSQL_DATABASE_VENTAS=db_ventas
MYSQL_DATABASE_DESPACHOS=db_despachos
```
 
### 3. Levantar los servicios
 
```bash
docker-compose up --build
```
 
### 4. Acceder a la aplicación
 
| Servicio | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Backend Ventas | http://localhost:8080 |
| Backend Despachos | http://localhost:8081 |
| Swagger Ventas | http://localhost:8080/swagger-ui.html |
| Swagger Despachos | http://localhost:8081/swagger-ui.html |
 
### 5. Detener los servicios
 
```bash
docker-compose down
 
# Para eliminar también los volúmenes (borra la BD)
docker-compose down -v
```
 
---
 
## Ejecución de tests
 
### Backend Ventas
 
```bash
cd back-Ventas_SpringBoot/Springboot-API-REST
set JAVA_HOME=C:\Program Files\Java\jdk-17  # Windows
export JAVA_HOME=/usr/lib/jvm/java-17       # Linux/Mac
mvn test
```
 
### Backend Despachos
 
```bash
cd back-Despachos_SpringBoot/Springboot-API-REST-DESPACHO
mvn test
```
 
### Frontend
 
```bash
cd front_despacho
npm install
npm run test
```
 
---
 
## Despliegue en AWS
 
### Requisitos previos AWS
 
- Cuenta AWS Academy con laboratorio activo
- AWS CLI configurado con credenciales del lab
- Terraform instalado
 
### 1. Configurar credenciales AWS
 
```bash
aws configure
```
 
Ingresa las credenciales desde **AWS Academy → Start Lab → AWS Details**.
 
### 2. Desplegar infraestructura con Terraform
 
```bash
cd terraform
terraform init
terraform apply
```
 
Esto crea:
- VPC con subredes públicas y privada
- NAT Gateway
- ECS Cluster con Fargate
- EC2 con MySQL en subred privada
- Repositorios ECR
- Security Groups
- CloudWatch Log Groups
 
### 3. Construir y publicar imágenes en ECR
 
```bash
# Login a ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com
 
# Backend Ventas
docker buildx build --platform linux/amd64 \
  -t <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/entrega-devops-backend-ventas:latest \
  ./back-Ventas_SpringBoot/Springboot-API-REST --push
 
# Backend Despachos
docker buildx build --platform linux/amd64 \
  -t <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/entrega-devops-backend-despachos:latest \
  ./back-Despachos_SpringBoot/Springboot-API-REST-DESPACHO --push
 
# Frontend
docker buildx build --platform linux/amd64 \
  -t <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/entrega-devops-frontend:latest \
  ./front_despacho --push
```
 
### 4. Forzar redespliegue en ECS
 
```bash
aws ecs update-service \
  --cluster entrega-devops-cluster \
  --service app \
  --force-new-deployment \
  --no-cli-pager
```
 
### 5. Obtener IP pública del frontend
 
Ve a la consola de AWS:
**ECS → entrega-devops-cluster → app → Tasks → tarea activa → Network → IP pública**
 
---
 
## Pipeline CI/CD
 
### CI (Integración Continua)
 
Se activa automáticamente con cada push o pull request a la rama `develop`.
 
**Pasos:**
1. Build del frontend (`npm install` + `npm run build` + `npm run test`)
2. Build del backend ventas (`mvn clean install`)
3. Build del backend despachos (`mvn clean install`)
 
### CD (Despliegue Continuo)
 
Se activa automáticamente con cada push a la rama `develop`.
 
**Pasos:**
1. Configurar credenciales AWS desde GitHub Secrets
2. Login en ECR
3. Build y push de las tres imágenes Docker
4. Forzar redespliegue en ECS
 
### GitHub Secrets requeridos
 
| Secret | Descripción |
|---|---|
| `AWS_ACCESS_KEY_ID` | Credencial AWS (se renueva con cada lab) |
| `AWS_SECRET_ACCESS_KEY` | Credencial AWS (se renueva con cada lab) |
| `AWS_SESSION_TOKEN` | Token de sesión AWS (se renueva con cada lab) |
| `AWS_ACCOUNT_ID` | ID de la cuenta AWS |
 
> ⚠️ Las credenciales de AWS Academy expiran con cada sesión de laboratorio. Actualiza los secrets en GitHub cada vez que inicies un nuevo lab.
 
---
 
## Abrir el proyecto en otro equipo
 
```bash
# 1. Iniciar lab en AWS Academy y actualizar secrets en GitHub
 
# 2. Clonar el repositorio
git clone <url-del-repositorio>
 
# 3. Configurar credenciales AWS
aws configure
 
# 4. Recrear infraestructura
cd terraform
terraform init
terraform apply
 
# 5. Forzar redespliegue (reconstruye y sube las imágenes)
git commit --allow-empty -m "chore: forzar redespliegue"
git push origin develop
 
# 6. Esperar 3-4 minutos y buscar la IP pública en ECS
```
 
---
```
## Estructura del proyecto
proyecto-semestral/
├── .github/
│   └── workflows/
│       ├── ci.yml                          # Pipeline de integración continua
│       └── cd.yml                          # Pipeline de despliegue continuo
│
├── back-Ventas_SpringBoot/
│   └── Springboot-API-REST/
│       ├── src/main/resources/
│       │   └── application.properties      # Configuración Spring Boot y BD
│       ├── Dockerfile                      # Imagen Docker multi-stage (Maven + JDK)
│       └── entrypoint.sh                   # Script de arranque del contenedor
│
├── back-Despachos_SpringBoot/
│   └── Springboot-API-REST-DESPACHO/
│       ├── src/main/resources/
│       │   └── application.properties      # Configuración Spring Boot y BD
│       ├── Dockerfile                      # Imagen Docker multi-stage (Maven + JDK)
│       └── entrypoint.sh                   # Script de arranque del contenedor
│
├── front_despacho/
│   ├── nginx.conf                          # Configuración Nginx + proxy backends
│   └── Dockerfile                          # Imagen Docker multi-stage (Node + Nginx)
│
├── terraform/
│   ├── main.tf                             # Provider AWS y región
│   ├── vpc.tf                              # VPC, IGW, NAT Gateway, route tables
│   ├── instances.tf                        # EC2 MySQL + CloudWatch Log Group
│   ├── ecr.tf                              # Repositorios ECR
│   ├── ecs.tf                              # ECS Cluster + IAM Role
│   ├── service.tf                          # ECS Service Fargate
│   ├── task_app.tf                         # ECS Task Definition (3 contenedores)
│   ├── security_groups.tf                  # Security Groups ECS y MySQL
│   ├── variables.tf                        # Variables del proyecto
│   └── outputs.tf                          # URLs ECR e IP privada MySQL
│
├── docker-compose.yml                      # Ejecución local de todos los servicios
├── .env                                    # Variables de entorno locales
└── README.md
```
 
---
 
## Endpoints principales
 
### Backend Ventas (`/api/ventas/v1`)
 
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/ventas` | Obtener todas las ventas |
| GET | `/ventas/{id}` | Obtener venta por ID |
| POST | `/ventas` | Crear nueva venta |
| PUT | `/ventas/{id}` | Actualizar venta |
| DELETE | `/ventas/{id}` | Eliminar venta |
 
### Backend Despachos (`/api/despachos/v1`)
 
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/despachos` | Obtener todos los despachos |
| GET | `/despachos/{id}` | Obtener despacho por ID |
| POST | `/despachos` | Crear nuevo despacho |
| PUT | `/despachos/{id}` | Actualizar despacho |
| DELETE | `/despachos/{id}` | Eliminar despacho |
 
---
 
## Persistencia de datos
 
Los datos de MySQL se persisten mediante un **named volume** de Docker (`mysql_data`), lo que garantiza que la información no se pierda al reiniciar los contenedores.
 
Se eligió **named volume** sobre bind mount porque:
- Es gestionado completamente por Docker
- Es portable entre distintos sistemas operativos
- No depende de rutas absolutas del sistema host
 
---
 
## Diagrama de arquitectura

![Diagrama de arquitectura AWS](./assets/Diagrama%20de%20Arquitectura.png)