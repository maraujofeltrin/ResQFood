# PAW 2026a-03 - ResQFood

Plataforma web destinada a reducir el desperdicio de comida en establecimientos gastronómicos, conectando comercios con excedentes con clientes interesados en rescatarlos a precios reducidos.

## Requisitos Previos

- **Java 21**
- **Maven**
- **PostgreSQL**
- **Application Container** (ej. Tomcat)

## Configuración de Entorno

Antes de compilar o ejecutar el proyecto, es **estrictamente necesario** contar con el archivo de propiedades de entorno ubicado en:
`webapp/src/main/resources/env.properties`

Este archivo provee la configuración de conexión a la base de datos y demás variables de entorno requeridas por la aplicación.

En despliegue, estas propiedades pueden externalizarse mediante variables de entorno o parámetros JVM (`-D...`).
Si `env.properties` no está presente en el classpath, la aplicación usará los valores del entorno.
La propiedad `security.remember-me.key` debe ser un secreto largo y aleatorio (recomendado 32+ caracteres).
Si `app.base-url` no es localhost, no se aceptan valores placeholder para `security.remember-me.key`.

## Construcción del Proyecto

Para empaquetar el proyecto y generar el ejecutable, posicionarse en la raíz del mismo y ejecutar:

```bash
mvn clean package
```

Este comando descargará las dependencias necesarias, compilará el código y generará un archivo `.war` (típicamente en `webapp/target/`) listo para ser desplegado. El proyecto no incluye archivos innecesarios de configuración de IDEs ni dependencias pre-descargadas.

## Despliegue y Base de Datos

El proyecto utiliza **Flyway** para el manejo de migraciones de base de datos. 
Al deployar el `.war` generado en un Application Container, siempre que exista una base de datos PostgreSQL configurada con los permisos adecuados, la aplicación **generará de manera automática todas las tablas** y estructuras necesarias para su funcionamiento inicial.

## Usuarios de Prueba en Produccion (Autenticación)

El sistema requiere autenticación y diferencia a los usuarios según su rol. Se proveen las siguientes credenciales para probar la aplicación con los distintos niveles de acceso:

### Nivel Cliente (Role: CLIENT)
- **Email:** `cliente@yopmail.com`
- **Contraseña:** `12345678`

### Nivel Comercio (Role: COMMERCE)
- **Email:** `comercio@yopmail.com`
- **Contraseña:** `12345678`
