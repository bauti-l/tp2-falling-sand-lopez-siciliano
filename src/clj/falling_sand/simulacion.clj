(ns falling-sand.simulacion
  "Un paso completo de la simulación (enunciado 4.1 y 4.2): la partición de la
  grilla en bloques de 2 x 2 desplazada según el número de paso, la regla del
  bloque aplicada a cada bloque completo y el armado de la grilla nueva.

  Todos los bloques leen la grilla del paso anterior, así que son
  independientes. Se calculan por franjas: una franja es una fila de bloques,
  que ocupa dos filas de la grilla y devuelve esas dos filas nuevas. Las
  franjas se recorren con una función `mapear` que se recibe por parámetro
  (`map` para el recorrido secuencial, `pmap` para el paralelo, enunciado 7.2,
  punto 3); ese es el único punto de concurrencia del núcleo.

  Es parte del núcleo puro: no tiene estado, no lee el reloj y no usa azar."
  (:require [falling-sand.bloque :as bloque]
            [falling-sand.grilla :as grilla]))

(def ^:private desplazamientos
  "Tabla de desplazamientos de la partición (enunciado 4.2): el elemento de
  índice `p = paso mod 4` es el desplazamiento `[ox oy]`, en columnas y filas."
  [[0 0] [1 1] [1 0] [0 1]])

(defn mundo-inicial
  "Mundo al empezar la simulación (enunciado 4.1.3). Recibe una grilla y
  devuelve el mapa `{:grilla grilla :paso 0}`."
  [grilla]
  {:grilla grilla :paso 0})

(defn- particion
  "Partición en bloques de un paso (enunciado 4.2, \"Desplazamiento\" y \"Qué
  bloques se simulan\"). Recibe la grilla y el número de paso. Devuelve un
  mapa con:
    :ox :oy    desplazamiento de la tabla `desplazamientos`.
    :columnas  cantidad de bloques por franja: los `bx` válidos van de 0 a
               `columnas - 1`.
    :franjas   cantidad de franjas (filas de bloques): los `by` válidos van
               de 0 a `franjas - 1`.
  Solo cuentan los bloques que entran completos en la grilla."
  [grilla paso]
  (let [[ox oy] (desplazamientos (mod paso 4))]
    {:ox       ox
     :oy       oy
     :columnas (quot (- (grilla/ancho grilla) ox) 2)
     :franjas  (quot (- (grilla/alto grilla) oy) 2)}))

(defn- reemplazar-tramo
  "Vector igual a `v` salvo a partir del índice `desde`, donde van, en orden,
  los elementos de `nuevos`. Los anteriores a `desde` y los que siguen al
  último reemplazado quedan igual: así se conservan las celdas que no
  pertenecen a ningún bloque (enunciado 4.2, \"Celdas fuera de los bloques\").
  Devuelve un vector nuevo."
  [v desde nuevos]
  (let [nuevos (vec nuevos)]
    (into [] cat [(subvec v 0 desde) nuevos (subvec v (+ desde (count nuevos)))])))

(defn- leer-bloque
  "Contenido del bloque cuya esquina superior izquierda está en la columna `x`
  de la fila `arriba`, con la fila `abajo` debajo. Devuelve el vector de sus 4
  materiales en el orden de posiciones del enunciado 4.2: 0 y 1 de `arriba`,
  2 y 3 de `abajo`."
  [arriba abajo x]
  [(arriba x) (arriba (inc x)) (abajo x) (abajo (inc x))])

(defn- avanzar-franja
  "Calcula la franja `by`: la fila de bloques de índices `(0, by)` a
  `(columnas - 1, by)`. Recibe las filas de la grilla anterior, el número de
  paso, la partición y `by`. A cada bloque le aplica la regla del bloque
  (enunciado 4.4) con su fase (enunciado 4.3), leyendo solo la grilla anterior
  (enunciado 4.2, \"Lectura de la grilla anterior\"). Devuelve las dos filas
  nuevas que ocupa la franja, `[arriba abajo]`: las posiciones 0 y 1 de cada
  bloque van a la de arriba y las 2 y 3 a la de abajo."
  [filas paso {:keys [ox oy columnas]} by]
  (let [y      (+ oy (* 2 by))
        arriba (filas y)
        abajo  (filas (inc y))
        nuevos (mapv (fn [bx]
                       (bloque/aplicar-regla (leer-bloque arriba abajo (+ ox (* 2 bx)))
                                             (bloque/fase paso bx by)))
                     (range columnas))]
    [(reemplazar-tramo arriba ox (mapcat #(subvec % 0 2) nuevos))
     (reemplazar-tramo abajo ox (mapcat #(subvec % 2 4) nuevos))]))

(defn avanzar-con
  "Avanza un paso (enunciado 4.1.3 y 4.2). Recibe la función `mapear`, que se
  llama como `map`, `(mapear f franjas)`, y con la que se recorren las franjas
  de bloques, y el mundo. Devuelve el mundo siguiente: la grilla nueva y
  `:paso` aumentado en 1, con las demás claves del mundo sin cambios.

  La grilla nueva conserva las filas que no pertenecen a ninguna franja y
  reemplaza las demás por las que calcula cada franja. Como todas las franjas
  leen la grilla anterior, el resultado no depende de `mapear`."
  [mapear {:keys [grilla paso] :as mundo}]
  (let [{:keys [oy franjas] :as part} (particion grilla paso)
        anteriores (grilla/filas grilla)
        nuevas     (mapear #(avanzar-franja anteriores paso part %) (range franjas))]
    (assoc mundo
           :grilla (grilla/desde-filas (reemplazar-tramo anteriores oy (apply concat nuevas)))
           :paso   (inc paso))))

(defn avanzar
  "Avanza un paso calculando las franjas de bloques en paralelo, con `pmap`
  (enunciado 7.2, punto 3). Recibe el mundo y devuelve el siguiente; es
  `(avanzar-con pmap mundo)`."
  [mundo]
  (avanzar-con pmap mundo))
