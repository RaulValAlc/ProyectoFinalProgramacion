package com.mycompany.proyectofinal;

import org.mindrot.jbcrypt.BCrypt;

public class BcryptTest {

    public static void main(String[] args) {
        int fallos = 0;
        int pruebas = 0;

        // Prueba 1: hash y verificación básica
        pruebas++;
        String pass1 = "MiPass123!";
        String hash1 = BCrypt.hashpw(pass1, BCrypt.gensalt());
        if (!BCrypt.checkpw(pass1, hash1)) {
            System.out.println("FALLO 1: No se pudo verificar la contraseña '" + pass1 + "' contra su propio hash");
            fallos++;
        } else {
            System.out.println("OK 1: Hash y verificación básica");
        }

        // Prueba 2: hash con caracteres especiales
        pruebas++;
        String pass2 = "H0l@_Mundo!2024#";
        String hash2 = BCrypt.hashpw(pass2, BCrypt.gensalt());
        if (!BCrypt.checkpw(pass2, hash2)) {
            System.out.println("FALLO 2: No se pudo verificar contraseña con caracteres especiales");
            fallos++;
        } else {
            System.out.println("OK 2: Caracteres especiales");
        }

        // Prueba 3: contraseña incorrecta debe fallar
        pruebas++;
        String pass3 = "PassCorrecta99!";
        String hash3 = BCrypt.hashpw(pass3, BCrypt.gensalt());
        if (BCrypt.checkpw("PassIncorrecta!", hash3)) {
            System.out.println("FALLO 3: Contraseña incorrecta verificó como verdadera");
            fallos++;
        } else {
            System.out.println("OK 3: Contraseña incorrecta rechazada");
        }

        // Prueba 4: contraseña con espacios
        pruebas++;
        String pass4 = "Mi Pass Con Espacios 123!";
        String hash4 = BCrypt.hashpw(pass4, BCrypt.gensalt());
        if (!BCrypt.checkpw(pass4, hash4)) {
            System.out.println("FALLO 4: No se pudo verificar contraseña con espacios");
            fallos++;
        } else {
            System.out.println("OK 4: Contraseña con espacios");
        }

        // Prueba 5: contraseña larga (30 caracteres)
        pruebas++;
        String pass5 = "Cl4ve_Super_Segura_2024_X!";
        String hash5 = BCrypt.hashpw(pass5, BCrypt.gensalt());
        if (!BCrypt.checkpw(pass5, hash5)) {
            System.out.println("FALLO 5: No se pudo verificar contraseña larga");
            fallos++;
        } else {
            System.out.println("OK 5: Contraseña larga");
        }

        // Prueba 6: hash generado tiene 60 caracteres
        pruebas++;
        String pass6 = "Test123!";
        String hash6 = BCrypt.hashpw(pass6, BCrypt.gensalt());
        if (hash6.length() != 60) {
            System.out.println("FALLO 6: Hash no tiene 60 caracteres (tiene " + hash6.length() + ")");
            fallos++;
        } else {
            System.out.println("OK 6: Hash tiene 60 caracteres");
        }

        // Prueba 7: mismo hash con contraseña incorrecta
        pruebas++;
        String pass7 = "MiClave!1";
        String hash7 = BCrypt.hashpw(pass7, BCrypt.gensalt());
        if (BCrypt.checkpw("otraClave!2", hash7)) {
            System.out.println("FALLO 7: Contraseña diferente verificó como verdadera");
            fallos++;
        } else {
            System.out.println("OK 7: Contraseña diferente rechazada");
        }

        // Prueba 8: texto plano como hash (compatibilidad con versión antigua)
        pruebas++;
        String hashPlano = "PassEnTextoPlano1!";
        try {
            BCrypt.checkpw("cualquier", hashPlano);
            System.out.println("FALLO 8: BCrypt.checkpw no lanzó excepción con hash inválido");
            fallos++;
        } catch (IllegalArgumentException e) {
            System.out.println("OK 8: BCrypt.checkpw lanza IllegalArgumentException con hash inválido (texto plano)");
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
