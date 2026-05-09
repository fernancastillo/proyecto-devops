#!/bin/sh

# Este comando intenta conectarse al host y puerto de la BD
# Sustituye 'db-host' y '5432' por tus variables si es necesario
echo "Esperando a que la base de datos esté lista..."

# Una forma sencilla de esperar (puedes usar herramientas como wait-for-it.sh)
sleep 10 

echo "Base de datos detectada, iniciando Spring Boot..."
exec java -jar app.jar