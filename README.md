# Falling Sand

Implementación en Clojure + JavaFX de un simulador interactivo de materiales ("falling sand"), desarrollada para el TP2 de la materia Paradigmas de Programación. El núcleo de la simulación está escrito en Clojure, con datos inmutables y funciones puras, y calcula los bloques de la grilla en paralelo. La interfaz gráfica está hecha en Java con JavaFX.

## Tecnologías

- JDK 17 o posterior
- Clojure 1.12.6
- JavaFX 21.0.13
- Maven 3.9.16 (vía Maven Wrapper 3.3.4)
- Leiningen 2.x (opcional)

## Ejecución con Maven Wrapper

El proyecto incluye Maven Wrapper (`mvnw` / `mvnw.cmd`): descarga automáticamente Maven 3.9.16 la primera vez que se ejecuta, por lo que no hace falta tener Maven instalado. Solo se requiere JDK 17 o posterior.

Windows:

```bash
.\mvnw.cmd compile clojure:run
```

Linux / macOS:

```bash
./mvnw compile clojure:run
```

Si en Linux o macOS aparece `Permission denied`, darle permiso de ejecución al wrapper con `chmod +x mvnw`.

## Ejecución de los tests

Windows:

```bash
.\mvnw.cmd test
```

Linux / macOS:

```bash
./mvnw test
```

## Ejecución con Leiningen

Con Leiningen instalado:

```bash
lein run
lein test
```

Para medir los pasos por segundo de la simulación, en secuencial y en paralelo:

```bash
lein run -m falling-sand.medicion
```

## Ejecución con IntelliJ IDEA

Abrir el proyecto como proyecto de Maven (`pom.xml`) y, desde el panel de Maven, ejecutar `compile clojure:run`. Esto corre la función `-main` de `src/clj/falling_sand/core.clj`, que abre la aplicación.

## Video 
- [Video Franco Siciliano - TP2](https://youtu.be/6ZiLhJtzv4c)

## Integrantes

- Franco Siciliano - Padrón 108157
- Bautista López - Padrón 112484

## Corrector

- Macarena Vita Sanchez
