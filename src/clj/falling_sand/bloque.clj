(ns falling-sand.bloque
  "Regla del bloque de 2 x 2 (enunciado 4.3 y 4.4): la fase de un bloque, la
  decisión \"una de cada N veces\" y las tres etapas que calculan el contenido
  nuevo de un bloque: decaimiento, reacciones y movimiento.

  Un bloque es un vector de 4 materiales en el orden de posiciones del
  enunciado 4.2:

      0  1
      2  3

  Cada etapa es una función pura `(etapa bloque fase)` que devuelve un bloque
  nuevo, y `aplicar-regla` es su composición. Qué material decae y cuáles
  reaccionan sale de las tablas de `falling-sand.materiales`. El movimiento
  decide solo por tipo, densidad y período: no nombra ningún material
  (enunciado 4.1.5).

  Es parte del núcleo puro: no tiene estado, no lee el reloj y no usa azar."
  (:require [falling-sand.materiales :as materiales]))

(defn fase
  "Fase de un bloque en un paso (enunciado 4.3): ⌊paso / 4⌋ + 3·bx + 5·by.
  Recibe el número de paso y el índice `(bx, by)` del bloque dentro de la
  partición de ese paso. Devuelve un entero."
  [paso bx by]
  (+ (quot paso 4) (* 3 bx) (* 5 by)))

(defn cumple?
  "Decisión \"una de cada N veces\" (enunciado 4.3). Recibe la fase del bloque,
  el número de decisión `k` y el período `n`. Devuelve true si `fase + 7·k` es
  múltiplo de `n` y false si no. Con `n` = 1 devuelve siempre true.

  Es la única función de la simulación que toma esta decisión."
  [fase k n]
  (zero? (mod (+ fase (* 7 k)) n)))

(def ^:private k-de
  "Números de decisión `k` de la regla del bloque, copiados de la tabla
  \"Resumen de números de decisión k\" del enunciado 4.4.

  En :decaimiento y :permiso el valor es el `k` de la posición 0, y en
  :reaccion el del par 0: al usarlo se le suma el número de posición `i` o el
  número de par `q`. Los otros tres son el `k` completo."
  {:decaimiento   0
   :reaccion      4
   :permiso       10
   :diagonales    14
   :fila-superior 15
   :fila-inferior 16})

(defn- decaer-posicion
  "Decaimiento de una posición (enunciado 4.4, etapa 1). Recibe la fase del
  bloque, el número de posición `i` y el material que contiene. Devuelve el
  destino del decaimiento si el material decae y se cumple la decisión de su
  período con `k = i`; si no, el mismo material."
  [fase i material]
  (let [{:keys [destino periodo]} (materiales/decaimiento material)]
    (if (and destino (cumple? fase (+ (k-de :decaimiento) i) periodo))
      destino
      material)))

(defn- decaer
  "Etapa 1 de la regla del bloque (enunciado 4.4): decaimiento de las posiciones
  0, 1, 2 y 3, en ese orden. Recibe un bloque y su fase y devuelve el bloque
  nuevo, siempre como vector."
  [bloque fase]
  (into [] (map-indexed (fn [i material] (decaer-posicion fase i material))) bloque))

(def ^:private pares-de-reaccion
  "Pares de posiciones entre los que se busca una reacción, en el orden en que
  se recorren (tabla del enunciado 4.4, etapa 2). El índice de cada par dentro
  del vector es su número de par `q`."
  [[0 1] [2 3] [0 2] [1 3] [0 3] [1 2]])

(defn- reaccionar-par
  "Reacción de un par de posiciones (enunciado 4.4, etapa 2). Recibe la fase,
  el bloque tal como lo dejaron los pares anteriores, el número de par `q` y el
  par `[i j]`. Si la tabla de reacciones tiene una reacción entre los
  materiales actuales de `i` y `j`, en cualquier orden, y se cumple la decisión
  de su período con `k = 4 + q`, devuelve el bloque con cada una de las dos
  posiciones cambiada por lo que la reacción produce a partir del material que
  contenía. Si no, devuelve el mismo bloque."
  [fase bloque q [i j]]
  (let [a (bloque i)
        b (bloque j)
        {:keys [periodo resultado]} (materiales/reaccion a b)]
    (if (and resultado (cumple? fase (+ (k-de :reaccion) q) periodo))
      (assoc bloque i (resultado a) j (resultado b))
      bloque)))

(defn- reaccionar
  "Etapa 2 de la regla del bloque (enunciado 4.4): recorre `pares-de-reaccion`
  en orden, y cada par ve los materiales que dejaron los anteriores. Recibe un
  bloque (vector) y su fase y devuelve el bloque nuevo."
  [bloque fase]
  (reduce-kv (fn [actual q par] (reaccionar-par fase actual q par))
             bloque
             pares-de-reaccion))

(defn- puede-moverse?
  "Permiso de movimiento de una posición (enunciado 4.4, etapa 3a). Recibe la
  fase, el número de posición `i` y el material que contiene. Devuelve true si
  el material no es de tipo fijo y se cumple la decisión de su período de
  movimiento con `k = 10 + i`; si no, false."
  [fase i material]
  (and (not= :fijo (materiales/tipo material))
       (cumple? fase (+ (k-de :permiso) i) (materiales/periodo material))))

(defn- permisos-de-movimiento
  "Permisos de movimiento de las cuatro posiciones (enunciado 4.4, etapa 3a).
  Recibe el bloque tal como lo dejó la etapa 2 y su fase. Devuelve un vector de
  4 booleanos, uno por posición. Se calcula una sola vez, antes de mover nada:
  el permiso pertenece a la posición y no al material que después llegue a
  ella."
  [bloque fase]
  (into [] (map-indexed (fn [i material] (puede-moverse? fase i material))) bloque))

(defn- intercambiar-si
  "El único intercambio de la etapa 3 (enunciado 4.4, etapas 3b y 3c). Recibe
  el bloque, el vector de permisos, un par de posiciones `[i j]` y un predicado
  `condicion?` de dos materiales. Devuelve el bloque con los contenidos de `i`
  y `j` intercambiados si las dos posiciones tienen permiso y
  `(condicion? material-de-i material-de-j)` es verdadero; si no, el mismo
  bloque."
  [bloque permisos [i j] condicion?]
  (let [a (bloque i)
        b (bloque j)]
    (if (and (permisos i) (permisos j) (condicion? a b))
      (assoc bloque i b j a)
      bloque)))

(def ^:private pares-verticales
  "Pares `[arriba abajo]` de las dos columnas del bloque, en el orden en que se
  aplica el intercambio por densidad (enunciado 4.4, etapa 3b, puntos 1 y 2)."
  [[0 2] [1 3]])

(def ^:private diagonales-si-cumple
  "Pares `[arriba abajo]` de las dos diagonales, en el orden en que se aplican
  cuando se cumple la decisión de período 2 con `k = 14` (enunciado 4.4,
  etapa 3b, punto 3)."
  [[0 3] [1 2]])

(def ^:private diagonales-si-no-cumple
  "Pares `[arriba abajo]` de las dos diagonales, en el orden en que se aplican
  cuando no se cumple la decisión de período 2 con `k = 14` (enunciado 4.4,
  etapa 3b, punto 3)."
  [[1 2] [0 3]])

(defn- pares-por-densidad
  "Pares `[arriba abajo]` del intercambio por densidad, en el orden en que se
  aplican para una fase (enunciado 4.4, etapa 3b): las dos columnas y después
  las dos diagonales, cuyo orden depende de la decisión de período 2 con
  `k = 14`. Recibe la fase y devuelve un vector de 4 pares."
  [fase]
  (into pares-verticales
        (if (cumple? fase (k-de :diagonales) 2)
          diagonales-si-cumple
          diagonales-si-no-cumple)))

(defn- mas-denso?
  "Condición del intercambio por densidad (enunciado 4.4, etapa 3b). Recibe el
  material de la posición de arriba y el de la de abajo. Devuelve true si la
  densidad del de arriba es estrictamente mayor que la del de abajo."
  [arriba abajo]
  (> (materiales/densidad arriba) (materiales/densidad abajo)))

(defn- intercambiar-por-densidad
  "Intercambio por densidad (enunciado 4.4, etapa 3b). Recibe el bloque, el
  vector de permisos y la fase. Recorre en orden los pares de
  `pares-por-densidad` e intercambia los que tienen permiso en las dos
  posiciones y arriba un material más denso que abajo. Devuelve el bloque
  nuevo."
  [bloque permisos fase]
  (reduce (fn [actual par] (intercambiar-si actual permisos par mas-denso?))
          bloque
          (pares-por-densidad fase)))

(def ^:private filas
  "Filas del bloque en el orden en que se aplica el deslizamiento horizontal
  (enunciado 4.4, etapa 3c): primero la superior y después la inferior. Cada
  una es un mapa con:
    :par           las dos posiciones de la fila.
    :k             número de decisión de la fila.
    :se-desplazan  tipos de material que se deslizan en esa fila: los gases en
                   las dos, los líquidos solo en la inferior."
  [{:par [0 1] :k (k-de :fila-superior) :se-desplazan #{:gas}}
   {:par [2 3] :k (k-de :fila-inferior) :se-desplazan #{:gas :liquido}}])

(defn- se-desliza-hacia?
  "Condición del deslizamiento para un sentido (enunciado 4.4, etapa 3c).
  Recibe el conjunto de tipos que se desplazan en la fila y dos materiales.
  Devuelve true si `origen` es de uno de esos tipos y `destino` es de tipo
  vacío."
  [se-desplazan origen destino]
  (and (contains? se-desplazan (materiales/tipo origen))
       (= :vacio (materiales/tipo destino))))

(defn- deslizar-fila
  "Deslizamiento de una fila (enunciado 4.4, etapa 3c). Recibe la fase, el
  vector de permisos, el bloque y una fila de `filas`. Devuelve el bloque con
  las dos posiciones de la fila intercambiadas si se cumple la decisión de
  período 2 con el `k` de la fila, las dos tienen permiso, y una contiene un
  material que se desplaza en esa fila y la otra uno de tipo vacío. Si no,
  devuelve el mismo bloque."
  [fase permisos bloque {:keys [par k se-desplazan]}]
  (if (cumple? fase k 2)
    (intercambiar-si bloque permisos par
                     (fn [a b]
                       (or (se-desliza-hacia? se-desplazan a b)
                           (se-desliza-hacia? se-desplazan b a))))
    bloque))

(defn- deslizar
  "Deslizamiento horizontal (enunciado 4.4, etapa 3c). Recibe el bloque, el
  vector de permisos y la fase. Aplica `deslizar-fila` a cada fila de `filas`,
  en orden, y devuelve el bloque nuevo."
  [bloque permisos fase]
  (reduce (fn [actual fila] (deslizar-fila fase permisos actual fila))
          bloque
          filas))

(defn- mover
  "Etapa 3 de la regla del bloque (enunciado 4.4): calcula los permisos de
  movimiento sobre el bloque recibido y, con esos mismos permisos, aplica el
  intercambio por densidad y después el deslizamiento horizontal. Recibe un
  bloque (vector) y su fase y devuelve el bloque nuevo."
  [bloque fase]
  (let [permisos (permisos-de-movimiento bloque fase)]
    (-> bloque
        (intercambiar-por-densidad permisos fase)
        (deslizar permisos fase))))

(defn aplicar-regla
  "Regla del bloque completa (enunciado 4.4): decaimiento, reacciones y
  movimiento, en ese orden, cada etapa sobre el resultado de la anterior.
  Recibe un bloque (los 4 materiales en el orden de posiciones 0, 1, 2, 3) y su
  fase. Devuelve el bloque nuevo, como vector de 4 materiales."
  [bloque fase]
  (-> bloque
      (decaer fase)
      (reaccionar fase)
      (mover fase)))