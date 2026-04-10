# Instrucciones del Agente — Ejemplos de Clase FP DAM

## Contexto del proyecto

Soy profesor de FP DAM (Desarrollo de Aplicaciones Multiplataforma) y este proyecto contiene
ejemplos de clase sobre manejo de ficheros en Java. Los ejemplos están destinados a alumnos que
están aprendiendo Java y necesitan explicaciones claras y detalladas.

- **Proyecto**: Maven, `com.permuta`, Java 25
- **Testing**: JUnit 5 (`junit-jupiter`)
- **Objetivo pedagógico**: Que el alumno entienda qué hace el código y por qué, no sólo que lo copie

---

## Tecnología y estilo de código

### Java 25
- Usa siempre **Java 25** y sus características modernas.
- Usa **records** cuando el tipo represente datos inmutables (p. ej. una línea de un CSV, una entrada de log, un resultado de lectura).
- Usa **text blocks** (`"""..."""`) para literales de texto multilínea (cabeceras CSV, mensajes, etc.).
- Usa **pattern matching** (`instanceof`, `switch` expressions con patrones) cuando aporte claridad.
- Usa **`var`** sólo cuando el tipo sea evidente por el contexto; evítalo si añade ambigüedad.

### Programación funcional
- Prioriza la **API de Streams** (`stream()`, `map()`, `filter()`, `collect()`, etc.) para transformar y procesar datos de ficheros.
- Usa **referencias a métodos** (`Clase::método`) en lugar de lambdas cuando mejoren la legibilidad.
- Usa **`Optional`** para manejar resultados que pueden estar vacíos en lugar de retornar `null`.
- Evita bucles `for`/`while` cuando un Stream sea más expresivo.

### Manejo de ficheros (NIO.2)
- Usa exclusivamente la **API NIO.2** (`java.nio.file`): `Path`, `Files`, `Paths`.
- Preferencias de métodos:
  - Lectura completa de texto: `Files.readString(path)` / `Files.readAllLines(path)`
  - Escritura de texto: `Files.writeString(path, contenido)` / `Files.write(path, líneas)`
  - Streaming línea a línea: `Files.lines(path)` (cierra el stream con `try-with-resources`)
  - Operaciones de metadatos: `Files.exists()`, `Files.size()`, `Files.getLastModifiedTime()`
- Usa siempre **`try-with-resources`** cuando trabajes con streams o canales de I/O.
- Indica explícitamente el charset (`StandardCharsets.UTF_8`) en todas las operaciones de texto.

### Calidad del código
- Sigue las convenciones de estilo de Google Java Style (Checkstyle ya configurado en el proyecto).
- Las clases de utilidad deben tener constructor privado y métodos estáticos.
- No uses `System.exit()` ni capturas genéricas de `Exception` sin justificación.
- Cada ejemplo debe poder compilar y ejecutarse de forma independiente.

---

## Estructura de cada ejemplo

Cada ejemplo consta **siempre de dos archivos**:

### 1. Archivo Java — `src/main/java/com/permuta/<NombreEjemplo>.java`
- Package: `com.permuta`
- Clase con `main` ejecutable y métodos auxiliares bien nombrados.
- Sin dependencias externas (sólo JDK estándar).

### 2. Archivo de apuntes — `docs/<nombre-ejemplo>.md`
El `.md` acompaña **siempre** al `.java` y sigue esta estructura:

```
# Título del ejemplo

## Objetivo
Qué aprenderá el alumno con este ejemplo (1-3 frases).

## Conceptos clave
Lista breve de los conceptos Java que se trabajan (NIO.2, Streams, records, etc.).

## Explicación paso a paso
Descripción detallada de cada bloque significativo del código,
con el fragmento de código en cuestión y su explicación.

## Ejemplo de uso / Salida esperada
Entrada o fichero de prueba y salida que produce el programa.

## Para saber más (opcional)
Referencias a la Javadoc oficial u otros recursos útiles.
```

---

## Tono y nivel de las explicaciones

- Escribe para un alumno de **2.º de FP DAM** con conocimientos básicos de Java (POO, colecciones).
- Explica **el porqué**, no sólo el qué ("Usamos `try-with-resources` para garantizar que el fichero se cierra aunque ocurra una excepción").
- Usa **analogías sencillas** cuando introduzcas conceptos nuevos (p. ej. "un `Stream` es como una cadena de montaje...").
- Nunca asumas conocimiento de la API NIO.2 ni de Streams; explícalos desde cero en el primer ejemplo que los usen.
- El español es el idioma de los apuntes; el código y los identificadores pueden ir en inglés.

