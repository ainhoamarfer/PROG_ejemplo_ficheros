package com.permuta;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Ejemplo 02 — Leer un fichero línea a línea y mostrar su contenido por pantalla.
 *
 * <p>El programa recibe la ruta de un fichero como argumento de línea de comandos,
 * lo lee línea a línea usando un Stream y muestra cada línea numerada en la consola.
 */
public final class LeerFichero {

  private static final String FILE_PATH = "datos/contacto.json";
  private LeerFichero() {
  }

  /**
   * Punto de entrada del programa.
   *
   * @param args args[0] debe ser la ruta al fichero que se quiere leer
   * @throws IOException si ocurre un error al leer el fichero
   */
  public static void main(String[] args) throws IOException {
    var ruta = Path.of(FILE_PATH);

    if (!Files.exists(ruta)) {
      System.err.printf("El fichero no existe: %s%n", ruta.toAbsolutePath());
      return;
    }

    mostrarFichero(ruta);
  }

  /**
   * Lee el fichero línea a línea y muestra cada línea precedida de su número.
   *
   * @param ruta ruta del fichero a leer
   * @throws IOException si ocurre un error de entrada/salida
   */
  private static void mostrarFichero(Path ruta) throws IOException {
    System.out.printf("--- Contenido de: %s ---%n", ruta.getFileName());

    try (var lineas = Files.lines(ruta, StandardCharsets.UTF_8)) {
      var contador = new int[]{1};
      lineas.forEach(linea -> {
        System.out.printf("%4d │ %s%n", contador[0], linea);
        contador[0]++;
      });
    }

    System.out.printf("--- Fin del fichero ---%n");
  }
}
