package com.permuta.ejemplo_clase.escritura;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

//Leera la entrada de un usuario para obtener un listado de números positivos. 
// Se dejará de introducir números cuando el usuario introduzca un -1
//Después se escribirá el listado de números introducidos en un fichero de texto, en una sola línea, separados por coma y un espacio.
public class App {
    private static final String SEPARATOR = ", ";

    public static void main(String[] args) {
        List<Integer> numeros = readUserNumbers();

        Path path = readUserFilePath();

        writeNumbersToFile(numeros, path);
    }

    private static Path readUserFilePath() {

        // Obtenemos la ruta del fichero
        Scanner sc = new Scanner(System.in);
        System.out.println("Introduce la ruta del fichero de salida:");
        String ruta = sc.nextLine();

        Path path = Path.of(ruta);

        return path;
    }

    private static List<Integer> readUserNumbers() {

        Scanner sc = new Scanner(System.in);
        System.out.println("Introduce números positivos (introduce -1 para finalizar):");

        List<Integer> numeros = new java.util.ArrayList<>();

        while (true) {
            int numero = sc.nextInt();
            sc.nextLine();
            if (numero == -1) {
                break;
            }
            if (numero < -1) {
                System.out.println("Número no válido, introduce un número positivo o -1 para finalizar.");
                continue;
            }
            numeros.add(numero);
        }

        return numeros;

    }

    private static void writeNumbersToFile(List<Integer> numeros, Path path) {
        // Convierte la lista de numeros a una cadena de texto con el formato concreto

        // Precondiciones - Cosas que se deben cumplir para que el método funcione
        // correctamente
        if (numeros == null) {
            throw new IllegalArgumentException("numeros: null");
        }
        if (path == null) {
            throw new IllegalArgumentException("path: null");
        }
        if (Files.exists(path)) {
            throw new IllegalArgumentException("El path ya existe: " + path);
        }

        // Cuerpo del método - Lo que hace el método
        String s = numeros.isEmpty() ? "No se introdujeron números" // Si la lista de numeros está vacía, la cadena resultante es un mensaje indicando que no se introdujeron números
                : numeros.stream()
                        .map(n -> n + "") // Convierte el numero a su String asociada
                        .reduce("", (acc, n) -> acc + SEPARATOR + n); // Concatena cada numero a la cadena acumulada,
                                                                      // separando
                                                                      // por el SEPARATOR

        s = s.substring(0, s.length() - SEPARATOR.length()); // Elimina del final de la cadena

        try {
            Files.writeString(path, s);
        } catch (IOException e) {
            System.err.printf("Error escribiendo numeros: %s en path: %s%n", s, path);
            e.printStackTrace();
        }

        // Postcondiciones - Lo que se garantiza después de ejecutar el método
        // En este caso no hay postcondiciones explícitas.
    }
}
