# Ejemplo 04 — Guardar y recuperar objetos serializados

## Objetivo

El alumno aprenderá qué es la serialización de objetos en Java, cómo guardar
una lista de objetos en un fichero binario y cómo recuperarlos más tarde con
el estado exacto que tenían al guardarse. También verá cómo combinar la
serialización clásica de Java (`ObjectOutputStream`) con la API NIO.2.

---

## Conceptos clave

- **Serialización**: proceso de convertir un objeto en memoria en una secuencia
  de bytes que puede guardarse en disco o enviarse por red.
- **`Serializable`**: interfaz marcadora que habilita la serialización de una clase.
- **`serialVersionUID`**: identificador de versión que protege contra
  incompatibilidades al cambiar la clase.
- **`ObjectOutputStream` / `ObjectInputStream`**: flujos para escribir y leer
  objetos serializados.
- **`Files.newOutputStream()` / `Files.newInputStream()`**: puente entre NIO.2
  y los flujos clásicos de E/S.
- **`record` serializable**: los records de Java pueden implementar `Serializable`.

---

## Explicación paso a paso

### 1. El record `Contacto`

```java
record Contacto(String nombre, String email, String telefono) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
```

Un **record** es una clase de datos inmutable: Java genera automáticamente el
constructor, los getters (`nombre()`, `email()`, `telefono()`), `equals()`,
`hashCode()` y `toString()`. Perfecto para representar datos sin lógica.

Al añadir `implements Serializable` indicamos a la JVM que esta clase puede
convertirse en bytes. Sin esta interfaz, cualquier intento de serializar el
objeto lanzará `NotSerializableException`.

**`serialVersionUID`** es un número que identifica la "versión" de la clase.
Si más adelante añadimos o quitamos campos y alguien intenta leer un fichero
antiguo, la JVM comparará este número y lanzará una excepción si no coinciden,
protegiéndonos de leer datos corruptos. La anotación `@Serial` confirma que
esta constante tiene ese propósito específico.

> **Regla práctica**: declara siempre `serialVersionUID = 1L` cuando crees
> una clase serializable. Si cambias la clase de forma incompatible, increméntalo.

---

### 2. Guardar objetos: `ObjectOutputStream`

```java
try (var out = new ObjectOutputStream(Files.newOutputStream(ruta))) {
    out.writeObject(contactos);
}
```

Paso a paso:

1. **`Files.newOutputStream(ruta)`** abre (o crea) el fichero en la ruta indicada
   y devuelve un `OutputStream` — el puente entre NIO.2 y las clases clásicas de E/S.
2. **`new ObjectOutputStream(...)`** envuelve ese flujo y añade la capacidad de
   serializar objetos Java. Al crearlo, escribe automáticamente una cabecera
   binaria en el fichero que identifica el formato.
3. **`out.writeObject(contactos)`** serializa toda la lista: primero la propia
   lista y luego cada objeto `Contacto` dentro de ella.
4. El bloque **`try-with-resources`** cierra ambos flujos al salir, volcando
   cualquier dato que pudiera estar en búfer.

El fichero resultante (`.dat`) es binario: no está pensado para abrirlo con
un editor de texto, sino para leerlo de vuelta con Java.

---

### 3. Recuperar objetos: `ObjectInputStream`

```java
@SuppressWarnings("unchecked")
private static List<Contacto> cargarContactos(Path ruta)
    throws IOException, ClassNotFoundException {
  try (var in = new ObjectInputStream(Files.newInputStream(ruta))) {
    return (List<Contacto>) in.readObject();
  }
}
```

- **`Files.newInputStream(ruta)`** abre el fichero para lectura.
- **`in.readObject()`** lee el siguiente objeto del flujo y lo devuelve como
  `Object`. Como sabemos que guardamos una `List<Contacto>`, hacemos un cast.
  La anotación `@SuppressWarnings("unchecked")` silencia la advertencia del
  compilador sobre este cast no verificable en tiempo de ejecución.
- `readObject()` declara `throws ClassNotFoundException` porque si el fichero
  fue escrito con una clase que ya no existe en el classpath, la deserialización
  fallaría.

> **Precaución de seguridad**: nunca deserialices ficheros de orígenes no
> confiables. `readObject()` puede ejecutar código arbitrario si el fichero
> ha sido manipulado. Para datos de red o usuarios externos, usa formatos de
> texto como JSON o CSV en lugar de serialización binaria.

---

### 4. El puente NIO.2 ↔ E/S clásica

```
Path → Files.newOutputStream() → OutputStream → ObjectOutputStream
Path → Files.newInputStream()  → InputStream  → ObjectInputStream
```

`ObjectOutputStream` y `ObjectInputStream` pertenecen a la API clásica
(`java.io`) y necesitan un `OutputStream`/`InputStream`. Los métodos
`Files.newOutputStream()` y `Files.newInputStream()` de NIO.2 nos dan
exactamente eso a partir de un `Path`, conectando ambos mundos.

---

## Ejemplo de uso / Salida esperada

```
Guardados 3 contactos en 'contactos.dat'.
Fichero creado: C:\...\contactos.dat  (187 bytes)

Contactos recuperados (3):
  Ana García            ana@ejemplo.com            600 111 222
  Luis Martínez         luis@ejemplo.com           611 333 444
  Sara López            sara@ejemplo.com           622 555 666
```

---

## Para saber más

- [`Serializable` (Javadoc oficial)](https://docs.oracle.com/en/java/docs/api/java.base/java/io/Serializable.html)
- [`ObjectOutputStream` (Javadoc oficial)](https://docs.oracle.com/en/java/docs/api/java.base/java/io/ObjectOutputStream.html)
- [`ObjectInputStream` (Javadoc oficial)](https://docs.oracle.com/en/java/docs/api/java.base/java/io/ObjectInputStream.html)
- [`Files.newOutputStream()` (Javadoc oficial)](https://docs.oracle.com/en/java/docs/api/java.base/java/nio/file/Files.html#newOutputStream(java.nio.file.Path,java.nio.file.OpenOption...))
- [Java Object Serialization Specification](https://docs.oracle.com/en/java/docs/specs/serialization/index.html)
