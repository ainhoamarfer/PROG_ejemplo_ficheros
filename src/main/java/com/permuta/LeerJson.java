package com.permuta;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Ejemplo 05 — Leer un fichero JSON y convertirlo en objetos Java con Gson.
 *
 * <p>El programa lee un fichero {@code contacto.json}, lo deserializa en un
 * objeto {@link Contacto} usando la librería Gson, muestra sus datos por
 * pantalla y vuelve a serializarlo a JSON para mostrar el resultado.
 */
public final class LeerJson {

  /** Ruta del fichero JSON de entrada. */
  private static final Path RUTA_JSON = Path.of("datos/contacto.json");

  private LeerJson() {
  }

  // -------------------------------------------------------------------------
  // Records que representan la estructura del JSON
  // -------------------------------------------------------------------------

  /**
   * Dirección postal. Mapea el objeto anidado "direccion" del JSON.
   *
   * @param calle        nombre de la calle y número
   * @param ciudad       ciudad
   * @param codigoPostal código postal
   */
  record Direccion(String calle, 
    String ciudad, 
    String codigoPostal) {
  }

  /**
   * Contacto completo. Los nombres de los campos deben coincidir exactamente
   * con las claves del JSON (Gson los empareja por nombre).
   *
   * @param nombre     nombre completo
   * @param email      dirección de correo electrónico
   * @param edad       edad en años
   * @param activo     si el contacto está activo
   * @param direccion  dirección postal (objeto anidado)
   * @param telefonos  lista de teléfonos
   */
  record Contacto(
      String nombre,
      String email,
      int edad,
      boolean activo,
      Direccion direccion,
      List<String> telefonos) {
  }

  // -------------------------------------------------------------------------
  // main
  // -------------------------------------------------------------------------

  /**
   * Punto de entrada del programa.
   *
   * @param args no se usan en este ejemplo
   * @throws IOException si ocurre un error al leer el fichero
   */
  public static void main(String[] args) throws IOException {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // 1. Leer el fichero y deserializar
    Contacto contacto = deserializarJson(RUTA_JSON, gson);

    // 2. Mostrar los datos del objeto
    System.out.println("=== Datos cargados del JSON ===");
    System.out.printf("  Nombre   : %s%n", contacto.nombre());
    System.out.printf("  Email    : %s%n", contacto.email());
    System.out.printf("  Edad     : %d%n", contacto.edad());
    System.out.printf("  Activo   : %b%n", contacto.activo());
    System.out.printf("  Calle    : %s%n", contacto.direccion().calle());
    System.out.printf("  Ciudad   : %s%n", contacto.direccion().ciudad());
    System.out.printf("  C.Postal : %s%n", contacto.direccion().codigoPostal());
    System.out.printf("  Teléfonos: %s%n", contacto.telefonos());

    // 3. Volver a serializar a JSON y mostrarlo
    System.out.println("\n=== Objeto serializado de vuelta a JSON ===");
    System.out.println(gson.toJson(contacto));
  }

  // -------------------------------------------------------------------------
  // Deserialización
  // -------------------------------------------------------------------------

  /**
   * Lee un fichero JSON y lo convierte en un objeto del tipo indicado.
   *
   * @param ruta ruta del fichero JSON
   * @param gson instancia de Gson configurada
   * @return objeto {@link Contacto} con los datos del JSON
   * @throws IOException si ocurre un error al leer el fichero
   */
  private static Contacto deserializarJson(Path ruta, Gson gson) throws IOException {
    String json = Files.readString(ruta, StandardCharsets.UTF_8);
    return gson.fromJson(json, Contacto.class);
  }
}
