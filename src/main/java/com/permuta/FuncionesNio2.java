package com.permuta;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Ejemplo 03 — Funciones más comunes de {@link Path} y {@link Files}.
 *
 * <p>El programa crea una estructura temporal de directorios y ficheros,
 * recorre las operaciones más habituales de la API NIO.2 y elimina todo
 * al finalizar, sin dejar rastro en el disco.
 */
public final class FuncionesNio2 {

  private FuncionesNio2() {
  }

  /**
   * Punto de entrada del programa.
   *
   * @param args no se usan en este ejemplo
   * @throws IOException si ocurre un error de entrada/salida
   */
  public static void main(String[] args) throws IOException {
    var base = Files.createTempDirectory("nio2-demo-");
    try {
      demostrarPath(base);
      demostrarMetadatos(base);
      demostrarLecturaEscritura(base);
      demostrarCopiaMovimiento(base);
      demostrarListadoDirectorio(base);
    } finally {
      eliminarDirectorioRecursivo(base);
      System.out.printf("%n[Limpieza] Directorio temporal eliminado: %s%n", base);
    }
  }

  // -------------------------------------------------------------------------
  // 1. Operaciones sobre Path
  // -------------------------------------------------------------------------

  private static void demostrarPath(Path base) {
    titulo("1. Operaciones sobre Path");

    Path fichero = base.resolve("documentos").resolve("notas.txt");

    linea("Path completo",          fichero.toString());
    linea("Nombre del fichero",     fichero.getFileName().toString());
    linea("Directorio padre",       fichero.getParent().toString());
    linea("Ruta absoluta",          fichero.toAbsolutePath().toString());
    linea("Normalizado",            fichero.normalize().toString());
    linea("Número de segmentos",    String.valueOf(fichero.getNameCount()));
    linea("Segmento 0",             fichero.getName(0).toString());
    linea("¿Termina en .txt?",      String.valueOf(fichero.endsWith("notas.txt")));

    Path relativo = base.relativize(fichero);
    linea("Ruta relativa desde base", relativo.toString());
  }

  // -------------------------------------------------------------------------
  // 2. Metadatos de ficheros y directorios
  // -------------------------------------------------------------------------

  private static void demostrarMetadatos(Path base) throws IOException {
    titulo("2. Metadatos con Files");

    Path fichero = base.resolve("metadatos.txt");
    Files.writeString(fichero, "contenido de prueba", StandardCharsets.UTF_8);

    linea("¿Existe?",               String.valueOf(Files.exists(fichero)));
    linea("¿Es fichero regular?",   String.valueOf(Files.isRegularFile(fichero)));
    linea("¿Es directorio?",        String.valueOf(Files.isDirectory(fichero)));
    linea("¿Se puede leer?",        String.valueOf(Files.isReadable(fichero)));
    linea("¿Se puede escribir?",    String.valueOf(Files.isWritable(fichero)));
    linea("Tamaño (bytes)",         String.valueOf(Files.size(fichero)));
    linea("Última modificación",    Files.getLastModifiedTime(fichero).toString());
    linea("Tipo MIME (probing)",    Files.probeContentType(fichero));

    linea("¿Existe base?",          String.valueOf(Files.exists(base)));
    linea("¿base es directorio?",   String.valueOf(Files.isDirectory(base)));
  }

  // -------------------------------------------------------------------------
  // 3. Lectura y escritura
  // -------------------------------------------------------------------------

  private static void demostrarLecturaEscritura(Path base) throws IOException {
    titulo("3. Lectura y escritura con Files");

    Path fichero = base.resolve("lectura.txt");
    String contenido = """
        Primera línea
        Segunda línea
        Tercera línea
        """;

    // Escritura completa como String
    Files.writeString(fichero, contenido, StandardCharsets.UTF_8);
    linea("Escrito con writeString", fichero.getFileName().toString());

    // Lectura completa como String
    String texto = Files.readString(fichero, StandardCharsets.UTF_8);
    linea("readString (chars)",     String.valueOf(texto.length()));

    // Lectura como lista de líneas
    var lista = Files.readAllLines(fichero, StandardCharsets.UTF_8);
    linea("readAllLines (líneas)", String.valueOf(lista.size()));

    // Lectura perezosa línea a línea con Stream
    System.out.println("  lines() (perezoso):");
    try (var stream = Files.lines(fichero, StandardCharsets.UTF_8)) {
      stream.map("    → "::concat).forEach(System.out::println);
    }
  }

  // -------------------------------------------------------------------------
  // 4. Copia y movimiento
  // -------------------------------------------------------------------------

  private static void demostrarCopiaMovimiento(Path base) throws IOException {
    titulo("4. Copia y movimiento con Files");

    Path origen  = base.resolve("origen.txt");
    Path copia   = base.resolve("copia.txt");
    Path destino = base.resolve("movido.txt");

    Files.writeString(origen, "texto de prueba", StandardCharsets.UTF_8);

    // Copia — REPLACE_EXISTING sobreescribe si el destino ya existe
    Files.copy(origen, copia, StandardCopyOption.REPLACE_EXISTING);
    linea("copy → copia.txt existe",   String.valueOf(Files.exists(copia)));

    // Movimiento (equivale a renombrar si es la misma partición)
    Files.move(copia, destino, StandardCopyOption.REPLACE_EXISTING);
    linea("move → movido.txt existe",  String.valueOf(Files.exists(destino)));
    linea("move → copia.txt ya no existe", String.valueOf(!Files.exists(copia)));

    // Borrado individual
    Files.deleteIfExists(origen);
    Files.deleteIfExists(destino);
    linea("deleteIfExists → origen eliminado", String.valueOf(!Files.exists(origen)));
  }

  // -------------------------------------------------------------------------
  // 5. Listado de directorios
  // -------------------------------------------------------------------------

  private static void demostrarListadoDirectorio(Path base) throws IOException {
    titulo("5. Listado de directorios con Files");

    // Crear estructura: base/sub/a.txt, base/sub/b.txt, base/c.txt
    Path sub = base.resolve("sub");
    Files.createDirectories(sub);
    Files.writeString(sub.resolve("a.txt"), "a", StandardCharsets.UTF_8);
    Files.writeString(sub.resolve("b.txt"), "b", StandardCharsets.UTF_8);
    Files.writeString(base.resolve("c.txt"), "c", StandardCharsets.UTF_8);

    // list() — solo el nivel inmediato, no recursivo
    System.out.println("  list() (nivel inmediato de base):");
    try (var stream = Files.list(base)) {
      stream.map(p -> "    " + p.getFileName()).forEach(System.out::println);
    }

    // walk() — recorre recursivamente todo el árbol
    System.out.println("  walk() (árbol completo):");
    try (var stream = Files.walk(base)) {
      stream
          .filter(Files::isRegularFile)
          .map(p -> "    " + base.relativize(p))
          .forEach(System.out::println);
    }
  }

  // -------------------------------------------------------------------------
  // Utilidades de presentación
  // -------------------------------------------------------------------------

  private static void titulo(String texto) {
    System.out.printf("%n=== %s ===%n", texto);
  }

  private static void linea(String etiqueta, String valor) {
    System.out.printf("  %-38s %s%n", etiqueta + ":", valor);
  }

  private static void eliminarDirectorioRecursivo(Path directorio) throws IOException {
    if (!Files.exists(directorio)) {
      return;
    }
    try (var stream = Files.walk(directorio)) {
      var rutas = stream.sorted(java.util.Comparator.reverseOrder()).toList();
      for (var ruta : rutas) {
        Files.deleteIfExists(ruta);
      }
    }
  }
}
