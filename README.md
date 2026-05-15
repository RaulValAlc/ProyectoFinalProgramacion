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
| contrasena  | VARCHAR(60)  | NOT NULL             |
| email       | VARCHAR(50)  | UNIQUE, NOT NULL     |

> Las contraseñas se almacenan hasheadas con **bcrypt** (60 caracteres).

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

1. **Login** (`VentanaLogin`): Pantalla de inicio de sesión. Verifica la contraseña contra el hash almacenado con bcrypt. Si no tienes cuenta, puedes registrarte.
2. **Registro** (`VentanaRegistro`): Formulario con validaciones (usuario único, email único y con formato válido, contraseña con mínimo 8 caracteres y al menos 1 especial, etc.). La contraseña se hashea con bcrypt antes de guardarse.
3. **Ventana Principal** (`VentanaPrincipal`): Menú con las siguientes opciones:
   - **Comprar Coche** — Muestra una tabla con todos los coches en venta. Puedes seleccionar uno y consultar el email del vendedor.
   - **Vender Coche** — Formulario para publicar un anuncio con los datos del coche (modelo, color, precio, kilómetros, matrícula). El email se rellena automáticamente.
   - **Editar Coche** — Introduce una matrícula para cargar los datos del coche y modificarlos. Solo puedes editar coches que te pertenecen.
   - **Eliminar Anuncio** — Muestra una tabla con tus coches publicados y permite borrarlos con confirmación.
   - **En desarrollo...** — Botón placeholder.
   - **Perfil** — Muestra tus datos personales y número de coches en venta. Permite exportar los datos a CSV. Para acceder requiere la contraseña de nuevo.
   - **Salir** — Cierra la aplicación.
4. **Perfil** (`Estadisticas`): Requiere introducir la contraseña de nuevo para acceder. Muestra nombre, email, contraseña (oculta con asteriscos por seguridad) y cantidad de coches publicados. Incluye exportación a CSV.

## Seguridad

- Las contraseñas se hashean con **bcrypt** (librería `jbcrypt 0.4`) antes de almacenarse en la base de datos.
- La contraseña en texto plano **nunca** se almacena en memoria durante la sesión ni se pasa entre ventanas.
- Al acceder al perfil, la contraseña se verifica contra el hash de la base de datos usando bcrypt, no contra texto plano.
- El diálogo de verificación de contraseña en el perfil usa un `JPasswordField` (campo oculto).
- En la pantalla de perfil, la contraseña se muestra como asteriscos (`********`) sin revelar su longitud.
- El campo de contraseña en registro usa `JPasswordField` (no visible en texto plano).
- - Se utiliza `PreparedStatement` para prevenir inyección SQL.
- La validación de email duplicado se realiza antes de insertar.
- La edición de coches verifica que el coche pertenezca al usuario antes de permitir modificaciones.
- El login tiene compatibilidad hacia atrás:
  - Si la BD tiene contraseñas en texto plano (versiones antiguas), las detecta, migra automáticamente a bcrypt e inicia sesión.
  - Si la BD tiene hashes bcrypt truncados (columna `VARCHAR(30)` en vez de `VARCHAR(60)`), los recupera usando la sal del hash truncado y los migra a hash completo.
  - Al iniciar, la aplicación ejecuta `ALTER TABLE login MODIFY contrasena VARCHAR(60) NOT NULL` para corregir el esquema automáticamente.
- Se usa `try-with-resources` para cerrar automáticamente conexiones JDBC y evitar fugas de recursos.
- Se validan campos vacíos antes de cualquier operación para evitar errores.
- `BCrypt.checkpw()` captura `IllegalArgumentException` para manejar hashes inválidos sin romper la aplicación.

## Ejecución

Desde NetBeans: abre el proyecto y ejecuta `ProyectoFinal.java` (main class: `com.mycompany.proyectofinal.ProyectoFinal`).

O con Maven desde terminal:
```bash
mvn clean compile exec:java
```

## Dependencias Maven

- `org.mariadb.jdbc:mariadb-java-client:3.3.3` — Driver JDBC para MariaDB.
- `org.mindrot:jbcrypt:0.4` — Librería para hash bcrypt de contraseñas.
