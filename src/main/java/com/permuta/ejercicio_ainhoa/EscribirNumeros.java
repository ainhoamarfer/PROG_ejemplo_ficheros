package com.permuta.ejercicio_ainhoa;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EscribirNumeros {

    private static final int END = -1;
    private static final Path RUTA_FICHERO = Path.of("recursos/numeros.txt");

    private static List<Integer> leerNumerosDelUsuario() {
        System.out.printf("Escribe todos los números que quieras. Escribe \"%s\" para terminar.%n", END);

        List<Integer> numeros = new ArrayList<Integer>();

        try (Scanner scanner = new Scanner(System.in)) {

            while (scanner.hasNextLine()) {
                int numero = scanner.nextInt();

                if (END == numero) {
                    break;
                }

                numeros.add(numero);
            }
        }
        return List.copyOf(numeros);
    }

    private static void escribirFicheroConNumeros(Path ruta, List<Integer> numeros) throws IOException {
        Files.write(ruta, numeros.stream().map(n -> n + "").toList(), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws IOException {
        List<Integer> numerosDelUsuario = leerNumerosDelUsuario();
        escribirFicheroConNumeros(RUTA_FICHERO, numerosDelUsuario);
    }
}
