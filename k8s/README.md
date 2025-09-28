# Kubernetes Deployment Guide

## Repository Structure

```
k8s/
├── deployment.yaml (backend)
├── services.yaml (backend)
├── keycloak/
│   ├── keycloak-deployment.yaml
│   └── keycloak-service.yaml
├── mariadb/
│   ├── database-deployment.yaml
│   └── database-service.yaml
├── flowable/
│   ├── flowable-deployment.yaml
│   └── flowable-service.yaml
└── README.md
```

## Deployment Order

Deploy the manifests in the following order:

### 1. Database First
```bash
kubectl apply -f mariadb/database-deployment.yaml
kubectl apply -f mariadb/database-service.yaml
```

### 2. Keycloak Second
```bash
kubectl apply -f keycloak/keycloak-deployment.yaml
kubectl apply -f keycloak/keycloak-service.yaml
```

### 3. Flowable Third
```bash
kubectl apply -f flowable/flowable-deployment.yaml
kubectl apply -f flowable/flowable-service.yaml
```

### 4. Backend Fourth
```bash
kubectl apply -f deployment.yaml
kubectl apply -f services.yaml
```

### 5. Frontend Last
Deploy the frontend from its own repository:
```bash
# In the frontend repository
kubectl apply -f K8s/deployment.yaml
```

## Service URLs in Kubernetes

- **Database**: `database-service:3306`
- **Keycloak**: `keycloak-service:8080`
- **Flowable**: `flowable-service:8080`
- **Backend**: `backend-service:8089`
- **Frontend**: `frontend-service:80`

## Environment Variables

The backend is configured to use:
- Database: `database-service:3306`
- Keycloak: `keycloak-service:8080`

Make sure your frontend environment configuration points to the correct service URLs.
