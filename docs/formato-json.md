# Apuntes — El formato JSON

## ¿Qué es JSON?

**JSON** (JavaScript Object Notation) es un formato de texto ligero para
representar datos estructurados. Aunque nació en el ecosistema JavaScript,
es completamente independiente del lenguaje: hoy es el estándar de facto
para intercambiar datos en APIs REST, ficheros de configuración, bases de
datos NoSQL y comunicaciones entre servicios.

---

## Por qué es importante

| Característica | Ventaja práctica |
|---|---|
| **Texto plano** | Legible por humanos y depurable sin herramientas especiales |
| **Universal** | Soportado nativamente en todos los lenguajes modernos |
| **Ligero** | Menos verboso que XML; menor tamaño en red |
| **Tipado básico** | Distingue cadenas, números, booleanos, null, objetos y arrays |
| **Interoperable** | El servidor puede ser Java, el cliente Python o JavaScript sin problemas |

---

## Tipos de datos en JSON

JSON solo reconoce **seis tipos**:

| Tipo | Ejemplo | Notas |
|---|---|---|
| `String` | `"Hola mundo"` | Siempre entre comillas dobles |
| `Number` | `42`, `3.14`, `-7` | Sin distinción entre entero y decimal |
| `Boolean` | `true`, `false` | En minúsculas, sin comillas |
| `null` | `null` | Ausencia de valor |
| `Object` | `{ "clave": valor }` | Colección de pares clave-valor |
| `Array` | `[1, 2, 3]` | Lista ordenada de valores |

---

## Sintaxis básica

### Objeto simple

```json
{
  "nombre": "Ana García",
  "edad": 28,
  "activo": true,
  "puntuacion": 9.5,
  "apodo": null
}
```

Reglas esenciales:
- Las **claves** son siempre `String` entre comillas dobles.
- Los pares clave-valor se separan con coma `,`.
- **No se permite coma** tras el último elemento.
- Los **objetos** se delimitan con `{ }`.

### Array

```json
["manzana", "naranja", "pera"]
```

```json
[1, 2, 3, 4, 5]
```

- Los **arrays** se delimitan con `[ ]`.
- Sus elementos pueden ser de tipos distintos (aunque no es recomendable).

### Objeto anidado

```json
{
  "nombre": "Ana García",
  "direccion": {
    "calle": "Calle Mayor, 12",
    "ciudad": "Madrid",
    "codigoPostal": "28001"
  },
  "telefonos": ["600 111 222", "911 333 444"]
}
```

Un objeto puede contener otros objetos y arrays como valores, formando
estructuras jerárquicas de cualquier profundidad.

### Array de objetos

```json
[
  { "id": 1, "nombre": "Ana" },
  { "id": 2, "nombre": "Luis" },
  { "id": 3, "nombre": "Marta" }
]
```

Este patrón es el más habitual en respuestas de APIs REST: una lista de
recursos del mismo tipo.

---

## Lo que JSON NO admite

- **Comentarios**: `// comentario` o `/* bloque */` no son válidos en JSON estricto.
- **Comas finales** (*trailing commas*): `{ "a": 1, }` es un error.
- **Claves sin comillas**: `{ nombre: "Ana" }` no es JSON, es JavaScript.
- **Valores de fecha nativos**: las fechas se representan como `String`
  (p. ej. `"2026-04-10"`) o como número Unix timestamp.
- **Funciones o expresiones**: JSON es solo datos, no código.

---

## JSON vs XML

```xml
<!-- XML -->
<contacto>
  <nombre>Ana García</nombre>
  <edad>28</edad>
</contacto>
```

```json
// JSON equivalente
{
  "nombre": "Ana García",
  "edad": 28
}
```

JSON es más compacto y directo. XML sigue siendo útil cuando se necesita
un esquema estricto (XSD), transformaciones XSLT o documentos con metadatos
embebidos (p. ej. SVG, SOAP).

---

## JSON en Java

Java no incluye un parser JSON en su biblioteca estándar. Las librería más habituales son:

| Librería | Uso principal |
|---|---|
| **Gson** (Google) | Proyectos pequeños y medianos, fácil de usar |
| **Jackson** | Estándar en Spring Boot, muy potente y configurable |
| **JSON-B** | Estándar Jakarta EE, basado en anotaciones |

Con **Gson** (usada en los ejemplos):

```java
// Objeto Java → JSON
String json = new Gson().toJson(miObjeto);

// JSON → Objeto Java
MiClase obj = new Gson().fromJson(json, MiClase.class);
```

Con **Jackson**:

```java
ObjectMapper mapper = new ObjectMapper();

// Objeto Java → JSON
String json = mapper.writeValueAsString(miObjeto);

// JSON → Objeto Java
MiClase obj = mapper.readValue(json, MiClase.class);
```

---

## Buenas prácticas

1. **Valida el JSON** antes de procesarlo (usa un validador online como
   [jsonlint.com](https://jsonlint.com) o el propio IDE).
2. **Usa nombres de clave en camelCase** para mantener coherencia con Java
   y JavaScript.
3. **No anides en exceso**: más de 3-4 niveles dificulta la lectura y el
   mantenimiento.
4. **Versiona tus estructuras**: si el formato cambia, añade un campo `"version"`
   para que los consumidores puedan adaptarse.
5. **Nunca confíes en JSON externo sin validar**: un campo que esperas como
   `Number` puede llegar como `null` o `String`.

---

## Resumen rápido

```
JSON
 ├── Tipos: String · Number · Boolean · null · Object · Array
 ├── Objeto: { "clave": valor, ... }
 ├── Array:  [ valor, valor, ... ]
 ├── Texto plano, UTF-8, sin comentarios
 └── Librería Java recomendada: Gson o Jackson
```
