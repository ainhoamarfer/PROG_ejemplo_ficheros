package com.permuta;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Ejemplo 04 — Guardar y recuperar objetos serializados en un fichero binario.
 *
 * <p>El programa crea una lista de objetos {@link Contacto}, los serializa en un
 * fichero binario (.dat) y los recupera de vuelta para mostrarlos por pantalla.
 */
public final class SerializarObjetos {

  /** Ruta del fichero binario donde se guardarán los objetos. */
  private static final Path RUTA_FICHERO = Path.of("contactos.dat");

  private SerializarObjetos() {
  }

  // -------------------------------------------------------------------------
  // Record serializable
  // -------------------------------------------------------------------------

  /**
   * Representa un contacto de agenda. Al implementar {@link Serializable},
   * sus instancias pueden guardarse directamente en un fichero binario.
   *
   * @param nombre   nombre completo del contacto
   * @param email    dirección de correo electrónico
   * @param telefono número de teléfono
   */
  record Contacto(String nombre, String email, String telefono) implements Serializable {

    /** Identificador de versión de serialización. */
    @Serial
    private static final long serialVersionUID = 1L;
  }

  // -------------------------------------------------------------------------
  // main
  // -------------------------------------------------------------------------

  /**
   * Punto de entrada del programa.
   *
   * @param args no se usan en este ejemplo
   * @throws IOException            si ocurre un error de entrada/salida
   * @throws ClassNotFoundException si el fichero contiene una clase desconocida
   */
  public static void main(String[] args) throws IOException, ClassNotFoundException {
    var contactos = List.of(
        new Contacto("Ana García",    "ana@ejemplo.com",   "600 111 222"),
        new Contacto("Luis Martínez", "luis@ejemplo.com",  "611 333 444"),
        new Contacto("Sara López",    "sara@ejemplo.com",  "622 555 666")
    );

    guardarContactos(RUTA_FICHERO, contactos);
    System.out.printf("Fichero creado: %s  (%.0f bytes)%n%n",
        RUTA_FICHERO.toAbsolutePath(), (double) Files.size(RUTA_FICHERO));

    List<Contacto> recuperados = cargarContactos(RUTA_FICHERO);
    System.out.printf("Contactos recuperados (%d):%n", recuperados.size());
    recuperados.forEach(c ->
        System.out.printf("  %-20s  %-25s  %s%n", c.nombre(), c.email(), c.telefono())
    );

    Files.deleteIfExists(RUTA_FICHERO);
  }

  // -------------------------------------------------------------------------
  // Serialización
  // -------------------------------------------------------------------------

  /**
   * Serializa una lista de contactos y la guarda en el fichero indicado.
   *
   * @param ruta      ruta del fichero de destino
   * @param contactos lista de contactos a guardar
   * @throws IOException si ocurre un error de escritura
   */
  private static void guardarContactos(Path ruta, List<Contacto> contactos)
      throws IOException {
    try (var out = new ObjectOutputStream(Files.newOutputStream(ruta))) {
      out.writeObject(contactos);
    }
    System.out.printf("Guardados %d contactos en '%s'.%n", contactos.size(), ruta.getFileName());
  }

  // -------------------------------------------------------------------------
  // Deserialización
  // -------------------------------------------------------------------------

  /**
   * Lee y deserializa la lista de contactos almacenada en el fichero indicado.
   *
   * @param ruta ruta del fichero fuente
   * @return lista de contactos recuperada del fichero
   * @throws IOException            si ocurre un error de lectura
   * @throws ClassNotFoundException si la clase deserializada no está en el classpath
   */
  @SuppressWarnings("unchecked")
  private static List<Contacto> cargarContactos(Path ruta)
      throws IOException, ClassNotFoundException {
    try (var in = new ObjectInputStream(Files.newInputStream(ruta))) {
      return (List<Contacto>) in.readObject();
    }
  }
}
