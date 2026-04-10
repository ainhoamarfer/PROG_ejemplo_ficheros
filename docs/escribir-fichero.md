# Ejemplo 01 — Escribir en un fichero las líneas introducidas por el usuario

## Objetivo

El alumno aprenderá a abrir (o crear) un fichero de texto y a escribir en él
líneas que el usuario introduce por consola. Al finalizar el programa, el
fichero contendrá exactamente lo que el usuario ha escrito, línea a línea.

---

## Conceptos clave

- **API NIO.2** (`java.nio.file`): la forma moderna de trabajar con ficheros en Java.
- **`Path`**: representa una ruta del sistema de ficheros.
- **`Files.write()`**: escribe una colección de líneas en un fichero.
- **`Scanner`** con `try-with-resources`: lee texto de la entrada estándar y garantiza el cierre del recurso.
- **`StandardCharsets.UTF_8`**: codificación de caracteres explícita para evitar problemas con tildes y caracteres especiales.
- **Clase de utilidad**: clase con constructor privado y métodos estáticos.

---

## Explicación paso a paso

### 1. Constantes de clase

```java
private static final String CENTINELA = "FIN";
private static final Path RUTA_FICHERO = Path.of("salida.txt");
```

Declaramos dos constantes:
- `CENTINELA` es la palabra que el usuario escribe para indicar que ha terminado
  de introducir texto. Usar una constante con nombre evita los "números/textos
  mágicos" repartidos por el código.
- `RUTA_FICHERO` es un objeto `Path` que representa la ruta del fichero de
  salida. `Path.of("salida.txt")` crea una ruta **relativa** al directorio
  desde el que se ejecuta el programa. Si el fichero no existe, `Files.write()`
  lo creará automáticamente.

> **¿Por qué `Path` en lugar de `File`?**  
> `Path` pertenece a la API NIO.2 (introducida en Java 7) y es más completa,
> legible y segura que la antigua clase `java.io.File`. En este módulo usaremos
> siempre NIO.2.

---

### 2. El método `main`

```java
public static void main(String[] args) throws IOException {
    List<String> lineas = leerLineasDelUsuario();
    escribirFichero(RUTA_FICHERO, lineas);
    System.out.printf("Fichero guardado en: %s%n", RUTA_FICHERO.toAbsolutePath());
    System.out.printf("Líneas escritas: %d%n", lineas.size());
}
```

El método `main` actúa como **coordinador**: delega la lógica real en métodos
auxiliares con nombres descriptivos. Así el código se lee casi como un
enunciado en prosa: _"lee las líneas, escríbelas en el fichero, informa al usuario"_.

Declaramos `throws IOException` porque escribir en disco puede fallar (disco
lleno, permisos insuficientes, etc.). En lugar de silenciar el error con un
`catch` vacío, dejamos que la excepción se propague: la JVM mostrará un mensaje
claro si algo va mal.

---

### 3. Leer líneas desde el terminal

```java
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
```

Puntos importantes:

- **`Scanner(System.in, StandardCharsets.UTF_8)`**: abrimos un lector sobre la
  entrada estándar del sistema (`System.in`) e indicamos explícitamente la
  codificación UTF-8 para que tildes y caracteres especiales se lean
  correctamente.

- **`try-with-resources`**: el bloque `try (...)` garantiza que el `Scanner`
  se cierra automáticamente al salir del bloque, incluso si ocurre una
  excepción. Piénsalo como un "contrato de devolución": sea cual sea el motivo
  por el que salimos del bloque, Java devuelve el recurso.

- **`scanner.hasNextLine()`**: comprueba si hay más líneas disponibles antes de
  intentar leerlas, evitando excepciones por fin de entrada inesperado.

- **`CENTINELA.equalsIgnoreCase(linea)`**: comparamos con el centinela sin
  distinguir mayúsculas (`fin`, `FIN`, `Fin`… todas sirven). Usamos
  `CENTINELA.equals(...)` en lugar de `linea.equals(CENTINELA)` como buena
  práctica: si `linea` fuera `null`, la primera forma no lanzaría
  `NullPointerException`.

- **`List.copyOf(lineas)`**: devolvemos una copia **inmutable** de la lista.
  Esto sigue el principio de menor sorpresa: el llamante no puede modificar
  accidentalmente la lista una vez devuelta.

---

### 4. Escribir el fichero

```java
private static void escribirFichero(Path ruta, List<String> lineas) throws IOException {
    Files.write(ruta, lineas, StandardCharsets.UTF_8);
}
```

`Files.write(ruta, lineas, charset)` hace tres cosas en una sola llamada:

1. **Crea el fichero** si no existe (o lo sobreescribe si ya existía).
2. **Escribe cada elemento** de la lista como una línea, añadiendo el separador
   de línea del sistema operativo entre ellas.
3. **Cierra el fichero** automáticamente al terminar.

Indicar `StandardCharsets.UTF_8` garantiza que las tildes y otros caracteres
especiales se guardan correctamente sin depender de la configuración del sistema
donde se ejecute el programa.

---

## Ejemplo de uso / Salida esperada

**Sesión en el terminal:**

```
Escribe líneas de texto y pulsa Enter. Escribe "FIN" para terminar.
Hola mundo
Esta es la segunda línea
Y esta la tercera
FIN
Fichero guardado en: C:\Users\alumno\proyecto\salida.txt
Líneas escritas: 3
```

**Contenido de `salida.txt` tras la ejecución:**

```
Hola mundo
Esta es la segunda línea
Y esta la tercera
```

---

## Para saber más

- [`Files` (Javadoc oficial)](https://docs.oracle.com/en/java/docs/api/java.base/java/nio/file/Files.html)
- [`Path` (Javadoc oficial)](https://docs.oracle.com/en/java/docs/api/java.base/java/nio/file/Path.html)
- [`StandardCharsets` (Javadoc oficial)](https://docs.oracle.com/en/java/docs/api/java.base/java/nio/charset/StandardCharsets.html)
- [try-with-resources — Tutorial Oracle](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html)
