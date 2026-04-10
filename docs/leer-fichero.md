# Ejemplo 02 — Leer un fichero línea a línea

## Objetivo

El alumno aprenderá a abrir un fichero de texto existente y recorrer su
contenido línea a línea usando la API NIO.2 y la API de Streams. Al finalizar
comprenderá la diferencia entre leer todo el fichero de golpe y procesarlo de
forma **perezosa** (lazy), una técnica esencial para ficheros de gran tamaño.

---

## Conceptos clave

- **`Files.lines(path, charset)`**: devuelve un `Stream<String>` que lee el
  fichero línea a línea de forma perezosa.
- **`try-with-resources`** con Streams: los `Stream` que envuelven recursos de
  E/S también deben cerrarse explícitamente.
- **`Files.exists(path)`**: comprueba que el fichero existe antes de intentar
  leerlo.
- **`Path.getFileName()`**: obtiene solo el nombre del fichero, sin la ruta
  completa.
- **Argumento de línea de comandos**: cómo pasar datos al programa al ejecutarlo.

---

## Explicación paso a paso

### 1. Validación de los argumentos y del fichero

```java
if (args.length == 0) {
    System.err.println("Uso: LeerFichero <ruta-del-fichero>");
    return;
}

var ruta = Path.of(args[0]);

if (!Files.exists(ruta)) {
    System.err.printf("El fichero no existe: %s%n", ruta.toAbsolutePath());
    return;
}
```

Antes de intentar leer el fichero, comprobamos dos cosas:

1. **Que el usuario ha proporcionado un argumento.** `args` es el array de
   argumentos que se pasan al programa al ejecutarlo desde el terminal
   (p. ej. `java LeerFichero salida.txt`). Si está vacío, mostramos el uso
   correcto y salimos.

2. **Que el fichero existe.** `Files.exists(ruta)` devuelve `false` si la ruta
   no existe o no es accesible. Informar al usuario con un mensaje claro es
   mejor que dejar que la JVM lance una excepción críptica.

> **`System.err` vs `System.out`**: los mensajes de error se envían a
> `System.err` (flujo de error estándar) y los mensajes normales a `System.out`.
> Esto permite que el usuario o un script redirija ambos flujos por separado.

---

### 2. Abrir el Stream de líneas con `try-with-resources`

```java
try (var lineas = Files.lines(ruta, StandardCharsets.UTF_8)) {
    ...
}
```

`Files.lines()` devuelve un **`Stream<String>`** donde cada elemento es una
línea del fichero. A diferencia de `Files.readAllLines()`, que carga todo el
fichero en memoria de una vez, `Files.lines()` lee el fichero de forma
**perezosa**: solo obtiene la siguiente línea cuando el Stream la necesita.

Imagina una cinta transportadora en una fábrica: `readAllLines()` descarga
todos los paquetes del camión antes de empezar a procesarlos, mientras que
`lines()` va cogiendo un paquete cada vez conforme la cadena de producción lo
pide. Para ficheros de cientos de megabytes, la diferencia es enorme.

Como el Stream abre internamente un canal hacia el fichero, **debemos cerrarlo**
cuando terminemos. El bloque `try-with-resources` lo hace automáticamente.

---

### 3. Recorrer y mostrar las líneas

```java
var contador = new int[]{1};
lineas.forEach(linea -> {
    System.out.printf("%4d │ %s%n", contador[0], linea);
    contador[0]++;
});
```

Usamos `forEach` para iterar sobre cada línea del Stream y la imprimimos
formateada con su número de línea.

**¿Por qué `new int[]{1}` y no una variable `int` normal?**  
Las lambdas en Java solo pueden capturar variables del entorno circundante si
son **efectivamente finales** (no se reasignan). Un `int` normal no se puede
modificar dentro de una lambda, pero el contenido de un array sí. Es un
truco habitual cuando necesitamos un contador mutable dentro de una lambda.

El formato `%4d │ %s%n` imprime el número con 4 dígitos de ancho
(para que las líneas queden alineadas), un separador visual y el texto.

---

## Ejemplo de uso / Salida esperada

Suponiendo que `salida.txt` contiene (generado con el Ejemplo 01):

```
Hola mundo
Esta es la segunda línea
Y esta la tercera
```

**Ejecución:**

```
java LeerFichero salida.txt
```

**Salida en pantalla:**

```
--- Contenido de: salida.txt ---
   1 │ Hola mundo
   2 │ Esta es la segunda línea
   3 │ Y esta la tercera
--- Fin del fichero ---
```

**Si el fichero no existe:**

```
El fichero no existe: C:\Users\alumno\proyecto\noexiste.txt
```

---

## Para saber más

- [`Files.lines()` (Javadoc oficial)](https://docs.oracle.com/en/java/docs/api/java.base/java/nio/file/Files.html#lines(java.nio.file.Path,java.nio.charset.Charset))
- [`Files.readAllLines()` (Javadoc oficial)](https://docs.oracle.com/en/java/docs/api/java.base/java/nio/file/Files.html#readAllLines(java.nio.file.Path,java.nio.charset.Charset))
- [Stream (Javadoc oficial)](https://docs.oracle.com/en/java/docs/api/java.base/java/util/stream/Stream.html)
