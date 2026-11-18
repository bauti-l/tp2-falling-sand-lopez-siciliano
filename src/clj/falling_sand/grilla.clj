(ns falling-sand.grilla
  "Grilla del mundo (enunciado 4.1): un rectángulo de celdas con un material
  cada una, como valor inmutable con igualdad por valor. También la lee y la
  escribe como texto, con los símbolos de la tabla 5.2.

  La grilla es un mapa `{:ancho w :alto h :filas [...]}`. `:filas` es un vector
  con una fila por cada `y`, desde la fila 0, que es la de arriba (enunciado
  4.1.2); cada fila es un vector de `w` materiales (keywords), uno por cada
  columna `x`. Se guarda por filas porque un paso arma la grilla nueva por
  franjas de dos filas; las filas que quedan fuera de todo bloque se conservan
  tal cual. El resto del código usa la grilla solo a través de estas
  funciones.

  Es parte del núcleo puro: no tiene estado, no lee el reloj y no usa azar."
  (:require [falling-sand.materiales :as materiales]))

(def ^:private material-de-simbolo
  "Mapa de cada símbolo de la tabla 5.2 (un carácter) al material que
  representa. Se arma invirtiendo los `:simbolo` de `materiales/materiales`."
  (into {} (map (fn [[material {:keys [simbolo]}]] [simbolo material]))
        materiales/materiales))

(defn desde-filas
  "Grilla a partir de sus filas. Recibe una secuencia de filas, desde la de
  arriba, cada una una secuencia de materiales del mismo largo. Devuelve la
  grilla: el alto es la cantidad de filas y el ancho el largo de la primera.

  Precondición: al menos una fila y filas de al menos un material, es decir,
  ancho y alto de al menos 1. Una grilla sin celdas queda fuera del enunciado
  4.1.1, y la simulación no la admite."
  [filas]
  (let [filas (mapv vec filas)]
    {:ancho (count (first filas))
     :alto  (count filas)
     :filas filas}))

(defn crear
  "Grilla de `ancho` x `alto` celdas, todas de aire. Recibe las dimensiones en
  celdas y devuelve la grilla.

  Precondición: `ancho` y `alto` valen al menos 1, como en `desde-filas`."
  [ancho alto]
  (desde-filas (repeat alto (repeat ancho :aire))))

(defn- leer-simbolo
  "Material que representa `simbolo`, un carácter de la tabla 5.2. Lanza una
  excepción `ex-info` si el carácter no es el símbolo de ningún material."
  [simbolo]
  (or (material-de-simbolo simbolo)
      (throw (ex-info (str "Simbolo de material desconocido: " simbolo)
                      {:simbolo simbolo}))))

(defn desde-texto
  "Grilla a partir de su escritura como texto. Recibe un vector de strings, uno
  por fila desde la de arriba, con un símbolo de la tabla 5.2 por celda (por
  ejemplo `[\"a.\" \"##\"]`). Devuelve la grilla. Lanza una excepción `ex-info`
  si encuentra un carácter que no es el símbolo de ningún material."
  [filas]
  (desde-filas (map #(map leer-simbolo %) filas)))

(defn ancho
  "Ancho de la grilla: cantidad de columnas, en celdas."
  [grilla]
  (:ancho grilla))

(defn alto
  "Alto de la grilla: cantidad de filas, en celdas."
  [grilla]
  (:alto grilla))

(defn filas
  "Filas de la grilla. Devuelve un vector con una fila por cada `y`, desde la
  de arriba; cada fila es un vector de materiales indexado por la columna `x`."
  [grilla]
  (:filas grilla))

(defn material
  "Material de la celda de la columna `x` y la fila `y` (la fila 0 es la de
  arriba). Las coordenadas tienen que estar dentro de la grilla."
  [grilla x y]
  (get-in grilla [:filas y x]))

(defn con-material
  "Grilla igual a `grilla` salvo la celda de la columna `x` y la fila `y`, que
  pasa a contener `material`. Las coordenadas tienen que estar dentro de la
  grilla. No modifica la grilla recibida."
  [grilla x y material]
  (assoc-in grilla [:filas y x] material))

(defn a-texto
  "Escritura de la grilla como texto. Recibe la grilla y devuelve un vector de
  strings, uno por fila desde la de arriba, con el símbolo de la tabla 5.2 de
  cada celda."
  [grilla]
  (mapv (fn [fila] (apply str (map #(get-in materiales/materiales [% :simbolo]) fila)))
        (filas grilla)))

(defn contar-particulas
  "Cantidad de partículas de la grilla: las celdas que no son aire
  (enunciado 3, \"Indicadores\")."
  [grilla]
  (count (remove #{:aire} (apply concat (filas grilla)))))