CREATE TABLE login (
    usuario VARCHAR(20) PRIMARY KEY,
    contrasena VARCHAR(30) NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE coches (
    matricula VARCHAR(20) PRIMARY KEY,
    modelo VARCHAR(20),
    color VARCHAR(30),
    email VARCHAR(50),
    kilometros INT,
    precio DECIMAL(10,2),

    CONSTRAINT fk_coches_login
    FOREIGN KEY (email) REFERENCES login(email)
);
