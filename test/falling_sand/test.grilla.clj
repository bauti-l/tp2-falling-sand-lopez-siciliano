(ns falling-sand.grilla-test
  (:require [clojure.test :refer [deftest is testing]]
            [falling-sand.grilla :as grilla]))

;; Hito H2: grilla inmutable y su escritura como texto (enunciado 4.1 y 5.2; arquitectura 3 y 4).
;; La grilla se usa solo por las funciones del contrato y por `=`: su representacion interna es
;; de quien implementa.

;; Tabla 5.2: simbolo de cada material.
(def simbolos-5-2
  {\. :aire, \# :pared, \p :piedra, \v :vidrio, \m :madera,
   \a :arena, \g :agua, \l :lava, \^ :vapor, \f :fuego})

;; Los 10 simbolos de la tabla 5.2, en una grilla de 5 columnas y 3 filas.
(def texto-con-los-10-simbolos
  [".#pvm"
   "agl^f"
   "#...a"])

(deftest crear-da-una-grilla-de-aire-con-las-dimensiones-pedidas
  (let [g (grilla/crear 5 3)]
    (testing "ancho: 5 columnas"
      (is (= 5 (grilla/ancho g))))
    (testing "alto: 3 filas"
      (is (= 3 (grilla/alto g))))
    (testing "todas las celdas son aire"
      (is (every? #{:aire} (for [y (range 3) x (range 5)] (grilla/material g x y)))))
    (testing "como texto: 3 filas de 5 aires"
      (is (= ["....." "....." "....."] (grilla/a-texto g))))))

(deftest a-texto-de-desde-texto-devuelve-el-mismo-texto
  (let [g (grilla/desde-texto texto-con-los-10-simbolos)]
    (testing "los 10 simbolos de la tabla 5.2 vuelven iguales"
      (is (= texto-con-los-10-simbolos (grilla/a-texto g))))
    (testing "el ancho es el largo de cada fila y el alto la cantidad de filas"
      (is (= [5 3] [(grilla/ancho g) (grilla/alto g)])))))

(deftest material-lee-la-columna-x-y-la-fila-y
  (let [g (grilla/desde-texto texto-con-los-10-simbolos)]
    (doseq [y (range 3)
            x (range 5)
            :let [simbolo (get-in texto-con-los-10-simbolos [y x])]]
      (testing (str "celda (" x ", " y "): simbolo " simbolo " de la tabla 5.2")
        (is (= (simbolos-5-2 simbolo) (grilla/material g x y))))))
  ;; Enunciado 4.1.2: la fila 0 es la de arriba. La grilla no es cuadrada, para que una x y una
  ;; y intercambiadas no lean la misma celda.
  (let [g (grilla/desde-texto ["..." "..a"])]
    (testing "la arena de la columna 2 de la segunda fila esta en (2, 1)"
      (is (= :arena (grilla/material g 2 1))))
    (testing "la primera fila del texto es la fila 0: (2, 0) es aire"
      (is (= :aire (grilla/material g 2 0))))))

(deftest dos-grillas-con-el-mismo-contenido-son-iguales
  (testing "dos grillas cargadas del mismo texto"
    (is (= (grilla/desde-texto ["a." "#g"]) (grilla/desde-texto ["a." "#g"]))))
  (testing "una grilla creada y una cargada de un texto todo de aire, del mismo tamano"
    (is (= (grilla/crear 3 2) (grilla/desde-texto ["..." "..."]))))
  (testing "con una celda distinta no son iguales"
    (is (not= (grilla/desde-texto ["a." ".."]) (grilla/desde-texto [".a" ".."]))))
  (testing "con las mismas 6 celdas en otra forma (2 x 3 contra 3 x 2) no son iguales"
    (is (not= (grilla/crear 2 3) (grilla/crear 3 2)))))

(deftest contar-particulas-cuenta-las-celdas-que-no-son-aire
  (testing "una grilla creada no tiene particulas"
    (is (= 0 (grilla/contar-particulas (grilla/crear 5 3)))))
  (testing "un grano sobre una fila de 3 paredes: 4"
    (is (= 4 (grilla/contar-particulas (grilla/desde-texto ["a.." "..." "###"])))))
  (testing "los 10 simbolos: 4 + 5 + 2 celdas que no son aire"
    (is (= 11 (grilla/contar-particulas (grilla/desde-texto texto-con-los-10-simbolos))))))
