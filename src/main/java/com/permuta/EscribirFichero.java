package com.permuta;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Ejemplo 01 — Escribir en un fichero las líneas introducidas por el usuario.
 *
 * <p>El programa solicita líneas de texto al usuario por consola y las guarda
 * en un fichero de texto cuando el usuario escribe la palabra reservada "FIN".
 */
public final class EscribirFichero {

  /** Palabra que el usuario escribe para finalizar la entrada de datos. */
  private static final String CENTINELA = "FIN";

  /** Ruta del fichero de salida (relativa al directorio de ejecución). */
  private static final Path RUTA_FICHERO = Path.of("salida.txt");

  private EscribirFichero() {
  }

  /**
   * Punto de entrada del programa.
   *
   * @param args argumentos de línea de comandos (no se usan en este ejemplo)
   * @throws IOException si ocurre un error al escribir el fichero
   */
  public static void main(String[] args) throws IOException {
    List<String> lineas = leerLineasDelUsuario();
    escribirFichero(RUTA_FICHERO, lineas);
    System.out.printf("Fichero guardado en: %s%n", RUTA_FICHERO.toAbsolutePath());
    System.out.printf("Líneas escritas: %d%n", lineas.size());
  }

  /**
   * Lee líneas desde la entrada estándar hasta que el usuario escribe "FIN".
   *
   * @return lista inmutable con las líneas introducidas
   */
  private static List<String> leerLineasDelUsuario() {
    System.out.printf(
        "Escribe líneas de texto y pulsa Enter. Escribe \"%s\" para terminar.%n", CENTINELA);

    var lineas = new ArrayList<String>();

    try (var scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
      while (scanner.hasNextLine()) {
        var linea = scanner.nextLine();

        if (CENTINELA.equalsIgnoreCase(linea)) {
          break;
        }

        lineas.add(linea);
      }
    }

    return List.copyOf(lineas);
  }

  /**
   * Escribe una lista de líneas en el fichero indicado, creándolo si no existe
   * o sobreescribiéndolo si ya existía.
   *
   * @param ruta   ruta del fichero destino
   * @param lineas líneas de texto a escribir
   * @throws IOException si ocurre un error de entrada/salida
   */
  private static void escribirFichero(Path ruta, List<String> lineas) throws IOException {
    Files.write(ruta, lineas, StandardCharsets.UTF_8);
  }
}
