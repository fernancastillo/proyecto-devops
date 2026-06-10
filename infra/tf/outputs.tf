output "eks_cluster_name" {
  value       = aws_eks_cluster.eks.name
  description = "Nombre del cluster EKS"
}

output "eks_cluster_endpoint" {
  value       = aws_eks_cluster.eks.endpoint
  description = "Endpoint del cluster EKS"
}

output "backend_ventas_ecr" {
  value       = aws_ecr_repository.backend_ventas.repository_url
  description = "URL del repositorio ECR backend ventas"
}

output "backend_despachos_ecr" {
  value       = aws_ecr_repository.backend_despachos.repository_url
  description = "URL del repositorio ECR backend despachos"
}

output "frontend_ecr" {
  value       = aws_ecr_repository.frontend.repository_url
  description = "URL del repositorio ECR frontend"
}