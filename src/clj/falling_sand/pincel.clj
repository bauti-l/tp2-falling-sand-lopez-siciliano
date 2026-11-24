(ns falling-sand.pincel
  "Pincel (enunciado 3, \"Pintar\"): pinta con un material la unión de los
  discos de un radio dado centrados a lo largo de un segmento, para que al
  arrastrar el mouse rápido no queden huecos entre un evento y el siguiente.

  Es parte del núcleo puro: no tiene estado, no lee el reloj y no usa azar."
  (:require [falling-sand.grilla :as grilla]))

(defn- interpolar
  "Coordenada del punto `i` de los `n + 1` que van de `a` a `b` en `n` pasos
  iguales, redondeada al entero más cercano. Con `n` = 0 devuelve `a`."
  [a b i n]
  (if (zero? n)
    a
    (Math/round (double (+ a (/ (* i (- b a)) n))))))

(defn- puntos-del-segmento
  "Celdas del segmento que une `(x0, y0)` con `(x1, y1)`. Recibe las dos
  puntas. Devuelve los `n + 1` puntos `[x y]` que salen de interpolar entre
  ellas en `n = max(|x1 - x0|, |y1 - y0|)` pasos, del primero al último: así
  dos puntos seguidos son celdas vecinas y el trazo no tiene huecos. Con las
  dos puntas iguales devuelve solo esa celda."
  [x0 y0 x1 y1]
  (let [n (max (abs (- x1 x0)) (abs (- y1 y0)))]
    (for [i (range (inc n))]
      [(interpolar x0 x1 i n) (interpolar y0 y1 i n)])))

(defn- disco
  "Celdas del disco de radio `r` centrado en `[cx cy]` (enunciado 3): las
  `[x y]` con `(x - cx)² + (y - cy)² ≤ r²`. Con radio 0 es solo el centro.
  Devuelve una secuencia de celdas, que pueden caer fuera de la grilla."
  [r [cx cy]]
  (for [dy (range (- r) (inc r))
        dx (range (- r) (inc r))
        :when (<= (+ (* dx dx) (* dy dy)) (* r r))]
    [(+ cx dx) (+ cy dy)]))

(defn- dentro?
  "Verdadero si la celda `[x y]` está dentro de `grilla`."
  [grilla [x y]]
  (and (< -1 x (grilla/ancho grilla))
       (< -1 y (grilla/alto grilla))))

(defn pintar
  "Pinta un trazo (enunciado 3, \"Pintar\"). Recibe la grilla, el material
  (keyword), el radio del pincel en celdas (0 o más) y las dos puntas del
  segmento, `(x0, y0)` y `(x1, y1)`, en celdas. Devuelve una grilla nueva en
  la que cada celda de la unión de los discos de ese radio centrados en los
  puntos del segmento contiene `material`, sea cual sea el que tenía (pintar
  reemplaza). Las celdas de los discos que caen fuera de la grilla se
  ignoran."
  [grilla material radio x0 y0 x1 y1]
  (->> (puntos-del-segmento x0 y0 x1 y1)
       (mapcat #(disco radio %))
       distinct
       (filter #(dentro? grilla %))
       (reduce (fn [g [x y]] (grilla/con-material g x y material)) grilla)))