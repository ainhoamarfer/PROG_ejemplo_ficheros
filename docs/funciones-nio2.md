# Ejemplo 03 — Funciones más comunes de `Path` y `Files`

## Objetivo

El alumno conocerá las operaciones más habituales de la API NIO.2: construir y
navegar rutas con `Path`, consultar metadatos de ficheros, leer y escribir texto,
copiar, mover y eliminar ficheros, y listar el contenido de un directorio.
El programa crea una estructura temporal, demuestra cada operación y la limpia,
por lo que puede ejecutarse sin preparar nada.

---

## Conceptos clave

- **`Path`**: representa una ruta del sistema de ficheros (fichero o directorio).
- **`Files`**: clase de utilidad con métodos estáticos para casi cualquier
  operación sobre ficheros y directorios.
- **`Files.createTempDirectory()`**: crea un directorio temporal gestionado por
  el SO.
- **`Files.walk()` / `Files.list()`**: recorre el árbol de directorios.
- **`StandardCopyOption`**: opciones de comportamiento para copias y movimientos.
- Bloque `try/finally` para garantizar la limpieza aun si ocurre un error.

---

## Explicación paso a paso

### 1. Operaciones sobre `Path`

```java
Path fichero = base.resolve("documentos").resolve("notas.txt");

fichero.toString()          // ruta completa como String
fichero.getFileName()       // último segmento: "notas.txt"
fichero.getParent()         // ruta del directorio que lo contiene
fichero.toAbsolutePath()    // ruta absoluta desde la raíz del sistema
fichero.normalize()         // elimina ".." y "." redundantes
fichero.getNameCount()      // número de segmentos en la ruta
fichero.getName(0)          // segmento en la posición indicada
fichero.endsWith("notas.txt") // comprueba el final de la ruta
base.relativize(fichero)    // ruta relativa de fichero respecto a base
```

`Path` **no representa el fichero real** del disco hasta que se llama a algún
método de `Files`. Es solo una descripción de una ruta, como una dirección
escrita en un papel: no dice nada sobre si la casa existe o no.

`resolve()` encadena segmentos de ruta: `base.resolve("a").resolve("b.txt")`
equivale a `base/a/b.txt`. Es la forma segura de construir rutas sin
concatenar Strings manualmente (lo que daría problemas en distintos SO).

---

### 2. Metadatos con `Files`

```java
Files.exists(fichero)               // ¿existe en disco?
Files.isRegularFile(fichero)        // ¿es un fichero (no directorio)?
Files.isDirectory(fichero)          // ¿es un directorio?
Files.isReadable(fichero)           // ¿tenemos permiso de lectura?
Files.isWritable(fichero)           // ¿tenemos permiso de escritura?
Files.size(fichero)                 // tamaño en bytes
Files.getLastModifiedTime(fichero)  // fecha y hora de última modificación
Files.probeContentType(fichero)     // tipo MIME ("text/plain", "image/png"…)
```

Estas llamadas son baratas (no leen el contenido del fichero) y son el primer
paso antes de abrir cualquier fichero: siempre debemos comprobar que existe y
que tenemos permisos antes de intentar leerlo.

---

### 3. Lectura y escritura

| Método | Cuándo usarlo |
|---|---|
| `Files.writeString(path, texto, charset)` | Escribir un `String` completo |
| `Files.write(path, líneas, charset)` | Escribir una `List<String>` (una línea por elemento) |
| `Files.readString(path, charset)` | Leer todo el fichero como un `String` |
| `Files.readAllLines(path, charset)` | Leer todas las líneas como `List<String>` |
| `Files.lines(path, charset)` | Leer línea a línea de forma perezosa (Stream) |

```java
// Escritura de un String completo
Files.writeString(fichero, contenido, StandardCharsets.UTF_8);

// Escritura de una lista de Strings (cada elemento → una línea)
List<String> lineas = List.of("Primera línea", "Segunda línea", "Tercera línea");
Files.write(fichero, lineas, StandardCharsets.UTF_8);

// Lectura completa como String
String texto = Files.readString(fichero, StandardCharsets.UTF_8);

// Lectura como lista de líneas
List<String> lista = Files.readAllLines(fichero, StandardCharsets.UTF_8);

// Lectura perezosa — requiere try-with-resources
try (var stream = Files.lines(fichero, StandardCharsets.UTF_8)) {
    stream.forEach(System.out::println);
}
```

#### ¿Por qué `write`, `writeString`, `readString` y `readAllLines` no necesitan `try-with-resources`?

Estos cuatro métodos son **operaciones completas en una sola llamada**: Java abre el fichero
internamente, realiza toda la operación y lo cierra antes de devolver el control al programa,
ocurra un error o no. El cierre está garantizado por el propio método, así que no hay ningún
recurso abierto "en manos" de nuestro código que debamos cerrar nosotros.

`Files.lines()` funciona de forma diferente: devuelve un `Stream<Path>` **perezoso**, es decir,
no lee el fichero de golpe sino que mantiene abierto un canal al disco para ir leyendo línea a
línea a medida que el Stream las va necesitando. Ese canal queda abierto en nuestro código hasta
que alguien lo cierre. Si no usamos `try-with-resources` y se lanza una excepción antes de
terminar de procesar el Stream, el canal quedará abierto indefinidamente (fuga de recursos).

> **Regla práctica**: si un método devuelve un `Stream` o un objeto que representa una conexión
> a un recurso externo (fichero, red, base de datos), es probable que implemente `AutoCloseable`
> y por tanto requiera `try-with-resources`.

Usa `lines()` cuando el fichero pueda ser grande y no quieras cargarlo entero en memoria; usa
`readAllLines()` o `readString()` cuando el fichero sea pequeño y quieras simplicidad.

---

### 4. Copia y movimiento

```java
// Copia: si destino ya existe, REPLACE_EXISTING lo sobreescribe
Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);

// Movimiento (también sirve para renombrar)
Files.move(origen, destino, StandardCopyOption.REPLACE_EXISTING);

// Borrado: no lanza excepción si el fichero no existe
Files.deleteIfExists(ruta);
```

`move()` en la misma partición equivale a renombrar el fichero (operación
instantánea). Entre particiones diferentes, Java copia el contenido y borra
el original.

---

### 5. Listado de directorios

```java
// list() — solo el nivel inmediato (no entra en subdirectorios)
try (var stream = Files.list(directorio)) {
    stream.forEach(System.out::println);
}

// walk() — recorre el árbol completo de forma recursiva
try (var stream = Files.walk(directorio)) {
    stream.filter(Files::isRegularFile).forEach(System.out::println);
}
```

`Files.walk()` devuelve un `Stream<Path>` que incluye el propio directorio
raíz como primer elemento. Combínalo con `filter()` para quedarte solo con
ficheros, o con `sorted()` para ordenarlos.

> **Importante**: tanto `Files.list()` como `Files.walk()` devuelven Streams
> que mantienen abierto un canal al sistema de ficheros. Ciérralos siempre con
> `try-with-resources`.

---

### 6. Limpieza: borrar un directorio recursivamente

```java
try (var stream = Files.walk(directorio)) {
    var rutas = stream.sorted(Comparator.reverseOrder()).toList();
    for (var ruta : rutas) {
        Files.deleteIfExists(ruta);
    }
}
```

`Files.delete()` solo borra directorios **vacíos**. Para borrar un árbol
completo hay que recorrerlo con `walk()`, ordenar las rutas en **orden
inverso** (los hijos antes que los padres) y eliminarlas una a una.

---

## Ejemplo de uso / Salida esperada

```
=== 1. Operaciones sobre Path ===
  Path completo:                       C:\...\nio2-demo-...\documentos\notas.txt
  Nombre del fichero:                  notas.txt
  Directorio padre:                    C:\
  ...\nio2-demo-...\documentos
  Ruta absoluta:                       C:\...\nio2-demo-...\documentos\notas.txt
  Normalizado:                         C:\...\nio2-demo-...\documentos\notas.txt
  Número de segmentos:                 6
  Segmento 0:                          C:\
  ¿Termina en .txt?:                   true
  Ruta relativa desde base:            documentos\notas.txt

=== 2. Metadatos con Files ===
  ¿Existe?:                            true
  ¿Es fichero regular?:                true
  ¿Es directorio?:                     false
  ¿Se puede leer?:                     true
  ¿Se puede escribir?:                 true
  Tamaño (bytes):                      18
  Última modificación:                 2026-04-07T...
  Tipo MIME (probing):                 text/plain

=== 3. Lectura y escritura con Files ===
  Escrito con writeString:             lectura.txt
  readString (chars):                  45
  readAllLines (líneas):               3
  lines() (perezoso):
    → Primera línea
    → Segunda línea
    → Tercera línea

=== 4. Copia y movimiento con Files ===
  copy → copia.txt existe:             true
  move → movido.txt existe:            true
  move → copia.txt ya no existe:       true
  deleteIfExists → origen eliminado:   true

=== 5. Listado de directorios con Files ===
  list() (nivel inmediato de base):
    metadatos.txt
    lectura.txt
    sub
    c.txt
  walk() (árbol completo):
    metadatos.txt
    lectura.txt
    c.txt
    sub\a.txt
    sub\b.txt

[Limpieza] Directorio temporal eliminado: C:\...\nio2-demo-...
```

---

## Para saber más

- [`Path` (Javadoc oficial)](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html)
- [`Files` (Javadoc oficial)](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html)
- [`StandardCopyOption` (Javadoc oficial)](https://docs.oracle.com/javase/8/docs/api/java/nio/file/StandardCopyOption.html)
- [Guía NIO.2 — Oracle Tutorials](https://docs.oracle.com/javase/tutorial/essential/io/fileio.html)
