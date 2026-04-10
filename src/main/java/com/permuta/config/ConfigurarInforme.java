package com.permuta.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ejemplo 07 — Cargar un fichero de configuración JSON con Gson y generar
 * informes en distintos formatos según dicha configuración.
 *
 * <p>El programa lee {@code datos/config-informe.json}, deserializa sus valores
 * en un objeto de configuración y genera un informe de productos en el formato
 * indicado: {@code tabla}, {@code csv} o {@code json}.
 *
 * <p>La idea clave es que <strong>cambiar el fichero JSON cambia el
 * comportamiento del programa sin tocar el código fuente</strong>. Este patrón
 * se llama externalización de la configuración y es muy habitual en aplicaciones
 * reales (Spring Boot, Quarkus, etc.).
 */
public final class ConfigurarInforme {

  /** Ruta del fichero de configuración JSON. */
  private static final Path RUTA_CONFIG = Path.of("datos/config-informe.json");

  /** Formateador de fecha/hora para la cabecera del informe. */
  private static final DateTimeFormatter FMT_FECHA =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private ConfigurarInforme() {
  }

  // -------------------------------------------------------------------------
  // Records — estructura del JSON
  // -------------------------------------------------------------------------

  /**
   * Un producto del catálogo. Mapea cada objeto del array {@code productos}
   * del JSON.
   *
   * @param nombre  nombre del producto
   * @param precio  precio unitario en euros
   * @param stock   unidades disponibles
   */
  record Producto(String nombre, double precio, int stock) {
  }

  /**
   * Configuración completa de la aplicación. Gson mapea automáticamente cada
   * clave del JSON al campo del record con el mismo nombre.
   *
   * @param titulo       título que aparece en la cabecera del informe
   * @param formato      {@code tabla}, {@code csv} o {@code json}
   * @param maxEntradas  límite de productos que se incluyen en el informe
   * @param separador    carácter separador usado en el formato CSV
   * @param mostrarFecha si se incluye la fecha de generación
   * @param rutaSalida   ruta del fichero de salida
   * @param productos    lista completa de productos disponibles en el JSON
   */
  record ConfiguracionInforme(
      String titulo,
      String formato,
      int maxEntradas,
      String separador,
      boolean mostrarFecha,
      String rutaSalida,
      List<Producto> productos) {
  }

  // -------------------------------------------------------------------------
  // main
  // -------------------------------------------------------------------------

  /**
   * Punto de entrada del programa.
   *
   * @param args no se usan en este ejemplo
   * @throws IOException si ocurre un error de E/S
   */
  public static void main(String[] args) throws IOException {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // 1. Deserializar el fichero JSON en el objeto de configuración
    ConfiguracionInforme config = cargarConfig(RUTA_CONFIG, gson);
    System.out.printf("Configuracion cargada -> formato: %s | maxEntradas: %d%n",
        config.formato(), config.maxEntradas());

    // 2. Elegir el generador según el valor de "formato" en el JSON
    //    Cambiar ese campo en config-informe.json produce una salida diferente
    //    sin modificar ni una sola línea de código Java.
    String contenido = switch (config.formato().toLowerCase()) {
      case "tabla" -> generarTabla(config);
      case "csv"   -> generarCsv(config);
      case "json"  -> generarJson(config, gson);
      default      -> throw new IllegalArgumentException(
                          "Formato no reconocido: " + config.formato());
    };

    // 3. Escribir el resultado al fichero indicado en la configuración
    Path rutaSalida = Path.of(config.rutaSalida());
    Files.writeString(rutaSalida, contenido, StandardCharsets.UTF_8);
    System.out.printf("%nInforme escrito en: %s%n%n", rutaSalida.toAbsolutePath());
    System.out.println(contenido);
  }

  // -------------------------------------------------------------------------
  // Carga de configuración
  // -------------------------------------------------------------------------

  /**
   * Lee el fichero JSON y lo convierte en un objeto {@link ConfiguracionInforme}
   * usando Gson.
   *
   * @param ruta ruta del fichero JSON
   * @param gson instancia de Gson ya configurada
   * @return objeto de configuración deserializado
   * @throws IOException si el fichero no existe o no se puede leer
   */
  private static ConfiguracionInforme cargarConfig(Path ruta, Gson gson) throws IOException {
    String json = Files.readString(ruta, StandardCharsets.UTF_8);
    return gson.fromJson(json, ConfiguracionInforme.class);
  }

  // -------------------------------------------------------------------------
  // Variacion 1: tabla de texto
  // -------------------------------------------------------------------------

  /**
   * Genera un informe en forma de tabla de texto decorada con bordes ASCII.
   * Es el formato más visual para leer directamente en consola.
   */
  private static String generarTabla(ConfiguracionInforme config) {
    List<Producto> lista = productosLimitados(config);

    var sb = new StringBuilder();
    sb.append(cabeceraTexto(config));
    sb.append(String.format("%-22s %10s %9s%n", "PRODUCTO", "PRECIO", "STOCK"));
    sb.append("-".repeat(45)).append("\n");
    lista.forEach(p ->
        sb.append(String.format("%-22s %9.2f EUR %5d ud%n",
            p.nombre(), p.precio(), p.stock())));
    sb.append("-".repeat(45)).append("\n");

    double totalPrecio = lista.stream().mapToDouble(Producto::precio).sum();
    long totalStock = lista.stream().mapToLong(Producto::stock).sum();
    sb.append(String.format("Suma precios: %.2f EUR  |  Stock total: %d ud%n",
        totalPrecio, totalStock));

    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Variacion 2: CSV con separador configurable
  // -------------------------------------------------------------------------

  /**
   * Genera un informe en formato CSV. El separador de columnas se toma
   * del campo {@code separador} del JSON (puede ser {@code ;}, {@code ,}, etc.).
   */
  private static String generarCsv(ConfiguracionInforme config) {
    String sep = config.separador();

    String filas = productosLimitados(config).stream()
        .map(p -> String.join(sep,
            p.nombre(),
            String.format("%.2f", p.precio()),
            String.valueOf(p.stock())))
        .collect(Collectors.joining("\n"));

    return String.join(sep, "Producto", "Precio", "Stock") + "\n" + filas + "\n";
  }

  // -------------------------------------------------------------------------
  // Variacion 3: JSON estructurado
  // -------------------------------------------------------------------------

  /**
   * Genera un informe en formato JSON, reutilizando la misma instancia de
   * Gson con {@code setPrettyPrinting()} para que el resultado sea legible.
   */
  private static String generarJson(ConfiguracionInforme config, Gson gson) {
    List<Producto> lista = productosLimitados(config);

    JsonObject obj = new JsonObject();
    obj.addProperty("titulo", config.titulo());
    if (config.mostrarFecha()) {
      obj.addProperty("generado", LocalDateTime.now().format(FMT_FECHA));
    }
    obj.addProperty("totalProductos", lista.size());
    obj.add("productos", gson.toJsonTree(lista));

    return gson.toJson(obj);
  }

  // -------------------------------------------------------------------------
  // Auxiliares
  // -------------------------------------------------------------------------

  /**
   * Devuelve la sublista de productos limitada al valor de {@code maxEntradas}
   * definido en la configuración.
   */
  private static List<Producto> productosLimitados(ConfiguracionInforme config) {
    return config.productos().stream()
        .limit(config.maxEntradas())
        .toList();
  }

  /**
   * Genera la cabecera decorada con título y, opcionalmente, la fecha actual.
   * Solo se usa en el formato tabla.
   */
  private static String cabeceraTexto(ConfiguracionInforme config) {
    var sb = new StringBuilder();
    sb.append("=".repeat(45)).append("\n");
    sb.append("  ").append(config.titulo()).append("\n");
    if (config.mostrarFecha()) {
      sb.append("  Generado: ").append(LocalDateTime.now().format(FMT_FECHA)).append("\n");
    }
    sb.append("=".repeat(45)).append("\n");
    return sb.toString();
  }
}
