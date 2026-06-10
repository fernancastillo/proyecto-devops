variable "aws_region" {
  default = "us-east-1"
}

variable "project_name" {
  default = "entrega-devops"
}

variable "eks_cluster_name" {
  default = "entrega-devops-cluster"
}

variable "eks_node_group_name" {
  default = "entrega-devops-workers"
}

variable "db_user" {
  default = "root"
}

variable "db_password" {
  default = "secreto123"
}

variable "db_ven" {
  default = "db_ventas"
}

variable "db_desp" {
  default = "db_despachos"
}

variable "key_pair_name" {
  default = "devops_key"
}