resource "aws_eks_cluster" "eks" {
  name     = var.eks_cluster_name
  role_arn = data.aws_iam_role.lab.arn

  vpc_config {
    subnet_ids         = [aws_subnet.public.id, aws_subnet.public_b.id]
    security_group_ids = [aws_security_group.eks.id]
  }

  tags = {
    Name = var.eks_cluster_name
  }
}

resource "aws_eks_node_group" "workers" {
  cluster_name    = aws_eks_cluster.eks.name
  node_group_name = var.eks_node_group_name
  node_role_arn   = data.aws_iam_role.lab.arn

  subnet_ids = [aws_subnet.public.id, aws_subnet.public_b.id]

  scaling_config {
    desired_size = 2
    max_size     = 3
    min_size     = 1
  }

  instance_types = ["t3.medium"]
  capacity_type  = "ON_DEMAND"

  tags = {
    Name = var.eks_node_group_name
  }
}