(ns falling-sand.pincel-test
  (:require [clojure.test :refer [deftest is testing]]
            [falling-sand.grilla :as grilla]
            [falling-sand.pincel :as pincel]))

;; Hito H4: pincel (enunciado 3, "Pintar"). Una celda (x, y) pertenece al disco centrado en
;; (cx, cy) si (x - cx)^2 + (y - cy)^2 <= r^2. Los trazos son horizontales, verticales o diagonales
;; a 45 grados: en esos tres casos los puntos del segmento son exactamente sus celdas, asi que los
;; resultados no dependen de como se redondea.

(defn- celdas-con
  "Conjunto de las celdas [x y] de la grilla `g` que contienen `material`."
  [g material]
  (set (for [y (range (grilla/alto g))
             x (range (grilla/ancho g))
             :when (= material (grilla/material g x y))]
         [x y])))

(defn- pintado-sobre-aire
  "Pinta pared sobre una grilla de aire de `ancho` x `alto` y devuelve las celdas que quedaron
  con pared. Afirma ademas que ninguna otra celda dejo de ser aire."
  [ancho alto radio x0 y0 x1 y1]
  (let [g (pincel/pintar (grilla/crear ancho alto) :pared radio x0 y0 x1 y1)
        pared (celdas-con g :pared)]
    (is (= (count pared) (grilla/contar-particulas g))
        "fuera de las celdas con pared, todo sigue siendo aire")
    pared))

;; ---------------------------------------------------------------------------------------------
;; Disco
;; ---------------------------------------------------------------------------------------------

(deftest un-trazo-de-un-solo-punto-pinta-un-disco
  ;; Radio 1: dx^2 + dy^2 <= 1 solo con (0, 0) y los 4 vecinos ortogonales.
  (testing "radio 1 centrado en (5, 5): las 5 celdas en cruz"
    (is (= #{[5 4]
             [4 5] [5 5] [6 5]
             [5 6]}
           (pintado-sobre-aire 11 11 1 5 5 5 5))))
  ;; Radio 2: entran (0, 0); (+-1, 0) y (0, +-1), que suman 1; (+-1, +-1), que suman 2;
  ;; (+-2, 0) y (0, +-2), que suman 4. Quedan afuera (+-2, +-1) y (+-1, +-2), que suman 5.
  (testing "radio 2 centrado en (5, 5): las 13 celdas con distancia al cuadrado <= 4"
    (is (= #{[5 3]
             [4 4] [5 4] [6 4]
             [3 5] [4 5] [5 5] [6 5] [7 5]
             [4 6] [5 6] [6 6]
             [5 7]}
           (pintado-sobre-aire 11 11 2 5 5 5 5)))))

;; ---------------------------------------------------------------------------------------------
;; Segmento sin huecos
;; ---------------------------------------------------------------------------------------------

;; Union de las cruces de radio 1 centradas en (1, 3), (2, 3), ..., (8, 3): la fila 3 va de la
;; columna 0 a la 9 y las filas 2 y 4, de la 1 a la 8. Son 10 + 8 + 8 = 26 celdas.
(def trazo-horizontal
  (set (concat (for [x (range 1 9)] [x 2])
               (for [x (range 0 10)] [x 3])
               (for [x (range 1 9)] [x 4]))))

;; Union de las cruces de radio 1 centradas en (5, 1), (5, 2), ..., (5, 5): la columna 5 va de la
;; fila 0 a la 6 y las columnas 4 y 6, de la 1 a la 5. Son 7 + 5 + 5 = 17 celdas.
(def trazo-vertical
  (set (concat (for [y (range 1 6)] [4 y])
               (for [y (range 0 7)] [5 y])
               (for [y (range 1 6)] [6 y]))))

(deftest un-trazo-pinta-los-discos-de-todo-el-segmento
  (testing "horizontal de (1, 3) a (8, 3) con radio 1: 26 celdas, sin huecos"
    (is (= trazo-horizontal (pintado-sobre-aire 12 7 1 1 3 8 3))))
  (testing "el mismo trazo recorrido de (8, 3) a (1, 3) pinta lo mismo"
    (is (= trazo-horizontal (pintado-sobre-aire 12 7 1 8 3 1 3))))
  (testing "vertical de (5, 1) a (5, 5) con radio 1: 17 celdas, sin huecos"
    (is (= trazo-vertical (pintado-sobre-aire 12 7 1 5 1 5 5))))
  ;; Con radio 0 el disco es solo su centro: queda pintada cada celda del segmento.
  (testing "diagonal de (1, 1) a (5, 5) con radio 0: cada celda de la diagonal"
    (is (= #{[1 1] [2 2] [3 3] [4 4] [5 5]}
           (pintado-sobre-aire 12 7 0 1 1 5 5))))
  (testing "diagonal hacia arriba de (2, 5) a (6, 1) con radio 0: cada celda de la diagonal"
    (is (= #{[2 5] [3 4] [4 3] [5 2] [6 1]}
           (pintado-sobre-aire 12 7 0 2 5 6 1)))))

;; ---------------------------------------------------------------------------------------------
;; Celdas fuera de la grilla
;; ---------------------------------------------------------------------------------------------

(deftest las-celdas-fuera-de-la-grilla-se-ignoran
  ;; Del disco de radio 2 en (0, 0) entran las celdas con x >= 0 e y >= 0: (0,0), (1,0), (2,0),
  ;; (0,1), (1,1) y (0,2). (2,1) y (1,2) suman 5 y no son del disco.
  (testing "radio 2 centrado en la esquina (0, 0): solo el cuarto de disco de adentro"
    (is (= #{[0 0] [1 0] [2 0] [0 1] [1 1] [0 2]}
           (pintado-sobre-aire 12 7 2 0 0 0 0))))
  (testing "radio 1 centrado en la esquina (11, 6) de una grilla de 12 x 7"
    (is (= #{[11 5] [10 6] [11 6]}
           (pintado-sobre-aire 12 7 1 11 6 11 6))))
  ;; Centro en (-1, 3), radio 2. Columna 0: 1 + dy^2 <= 4 con dy = -1, 0, 1. Columna 1:
  ;; 4 + dy^2 <= 4 solo con dy = 0. Columna 2: 9 > 4.
  (testing "radio 2 centrado fuera de la grilla, en (-1, 3): la parte que cae adentro"
    (is (= #{[0 2] [0 3] [0 4] [1 3]}
           (pintado-sobre-aire 12 7 2 -1 3 -1 3))))
  (testing "trazo horizontal de (9, 3) a (14, 3) con radio 0 en 12 columnas: hasta la 11"
    (is (= #{[9 3] [10 3] [11 3]}
           (pintado-sobre-aire 12 7 0 9 3 14 3))))
  (testing "la grilla pintada conserva su tamano"
    (let [g (pincel/pintar (grilla/crear 12 7) :pared 2 0 0 0 0)]
      (is (= [12 7] [(grilla/ancho g) (grilla/alto g)])))))

;; ---------------------------------------------------------------------------------------------
;; Pintar reemplaza
;; ---------------------------------------------------------------------------------------------

(def todo-arena
  ["aaaaa"
   "aaaaa"
   "aaaaa"
   "aaaaa"
   "aaaaa"])

(deftest pintar-reemplaza-lo-que-hubiera
  (let [arena (grilla/desde-texto todo-arena)]
    (testing "pared con radio 1 en (2, 2) sobre arena: la cruz queda de pared"
      (is (= ["aaaaa"
              "aa#aa"
              "a###a"
              "aa#aa"
              "aaaaa"]
             (grilla/a-texto (pincel/pintar arena :pared 1 2 2 2 2)))))
    ;; Enunciado 3: "Seleccionar aire funciona como goma de borrar".
    (testing "aire con radio 1 en (2, 2) sobre arena borra la cruz"
      (is (= ["aaaaa"
              "aa.aa"
              "a...a"
              "aa.aa"
              "aaaaa"]
             (grilla/a-texto (pincel/pintar arena :aire 1 2 2 2 2)))))
    (testing "la grilla recibida no cambia"
      (is (= todo-arena (grilla/a-texto arena))))))
