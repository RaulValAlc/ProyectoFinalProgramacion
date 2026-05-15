# ProyectoFinal - Concesionario de Coches

Aplicación de escritorio Java Swing para gestionar la compra y venta de coches entre usuarios. Desarrollado con NetBeans + Maven + MariaDB.

## Requisitos previos

1. **XAMPP** (o cualquier servidor con MariaDB/MySQL) activo con los módulos **Apache** y **MariaDB** encendidos.
2. **phpMyAdmin** para importar la base de datos.
3. **JDK 24** o superior.
4. **NetBeans** (recomendado) o cualquier IDE compatible con Maven.

## Configuración de la base de datos

1. Abre phpMyAdmin (http://localhost/phpmyadmin).
2. Crea una base de datos llamada **`concesionario`**.
3. Importa el archivo `Importar.sql` incluido en el proyecto.
4. Esto creará automáticamente las tablas necesarias.

### Estructura de la base de datos

**Base de datos:** `concesionario`

#### Tabla: `login` (usuarios registrados)

| Campo       | Tipo         | Restricciones        |
|-------------|--------------|----------------------|
| usuario     | VARCHAR(20)  | PRIMARY KEY          |
| contrasena  | VARCHAR(30)  | NOT NULL             |
| email       | VARCHAR(50)  | UNIQUE, NOT NULL     |

#### Tabla: `coches` (anuncios de coches en venta)

| Campo      | Tipo         | Restricciones                              |
|------------|--------------|--------------------------------------------|
| matricula  | VARCHAR(20)  | PRIMARY KEY                                |
| modelo     | VARCHAR(20)  |                                            |
| color      | VARCHAR(30)  |                                            |
| email      | VARCHAR(50)  | FOREIGN KEY → login(email)                 |
| kilometros | INT          |                                            |
| precio     | DECIMAL(10,2)|                                            |

## Credenciales de conexión (por defecto)

- **Host:** `localhost:3306`
- **Base de datos:** `concesionario`
- **Usuario:** `root`
- **Contraseña:** *(vacía)*

Estos valores están configurados en `VentanaLogin.java:29`.

## Funcionamiento del programa

1. **Login** (`VentanaLogin`): Pantalla de inicio de sesión. Si no tienes cuenta, puedes registrarte al ser un proyecto local la sesión no se puede mantener pese a estar la opción, se mantiene por diseño.
2. **Registro** (`VentanaRegistro`): Formulario con validaciones (usuario único, email válido, contraseña con mínimo 8 caracteres y 1 especial, etc.).
3. **Ventana Principal** (`VentanaPrincipal`): Menú con las siguientes opciones:
   - **Comprar Coche** — Muestra una tabla con todos los coches en venta. Puedes seleccionar uno y consultar el email del vendedor.
   - **Vender Coche** — Formulario para publicar un anuncio con los datos del coche (modelo, color, precio, kilómetros, matrícula). El email se rellena automáticamente.
   - **Editar Coche** — Introduce una matrícula para cargar los datos del coche y modificarlos.
   - **Eliminar Anuncio** — Muestra una tabla con tus coches publicados y permite borrarlos con confirmación.
   - **En desarrollo...** — Botón placeholder.
   - **Perfil** — Muestra tus datos personales y número de coches en venta. Permite exportar los datos a CSV.
   - **Salir** — Cierra la aplicación.
4. **Perfil** (`Estadisticas`): Requiere introducir la contraseña de nuevo para acceder. Muestra nombre, email, contraseña y cantidad de coches publicados. Incluye exportación a CSV.

## Ejecución

Desde NetBeans: abre el proyecto y ejecuta `ProyectoFinal.java` (main class: `com.mycompany.proyectofinal.ProyectoFinal`).

O con Maven desde terminal:
```bash
mvn clean compile exec:java
```

## Dependencias Maven

- `org.mariadb.jdbc:mariadb-java-client:3.3.3` — Driver JDBC para MariaDB.
