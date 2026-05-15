package com.mycompany.proyectofinal;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class LoginFlowTest {

    static final String DB_URL = "jdbc:mariadb://localhost:3306/concesionario";
    static final String DB_USER = "root";
    static final String DB_PASS = "";

    public static void main(String[] args) {
        int fallos = 0;
        int pruebas = 0;

        // 1. Conectar a la BD
        pruebas++;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            System.out.println("OK 1: Conexión a MariaDB establecida");

            // 2. Verificar estructura de la tabla login
            pruebas++;
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, "login", "contrasena")) {
                if (rs.next()) {
                    int size = rs.getInt("COLUMN_SIZE");
                    System.out.println("OK 2: columna contrasena = VARCHAR(" + size + ")");
                    if (size < 60) {
                        System.out.println("FALLO 2: La columna contrasena es VARCHAR(" + size + ") pero necesita al menos VARCHAR(60) para bcrypt");
                        fallos++;
                    }
                } else {
                    System.out.println("FALLO 2: No se encontró la columna 'contrasena' en tabla 'login'");
                    fallos++;
                }
            }

            // 3. Simular registro: hashear contraseña con bcrypt
            pruebas++;
            String passwordOriginal = "TestPass123!";
            String hash = BCrypt.hashpw(passwordOriginal, BCrypt.gensalt());
            System.out.println("Hash generado: " + hash + " (" + hash.length() + " chars)");

            // 4. Insertar usuario de prueba
            pruebas++;
            String testUser = "test_" + System.currentTimeMillis();
            String testEmail = testUser + "@test.com";
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO login(usuario,contrasena,email) VALUES(?,?,?)")) {
                ps.setString(1, testUser);
                ps.setString(2, hash);
                ps.setString(3, testEmail);
                ps.executeUpdate();
                System.out.println("OK 4: Usuario de prueba insertado: " + testUser);
            }

            // 5. Login: recuperar hash y verificar con BCrypt.checkpw
            pruebas++;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT contrasena FROM login WHERE usuario = ?")) {
                ps.setString(1, testUser);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("contrasena");
                        System.out.println("Hash recuperado: " + storedHash + " (" + storedHash.length() + " chars)");

                        // Verificar que sea el mismo hash
                        if (!hash.equals(storedHash)) {
                            System.out.println("FALLO 5a: El hash recuperado NO coincide con el insertado");
                            System.out.println("  Insertado: " + hash);
                            System.out.println("  Recuperado: " + storedHash);
                            fallos++;
                        } else {
                            System.out.println("OK 5a: Hash insertado y recuperado coinciden");
                        }

                        // Verificar con BCrypt.checkpw (exactamente como hace el login)
                        if (BCrypt.checkpw(passwordOriginal, storedHash)) {
                            System.out.println("OK 5b: BCrypt.checkpw() verifica correctamente");
                        } else {
                            System.out.println("FALLO 5b: BCrypt.checkpw() NO pudo verificar la contraseña");
                            fallos++;
                        }

                        // Probar con contraseña incorrecta
                        if (!BCrypt.checkpw("WrongPass123!", storedHash)) {
                            System.out.println("OK 5c: Contraseña incorrecta rechazada correctamente");
                        } else {
                            System.out.println("FALLO 5c: Contraseña incorrecta NO fue rechazada");
                            fallos++;
                        }
                    } else {
                        System.out.println("FALLO 5: Usuario de prueba no encontrado en BD");
                        fallos++;
                    }
                }
            }

            // 6. Probar con contraseña que tiene caracteres especiales
            pruebas++;
            String passwordEspecial = "H0la_Mundo@2024#Segura!";
            String hashEsp = BCrypt.hashpw(passwordEspecial, BCrypt.gensalt());
            String testUser2 = "test2_" + System.currentTimeMillis();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO login(usuario,contrasena,email) VALUES(?,?,?)")) {
                ps.setString(1, testUser2);
                ps.setString(2, hashEsp);
                ps.setString(3, testUser2 + "@test.com");
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT contrasena FROM login WHERE usuario = ?")) {
                ps.setString(1, testUser2);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    String storedHash2 = rs.getString("contrasena");
                    if (BCrypt.checkpw(passwordEspecial, storedHash2)) {
                        System.out.println("OK 6: Contraseña con caracteres especiales verificada");
                    } else {
                        System.out.println("FALLO 6: Contraseña con caracteres especiales NO verificada");
                        fallos++;
                    }
                }
            }

            // 7. Probar el fallback de texto plano
            pruebas++;
            String testUser3 = "test3_" + System.currentTimeMillis();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO login(usuario,contrasena,email) VALUES(?,?,?)")) {
                ps.setString(1, testUser3);
                ps.setString(2, "PassEnTextoPlano1!");  // texto plano (simula versión antigua)
                ps.setString(3, testUser3 + "@test.com");
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT contrasena FROM login WHERE usuario = ?")) {
                ps.setString(1, testUser3);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    String storedHash3 = rs.getString("contrasena");
                    boolean bcryptOk = false;
                    try {
                        bcryptOk = BCrypt.checkpw("PassEnTextoPlano1!", storedHash3);
                    } catch (IllegalArgumentException e) {
                        System.out.println("  BCrypt.checkpw lanzó excepción (esperado con texto plano)");
                    }
                    if (!bcryptOk && storedHash3.equals("PassEnTextoPlano1!")) {
                        System.out.println("OK 7: Fallback a texto plano funciona correctamente");
                    } else {
                        System.out.println("FALLO 7: Fallback a texto plano NO funcionó");
                        fallos++;
                    }
                }
            }

            // 8. Probar recuperación de hash bcrypt truncado (VARCHAR(30))
            //    Si la columna ya fue corregida a VARCHAR(60), no hay truncamiento
            pruebas++;
            String pass8 = "MiClaveSegura99!";
            String hash8 = BCrypt.hashpw(pass8, BCrypt.gensalt());
            String testUser8 = "test8_" + System.currentTimeMillis();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO login(usuario,contrasena,email) VALUES(?,?,?)")) {
                ps.setString(1, testUser8);
                ps.setString(2, hash8);
                ps.setString(3, testUser8 + "@test.com");
                ps.executeUpdate();
                System.out.println("  Hash insertado: " + hash8 + " (" + hash8.length() + " chars)");
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT contrasena FROM login WHERE usuario = ?")) {
                ps.setString(1, testUser8);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    String storedHash = rs.getString("contrasena");
                    System.out.println("  Hash recuperado: " + storedHash + " (" + storedHash.length() + " chars)");

                    if (storedHash.length() < 60) {
                        // Columna VARCHAR(30): probar recuperación de truncamiento
                        boolean recuperado = false;
                        if (storedHash.startsWith("$2a$") && storedHash.length() >= 29) {
                            String salt = storedHash.substring(0, 29);
                            String fullHash = BCrypt.hashpw(pass8, salt);
                            if (fullHash.startsWith(storedHash)) {
                                recuperado = true;
                                System.out.println("  Hash regenerado: " + fullHash);
                            }
                        }
                        if (recuperado) {
                            System.out.println("OK 8: Recuperación de hash truncado funciona");
                        } else {
                            System.out.println("FALLO 8: No se pudo recuperar el hash truncado");
                            fallos++;
                        }
                    } else {
                        // Columna VARCHAR(60): no hay truncamiento
                        if (BCrypt.checkpw(pass8, storedHash)) {
                            System.out.println("OK 8: Columna correcta (VARCHAR(60)), hash almacenado sin truncar");
                        } else {
                            System.out.println("FALLO 8: Hash almacenado no verifica");
                            fallos++;
                        }
                    }
                }
            }

            // 9. Probar ALTER TABLE para corregir el esquema
            pruebas++;
            try (java.sql.Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE login MODIFY contrasena VARCHAR(60) NOT NULL");
                System.out.println("  ALTER TABLE ejecutado - columna cambiada a VARCHAR(60)");
            }
            DatabaseMetaData meta2 = conn.getMetaData();
            try (ResultSet rs = meta2.getColumns(null, null, "login", "contrasena")) {
                rs.next();
                int newSize = rs.getInt("COLUMN_SIZE");
                if (newSize >= 60) {
                    System.out.println("OK 9: ALTER TABLE corrigió columna a VARCHAR(" + newSize + ")");
                } else {
                    System.out.println("FALLO 9: Columna sigue siendo VARCHAR(" + newSize + ")");
                    fallos++;
                }
            }

            // 10. Después del ALTER TABLE, insertar y recuperar hash completo
            pruebas++;
            String pass10 = "NuevaClave99!";
            String hash10 = BCrypt.hashpw(pass10, BCrypt.gensalt());
            String testUser10 = "test10_" + System.currentTimeMillis();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO login(usuario,contrasena,email) VALUES(?,?,?)")) {
                ps.setString(1, testUser10);
                ps.setString(2, hash10);
                ps.setString(3, testUser10 + "@test.com");
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT contrasena FROM login WHERE usuario = ?")) {
                ps.setString(1, testUser10);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    String storedHash = rs.getString("contrasena");
                    if (hash10.equals(storedHash) && BCrypt.checkpw(pass10, storedHash)) {
                        System.out.println("OK 10: Después del ALTER, hash completo almacenado y verificado correctamente");
                    } else {
                        System.out.println("FALLO 10: Después del ALTER, el hash sigue truncado o no verifica");
                        System.out.println("  Original: " + hash10 + " (" + hash10.length() + ")");
                        System.out.println("  Almacenado: " + storedHash + " (" + storedHash.length() + ")");
                        fallos++;
                    }
                }
            }

            // Limpiar usuarios de prueba
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM coches WHERE email LIKE ?")) {
                ps.setString(1, "test_%@test.com");
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM login WHERE usuario LIKE ?")) {
                ps.setString(1, "test_%");
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM login WHERE usuario LIKE ?")) {
                ps.setString(1, "test2_%");
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM login WHERE usuario LIKE ?")) {
                ps.setString(1, "test3_%");
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM login WHERE usuario LIKE ?")) {
                ps.setString(1, "test8_%");
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM login WHERE usuario LIKE ?")) {
                ps.setString(1, "test10_%");
                ps.executeUpdate();
            }
            System.out.println("Limpieza completada");

        } catch (SQLException e) {
            System.out.println("FALLO " + (pruebas + 1) + ": Error de BD: " + e.getMessage());
            e.printStackTrace();
            fallos++;
        }

        System.out.println("\n=== Resultados: " + (pruebas - fallos) + "/" + pruebas + " pruebas pasadas ===");
        if (fallos > 0) {
            System.out.println("¡" + fallos + " FALLO(S)!");
            System.exit(1);
        } else {
            System.out.println("Todos los tests pasaron correctamente.");
        }
    }
}
