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