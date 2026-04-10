# Ejemplo 05 — Leer un fichero JSON con Gson

## Objetivo

El alumno aprenderá a usar la librería **Gson** de Google para convertir un
fichero JSON en objetos Java y viceversa. Al finalizar comprenderá el concepto
de serialización/deserialización en formato de texto legible, mucho más
interoperable que la serialización binaria del ejemplo anterior.

---

## Conceptos clave

- **JSON** (JavaScript Object Notation): formato de texto estándar para
  intercambiar datos estructurados entre aplicaciones.
- **Gson**: librería de Google que convierte automáticamente entre JSON y objetos Java.
- **`Gson.fromJson()`**: convierte un `String` JSON en un objeto Java (deserialización).
- **`Gson.toJson()`**: convierte un objeto Java en un `String` JSON (serialización).
- **Mapeo por nombre**: Gson empareja las claves del JSON con los campos del objeto
  por nombre, sin necesidad de anotaciones.
- **Records como DTOs**: los records son ideales para representar la estructura de un JSON.

---

## Configuración Maven

Para usar Gson hay que añadir su dependencia en `pom.xml`:

```xml
<dependency>
  <groupId>com.google.code.gson</groupId>
  <artifactId>gson</artifactId>
  <version>2.11.0</version>
</dependency>
```

---

## Explicación paso a paso

### 1. El fichero JSON de entrada (`datos/contacto.json`)

```json
{
  "nombre": "Ana García",
  "email": "ana@ejemplo.com",
  "edad": 28,
  "activo": true,
  "direccion": {
    "calle": "Calle Mayor, 12",
    "ciudad": "Madrid",
    "codigoPostal": "28001"
  },
  "telefonos": ["600 111 222", "911 333 444"]
}
```

El JSON contiene tipos variados: `String`, `int`, `boolean`, un **objeto
anidado** (`direccion`) y un **array** (`telefonos`). Gson es capaz de
mapear todos estos tipos automáticamente.

---

### 2. Los records que representan la estructura

```java
record Direccion(String calle, String ciudad, String codigoPostal) {}

record Contacto(
    String nombre,
    String email,
    int edad,
    boolean activo,
    Direccion direccion,
    List<String> telefonos) {}
```

Los records actúan como **DTOs** (Data Transfer Objects): clases cuyo único
propósito es transportar datos. Gson los soporta directamente.

**La regla clave**: el nombre del campo en el record debe coincidir exactamente
con la clave en el JSON. Si el JSON dice `"codigoPostal"`, el campo se llama
`codigoPostal`. Si los nombres no coinciden, el campo quedará a `null`.

> No necesitamos implementar `Serializable` ni añadir anotaciones para que
> Gson funcione. Esa es una ventaja importante frente a la serialización binaria.

---

### 3. Crear la instancia de Gson

```java
Gson gson = new GsonBuilder().setPrettyPrinting().create();
```

- **`GsonBuilder`** permite configurar Gson antes de crearlo.
- **`setPrettyPrinting()`** hace que `toJson()` genere JSON con indentación
  legible en lugar de una sola línea compacta. Útil para mostrar por pantalla
  o para ficheros que leerán personas; prescindible en ficheros de producción.
- **`create()`** construye el objeto `Gson` con la configuración elegida.

---

### 4. Deserializar: JSON → objeto Java

```java
String json = Files.readString(ruta, StandardCharsets.UTF_8);
return gson.fromJson(json, Contacto.class);
```

Dos pasos:

1. **`Files.readString()`** lee todo el fichero como un `String`. Para ficheros
   JSON esto es adecuado porque el fichero entero debe procesarse de una vez.
2. **`gson.fromJson(json, Contacto.class)`** analiza el JSON y crea un objeto
   `Contacto` rellenando cada campo. Gson también crea el objeto `Direccion`
   anidado y la `List<String>` de teléfonos de forma automática.

---

### 5. Serializar: objeto Java → JSON

```java
System.out.println(gson.toJson(contacto));
```

`toJson()` hace el camino inverso: convierte el objeto en una cadena JSON.
Dado que hemos activado `setPrettyPrinting()`, el resultado está indentado
y es fácil de leer.

---

## Ejemplo de uso / Salida esperada

```
=== Datos cargados del JSON ===
  Nombre   : Ana García
  Email    : ana@ejemplo.com
  Edad     : 28
  Activo   : true
  Calle    : Calle Mayor, 12
  Ciudad   : Madrid
  C.Postal : 28001
  Teléfonos: [600 111 222, 911 333 444]

=== Objeto serializado de vuelta a JSON ===
{
  "nombre": "Ana García",
  "email": "ana@ejemplo.com",
  "edad": 28,
  "activo": true,
  "direccion": {
    "calle": "Calle Mayor, 12",
    "ciudad": "Madrid",
    "codigoPostal": "28001"
  },
  "telefonos": [
    "600 111 222",
    "911 333 444"
  ]
}
```

---

## JSON vs Serialización binaria

| | Serialización binaria (`ObjectOutputStream`) | JSON (Gson) |
|---|---|---|
| Formato | Binario (ilegible) | Texto legible |
| Interoperabilidad | Solo Java | Cualquier lenguaje |
| Requiere `Serializable` | Sí | No |
| Velocidad | Más rápida | Ligeramente más lenta |
| Uso típico | Caché interno, paso de objetos | APIs, configuración, intercambio de datos |

---

## Para saber más

- [Gson — Repositorio oficial](https://github.com/google/gson)
- [Guía de usuario de Gson](https://github.com/google/gson/blob/main/UserGuide.md)
- [Introducción a JSON](https://www.json.org/json-es.html)
