# Ejemplo 07 — Configuración de la aplicación con JSON y Gson

## Objetivo

Aprender a **externalizar la configuración de un programa** en un fichero JSON y
usar la librería **Gson** para cargarlo automáticamente en objetos Java (records).
El objetivo final es que cambiar el fichero JSON modifique el comportamiento del
programa **sin tocar el código fuente**: distintos formatos de salida, distintos
límites de datos, distinto fichero de destino.

---

## Conceptos clave

| Concepto | API / Herramienta |
|---|---|
| Leer texto de un fichero | `Files.readString(Path, Charset)` (NIO.2) |
| Deserializar JSON → Java | `Gson.fromJson(String, Class<T>)` |
| Modelar datos inmutables | `record` (Java 16+) |
| Selección por valor de config | `switch` expression con patrones de texto |
| Limitar y transformar colecciones | `Stream.limit()`, `Stream.map()`, `Collectors.joining()` |
| Construir JSON dinámicamente | `JsonObject` + `Gson.toJsonTree()` |

---

## Estructura del proyecto

```
datos/
    config-informe.json          ← fichero de configuración (editable)
src/main/java/com/permuta/
    config/
        ConfigurarInforme.java   ← clase principal del ejemplo
```

El código está en el subpaquete `com.permuta.config` para separarlo del resto de
ejemplos y mostrar cómo organizar el código en paquetes temáticos.

---

## El fichero de configuración

```json
{
  "titulo": "Informe de Inventario",
  "formato": "tabla",
  "maxEntradas": 4,
  "separador": ";",
  "mostrarFecha": true,
  "rutaSalida": "salida-informe.txt",
  "productos": [
    { "nombre": "Laptop ProMax",    "precio": 1249.99, "stock":  8 },
    { "nombre": "Raton Laser",      "precio":   24.99, "stock": 42 },
    ...
  ]
}
```

Cada clave del JSON se corresponde exactamente con un campo del record
`ConfiguracionInforme`. Gson hace el mapeo de forma automática buscando coincidencias
de nombre.

---

## Explicación paso a paso

### 1. Records — modelar el JSON con tipos Java

```java
record Producto(String nombre, double precio, int stock) { }

record ConfiguracionInforme(
    String titulo,
    String formato,
    int maxEntradas,
    String separador,
    boolean mostrarFecha,
    String rutaSalida,
    List<Producto> productos) { }
```

Un `record` es una clase inmutable que solo almacena datos. Java genera
automáticamente el constructor, los getters (`nombre()`, `precio()`…), `equals`,
`hashCode` y `toString`. Es la forma más limpia de representar la estructura de un
JSON porque **el nombre del campo del record debe coincidir con la clave JSON**.

> **Analogía:** el record es como el molde de un objeto, y el JSON es el yeso
> que se vierte en él. Gson rellena cada hueco del molde con el valor correspondiente.

---

### 2. Cargar el JSON con Gson

```java
private static ConfiguracionInforme cargarConfig(Path ruta, Gson gson) throws IOException {
    String json = Files.readString(ruta, StandardCharsets.UTF_8);
    return gson.fromJson(json, ConfiguracionInforme.class);
}
```

`Files.readString` lee todo el contenido del fichero en una sola llamada y lo
devuelve como `String`. Luego `gson.fromJson` deserializa ese texto: busca cada
clave JSON y la asigna al campo del record con el mismo nombre.

El campo `productos` (un array en el JSON) se convierte automáticamente en
`List<Producto>` porque Gson entiende genéricos cuando se le indica el tipo
destino.

---

### 3. El switch expression — elegir la variación

```java
String contenido = switch (config.formato().toLowerCase()) {
    case "tabla" -> generarTabla(config);
    case "csv"   -> generarCsv(config);
    case "json"  -> generarJson(config, gson);
    default      -> throw new IllegalArgumentException(
                        "Formato no reconocido: " + config.formato());
};
```

El `switch` expression (Java 14+) selecciona el generador adecuado según el
valor de `"formato"` en el JSON. Si cambias `"tabla"` por `"csv"` en el JSON y
vuelves a ejecutar, obtienes una salida completamente diferente sin recompilar.

El `default` lanza una excepción descriptiva si alguien escribe un formato
desconocido: mejor fallar pronto con un mensaje claro que producir una salida
silenciosamente vacía.

---

### 4. Variación 1 — tabla de texto

```java
sb.append(String.format("%-22s %9.2f EUR %5d ud%n",
    p.nombre(), p.precio(), p.stock()));
```

`String.format` con especificadores de ancho (`%-22s` alinea a la izquierda en
22 caracteres, `%9.2f` reserva 9 caracteres con 2 decimales) produce columnas
alineadas sin necesitar ninguna librería externa.

El número de filas está limitado por `config.maxEntradas()` gracias a
`productosLimitados(config)`, que aplica `Stream.limit()`:

```java
private static List<Producto> productosLimitados(ConfiguracionInforme config) {
    return config.productos().stream()
        .limit(config.maxEntradas())
        .toList();
}
```

---

### 5. Variación 2 — CSV con separador configurable

```java
String filas = productosLimitados(config).stream()
    .map(p -> String.join(sep, p.nombre(),
                               String.format("%.2f", p.precio()),
                               String.valueOf(p.stock())))
    .collect(Collectors.joining("\n"));
```

`Stream.map` transforma cada `Producto` en una línea de texto usando el
separador almacenado en `config.separador()`. `Collectors.joining("\n")` une
todas las líneas con un salto de línea.

Si cambias `"separador": ";"` por `"separador": ","` en el JSON, el fichero
de salida pasa de ser un CSV con punto y coma a uno con coma, sin cambiar código.

---

### 6. Variación 3 — JSON re-serializado

```java
JsonObject obj = new JsonObject();
obj.addProperty("titulo", config.titulo());
obj.addProperty("generado", LocalDateTime.now().format(FMT_FECHA));
obj.addProperty("totalProductos", lista.size());
obj.add("productos", gson.toJsonTree(lista));
return gson.toJson(obj);
```

`JsonObject` es la clase de Gson para construir un objeto JSON dinámicamente.
`gson.toJsonTree(lista)` convierte una `List<Producto>` en un `JsonArray`
(el equivalente Gson de un array JSON), y `gson.toJson(obj)` serializa todo
el objeto a texto con indentación porque se creó con `setPrettyPrinting()`.

---

## Variaciones que puedes probar

Edita `datos/config-informe.json` y ejecuta de nuevo el programa:

| Cambio en el JSON | Efecto en la salida |
|---|---|
| `"formato": "csv"` | Genera un CSV en lugar de tabla |
| `"formato": "json"` | Genera un JSON formateado |
| `"maxEntradas": 6` | Incluye los 6 productos en lugar de 4 |
| `"separador": ","` | El CSV usa coma en lugar de punto y coma |
| `"mostrarFecha": false` | Elimina la línea de fecha de la cabecera |
| `"rutaSalida": "otro.txt"` | Escribe el resultado en un fichero diferente |

---

## Ejemplo de salida

### Con `"formato": "tabla"` y `"maxEntradas": 4`

```
=============================================
  Informe de Inventario
  Generado: 07/04/2026 10:30
=============================================
PRODUCTO                    PRECIO     STOCK
---------------------------------------------
Laptop ProMax           1249.99 EUR     8 ud
Raton Laser               24.99 EUR    42 ud
Teclado Mecanico          89.99 EUR    15 ud
Monitor 4K               449.99 EUR     5 ud
---------------------------------------------
Suma precios: 1814.96 EUR  |  Stock total: 70 ud
```

### Con `"formato": "csv"` y `"separador": ";"`

```
Producto;Precio;Stock
Laptop ProMax;1249.99;8
Raton Laser;24.99;42
Teclado Mecanico;89.99;15
Monitor 4K;449.99;5
```

### Con `"formato": "json"`

```json
{
  "titulo": "Informe de Inventario",
  "generado": "07/04/2026 10:30",
  "totalProductos": 4,
  "productos": [
    { "nombre": "Laptop ProMax", "precio": 1249.99, "stock": 8 },
    ...
  ]
}
```

---

## Para saber más

- [Gson User Guide](https://github.com/google/gson/blob/main/UserGuide.md) — documentación oficial de Gson.
- [java.nio.file.Files (Javadoc)](https://docs.oracle.com/en/java/docs/api/java.base/java/nio/file/Files.html) — todos los métodos de lectura/escritura NIO.2.
- [Records (JEP 395)](https://openjdk.org/jeps/395) — especificación oficial de los records en Java.
- [Switch expressions (JEP 361)](https://openjdk.org/jeps/361) — especificación oficial del `switch` expression.
