# ITPCARGO — Sistema de Gestión de Despachos

Sistema de gestión de despachos desarrollado con arquitectura de microservicios, contenedorizado con Docker y desplegado en AWS mediante **EKS (Kubernetes)**, con infraestructura gestionada por Terraform y automatización CI/CD con GitHub Actions.

---

## Tecnologías utilizadas

| Capa | Tecnología |
|---|---|
| Frontend | React + Vite + Tailwind CSS + Nginx |
| Backend Ventas | Spring Boot 3 + Java 17 + JPA/Hibernate |
| Backend Despachos | Spring Boot 3 + Java 17 + JPA/Hibernate |
| Base de datos | MySQL 8 (contenedor Kubernetes) |
| Contenedorización | Docker + Docker Compose |
| Orquestación | Kubernetes (AWS EKS) |
| Registro de imágenes | AWS ECR |
| Infraestructura como código | Terraform |
| Red | AWS VPC, Subnets, Internet Gateway, Security Groups |
| CI/CD | GitHub Actions |
| Monitoreo | AWS CloudWatch |

---

## Arquitectura

![Diagrama de arquitectura AWS](./assets/Diagrama%20de%20Actualizado.drawio.png)

```
Internet
   ↓
Internet Gateway
   ↓
VPC (10.0.0.0/16)
├── Subnet pública us-east-1a (10.0.1.0/24)
└── Subnet pública us-east-1b (10.0.2.0/24)
        ↓
   EKS Cluster (entrega-devops-cluster)
   ├── Node Group: 2x t3.medium (ON_DEMAND)
   ├── Deployment: frontend          (1–3 réplicas) → Service LoadBalancer :80
   ├── Deployment: backend-ventas    (1–4 réplicas) → Service ClusterIP :8080
   ├── Deployment: backend-despachos (1–4 réplicas) → Service ClusterIP :8081
   └── Deployment: mysql             (1 réplica)    → Service ClusterIP :3306
```

El frontend actúa como proxy hacia los backends mediante Nginx:
- `/api/ventas/*` → `backend-ventas:8080`
- `/api/despachos/*` → `backend-despachos:8081`

---

## Infraestructura AWS con Terraform

La infraestructura se define completamente como código en `infra/tf/`.

### Recursos creados

| Archivo | Recursos |
|---|---|
| `vpc.tf` | VPC, 2 subnets públicas, Internet Gateway, route tables |
| `eks.tf` | Cluster EKS + Node Group (t3.medium, 1–3 nodos) |
| `ecr.tf` | Repositorios ECR para las 3 imágenes |
| `security_groups.tf` | Security Groups para EKS |
| `variables.tf` | Variables del proyecto |
| `outputs.tf` | URLs ECR y datos del cluster |

Las subnets están etiquetadas con `kubernetes.io/role/elb = 1` para permitir la creación automática de Load Balancers por parte de Kubernetes.

### Desplegar infraestructura

```bash
cd infra/tf
terraform init
terraform apply
```

---

## Contenedorización con Docker

Cada servicio tiene su propio `Dockerfile` con build multi-stage para optimizar el tamaño de las imágenes.

### Construir y publicar imágenes en ECR

```bash
# Login a ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com

# Build y push de todas las imágenes
docker compose build
docker compose push
```

### Ejecución local con Docker Compose

```bash
# 1. Crear archivo .env en la raíz
cat > .env << EOF
MYSQL_ROOT_PASSWORD=secreto123
MYSQL_DATABASE_VENTAS=db_ventas
MYSQL_DATABASE_DESPACHOS=db_despachos
EOF

# 2. Levantar todos los servicios
docker-compose up --build
```

| Servicio | URL local |
|---|---|
| Frontend | http://localhost:3000 |
| Backend Ventas | http://localhost:8080 |
| Backend Despachos | http://localhost:8081 |
| Swagger Ventas | http://localhost:8080/swagger-ui.html |
| Swagger Despachos | http://localhost:8081/swagger-ui.html |

```bash
# Detener servicios
docker-compose down

# Detener y eliminar volúmenes (borra la BD)
docker-compose down -v
```

---

## Orquestación con Kubernetes

Los manifiestos se encuentran en `infra/k8s/` y definen todos los recursos del cluster.

### Estructura de manifiestos

```
infra/k8s/
├── secrets.yml           # Secret con credenciales de BD
├── mysql.yml             # Deployment + Service ClusterIP (MySQL 8)
├── backend-ventas.yml    # Deployment (2 réplicas) + Service ClusterIP
├── backend-despachos.yml # Deployment (2 réplicas) + Service ClusterIP
├── frontend.yml          # Deployment (1 réplica) + Service LoadBalancer
└── autoscaling.yml       # HorizontalPodAutoscaler para los 3 servicios
```

### Recursos por servicio

| Servicio | CPU request/limit | Memoria request/limit | Réplicas base |
|---|---|---|---|
| frontend | 100m / 200m | 128Mi / 256Mi | 1 |
| backend-ventas | 250m / 500m | 512Mi / 1Gi | 2 |
| backend-despachos | 250m / 500m | 512Mi / 1Gi | 2 |
| mysql | 250m / 500m | 512Mi / 1Gi | 1 |

### Secrets

Las credenciales de base de datos se gestionan mediante un `Secret` de Kubernetes (`app-secrets`) referenciado por los deployments mediante `secretKeyRef`, evitando hardcodear valores sensibles en los manifiestos.

### Aplicar manifiestos manualmente

```bash
# Configurar kubectl
aws eks update-kubeconfig --region us-east-1 --name entrega-devops-cluster

# Aplicar en orden
kubectl apply -f infra/k8s/secrets.yml
kubectl apply -f infra/k8s/mysql.yml
kubectl apply -f infra/k8s/backend-ventas.yml
kubectl apply -f infra/k8s/backend-despachos.yml
kubectl apply -f infra/k8s/frontend.yml
kubectl apply -f infra/k8s/autoscaling.yml

# Verificar estado
kubectl get pods
kubectl get svc
kubectl get hpa
```

### Obtener URL pública del frontend

```bash
kubectl get svc frontend -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
```

---

## Autoescalado Horizontal (HPA)

Se configuran `HorizontalPodAutoscaler` para los tres servicios principales. El escalado se activa por CPU o memoria.

| Servicio | Mín. pods | Máx. pods | CPU target | Mem target |
|---|---|---|---|---|
| frontend | 1 | 3 | 50% | 50% |
| backend-ventas | 1 | 4 | 50% | 70% |
| backend-despachos | 1 | 4 | 50% | 70% |

El Metrics Server se instala automáticamente durante el pipeline de CD para habilitar el HPA.

---

## Pipeline CI/CD

### CI — Integración Continua

Se activa con cada `push` o `pull_request` a la rama `develop`.

```
push/PR → develop
    ├── Job: frontend-build
    │       npm install → npm run build → npm run test
    ├── Job: backend-ventas-build
    │       mvn clean install (build + tests)
    └── Job: backend-despachos-build
            mvn clean install (build + tests)
```

### CD — Despliegue Continuo

Se activa con cada `push` a la rama `deploy`.

```
push → deploy
    ├── 1. Configurar credenciales AWS (desde GitHub Secrets)
    ├── 2. Login en ECR
    ├── 3. docker compose build && docker compose push
    ├── 4. aws eks update-kubeconfig
    ├── 5. Instalar Metrics Server (para HPA)
    ├── 6. kubectl apply -f infra/k8s/*.yml
    ├── 7. kubectl rollout restart (forzar redeploy)
    ├── 8. kubectl rollout status (esperar readiness)
    ├── 9. Mostrar URL pública + estado HPA
    ├── 10. Evidencia de logs (kubectl + CloudWatch)
    └── 11. Simulación de recuperación automática (pod deletion test)
```

### GitHub Secrets requeridos

| Secret | Descripción |
|---|---|
| `AWS_ACCESS_KEY_ID` | Credencial AWS (se renueva con cada lab) |
| `AWS_SECRET_ACCESS_KEY` | Credencial AWS (se renueva con cada lab) |
| `AWS_SESSION_TOKEN` | Token de sesión (se renueva con cada lab) |
| `AWS_ACCOUNT_ID` | ID de la cuenta AWS |

> ⚠️ Las credenciales de AWS Academy expiran con cada sesión de laboratorio. Actualiza los secrets en GitHub cada vez que inicies un nuevo lab.

---

## Requisitos previos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Node.js 20](https://nodejs.org/)
- [Java 17](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/)
- [AWS CLI](https://aws.amazon.com/cli/)
- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- [Terraform](https://www.terraform.io/)

---

## Abrir el proyecto en otro equipo

```bash
# 1. Iniciar lab en AWS Academy y actualizar secrets en GitHub

# 2. Clonar el repositorio
git clone <url-del-repositorio>
cd <nombre-del-repositorio>

# 3. Configurar credenciales AWS
aws configure

# 4. Desplegar infraestructura
cd infra/tf
terraform init
terraform apply

# 5. Forzar redespliegue via CI/CD
git commit --allow-empty -m "chore: forzar redespliegue"
git push origin deploy

# 6. Esperar ~5 minutos y obtener la URL pública
aws eks update-kubeconfig --region us-east-1 --name entrega-devops-cluster
kubectl get svc frontend -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
```

---

## Ejecución de tests

### Backend Ventas

```bash
cd back-Ventas_SpringBoot/Springboot-API-REST
export JAVA_HOME=/usr/lib/jvm/java-17   # Linux/Mac
# set JAVA_HOME=C:\Program Files\Java\jdk-17  # Windows
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

## Estructura del proyecto

```
proyecto-semestral/
├── .github/
│   └── workflows/
│       ├── ci.yml               # Pipeline CI (rama develop)
│       └── cd.yml               # Pipeline CD (rama deploy)
│
├── back-Ventas_SpringBoot/
│   └── Springboot-API-REST/
│       ├── src/main/resources/
│       │   └── application.properties
│       └── Dockerfile           # Multi-stage: Maven build + JDK runtime
│
├── back-Despachos_SpringBoot/
│   └── Springboot-API-REST-DESPACHO/
│       ├── src/main/resources/
│       │   └── application.properties
│       └── Dockerfile           # Multi-stage: Maven build + JDK runtime
│
├── front_despacho/
│   ├── nginx.conf               # Configuración Nginx + proxy a backends
│   └── Dockerfile               # Multi-stage: Node build + Nginx serve
│
├── infra/
│   ├── k8s/
│   │   ├── secrets.yml          # Kubernetes Secret (credenciales BD)
│   │   ├── mysql.yml            # MySQL Deployment + ClusterIP Service
│   │   ├── backend-ventas.yml   # Deployment + ClusterIP Service
│   │   ├── backend-despachos.yml
│   │   ├── frontend.yml         # Deployment + LoadBalancer Service
│   │   └── autoscaling.yml      # HPA para los 3 servicios
│   └── tf/
│       ├── provider.tf          # Provider AWS + región
│       ├── vpc.tf               # VPC, subnets, IGW, route tables
│       ├── eks.tf               # EKS Cluster + Node Group
│       ├── ecr.tf               # Repositorios ECR
│       ├── security_groups.tf   # Security Groups
│       ├── variables.tf
│       └── outputs.tf
│
├── docker-compose.yml           # Ejecución local + build de imágenes
└── README.md
```