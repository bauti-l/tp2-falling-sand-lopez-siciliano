(ns falling-sand.simulacion-test
  (:require [clojure.test :refer [deftest is testing]]
            [falling-sand.grilla :as grilla]
            [falling-sand.simulacion :as simulacion]))

(def secuencias-6-2
  [{:nombre "Grilla de 8 x 5 con un grano de arena"
    :grillas [["...a...."
               "........"
               "........"
               "........"
               "########"]
              ["........"
               "...a...."
               "........"
               "........"
               "########"]
              ["........"
               "........"
               "...a...."
               "........"
               "########"]
              ["........"
               "........"
               "........"
               "...a...."
               "########"]
              ["........"
               "........"
               "........"
               "...a...."
               "########"]]}

   {:nombre "Grilla de 4 x 4 con una fila de agua arriba"
    :grillas [["gggg"
               "...."
               "...."
               "####"]
              ["...."
               "gggg"
               "...."
               "####"]
              ["...."
               "g..g"
               ".gg."
               "####"]
              ["...."
               "g..g"
               ".gg."
               "####"]
              ["...."
               "...."
               "gggg"
               "####"]
              ["...."
               "...."
               "gggg"
               "####"]]}

   {:nombre "Grilla de ancho impar (5 x 4)"
    :grillas [["....a"
               "....."
               "....."
               "#####"]
              ["....a"
               "....."
               "....."
               "#####"]
              ["....a"
               "....."
               "....."
               "#####"]
              ["....."
               "....a"
               "....."
               "#####"]
              ["....."
               "....a"
               "....."
               "#####"]
              ["....."
               "....a"
               "....."
               "#####"]]}

   {:nombre "Grilla minima (3 x 3)"
    :grillas [["a.."
               "..."
               "###"]
              ["..."
               "a.."
               "###"]]}

   {:nombre "Arena que se hunde en el agua (2 x 5)"
    :grillas [["a."
               ".."
               "gg"
               "gg"
               "##"]
              [".."
               "a."
               "gg"
               "gg"
               "##"]
              [".."
               "a."
               "gg"
               "gg"
               "##"]
              [".."
               "a."
               "gg"
               "gg"
               "##"]
              [".."
               "g."
               "ag"
               "gg"
               "##"]
              [".."
               "g."
               "gg"
               "ag"
               "##"]
              [".."
               "g."
               "gg"
               "ag"
               "##"]]}

   {:nombre "Vapor que sube (4 x 4)"
    :grillas [["...."
               "...."
               "...."
               "^..."]
              ["...."
               "...."
               ".^.."
               "...."]
              ["...."
               ".^.."
               "...."
               "...."]
              [".^.."
               "...."
               "...."
               "...."]
              [".^.."
               "...."
               "...."
               "...."]
              ["^..."
               "...."
               "...."
               "...."]
              ["^..."
               "...."
               "...."
               "...."]]}

   {:nombre "Agua en una columna aislada (4 x 5)"
    :grillas [["g..."
               "...."
               "...."
               "...."
               "####"]
              ["...."
               ".g.."
               "...."
               "...."
               "####"]
              ["...."
               "...."
               "..g."
               "...."
               "####"]
              ["...."
               "...."
               "...."
               "..g."
               "####"]
              ["...."
               "...."
               "...."
               "..g."
               "####"]
              ["...."
               "...."
               "...."
               "..g."
               "####"]
              ["...."
               "...."
               "...."
               "..g."
               "####"]
              ["...."
               "...."
               "...."
               ".g.."
               "####"]
              ["...."
               "...."
               "...."
               ".g.."
               "####"]]}])

(defn- mundos-desde
  "Secuencia infinita de mundos que empieza en el mundo inicial de la grilla escrita como
  `texto` y sigue con `avanzar`: el elemento n es el mundo despues de n pasos."
  [texto]
  (iterate simulacion/avanzar (simulacion/mundo-inicial (grilla/desde-texto texto))))

(deftest la-transcripcion-de-6-2-esta-completa
  (testing "grillas por secuencia: 8 x 5, 4 x 4 agua, 5 x 4, 3 x 3, 2 x 5, vapor, columna aislada"
    (is (= [5 6 6 2 7 7 9] (mapv (comp count :grillas) secuencias-6-2))))
  (testing "en todas las grillas de una secuencia las filas tienen el mismo largo"
    (is (= [] (vec (for [{:keys [nombre grillas]} secuencias-6-2
                         :let [anchos (set (map count (apply concat grillas)))
                               altos (set (map count grillas))]
                         :when (or (not= 1 (count anchos)) (not= 1 (count altos)))]
                     nombre))))))

(deftest avanzar-reproduce-las-7-secuencias-de-6-2
  (doseq [{:keys [nombre grillas]} secuencias-6-2
          :let [mundos (vec (take (count grillas) (mundos-desde (first grillas))))]
          [n esperada] (map-indexed vector grillas)
          :let [mundo (mundos n)]]
    (testing (str "6.2 " nombre ", n=" n ": la grilla")
      (is (= esperada (grilla/a-texto (:grilla mundo)))))
    (testing (str "6.2 " nombre ", n=" n ": el numero de paso es n")
      (is (= n (:paso mundo))))))

(deftest el-grano-de-8-x-5-queda-quieto-desde-n-4
  (let [mundos (vec (take 9 (mundos-desde (first (:grillas (first secuencias-6-2))))))
        n-4 (last (:grillas (first secuencias-6-2)))]
    (doseq [n (range 5 9)]
      (testing (str "6.2 Grilla de 8 x 5 con un grano de arena, n=" n " es igual a n=4")
        (is (= n-4 (grilla/a-texto (:grilla (mundos n)))))))))

(deftest grillas-iguales-con-distinto-paso-no-son-el-mismo-estado
  (let [[_ _ n-2 n-3 n-4] (:grillas (second secuencias-6-2))
        g (grilla/desde-texto n-2)
        en-paso-2 (assoc (simulacion/mundo-inicial g) :paso 2)
        en-paso-3 (assoc (simulacion/mundo-inicial g) :paso 3)]
    (testing "la misma grilla con paso 2 y con paso 3 no es el mismo mundo"
      (is (not= en-paso-2 en-paso-3)))
    (testing "desde el paso 2 la grilla queda igual: es la de n=3"
      (is (= n-3 (grilla/a-texto (:grilla (simulacion/avanzar en-paso-2))))))
    (testing "desde el paso 3 el agua baja: es la de n=4"
      (is (= n-4 (grilla/a-texto (:grilla (simulacion/avanzar en-paso-3))))))))

(def texto-chico
  ["a."
   ".."
   "##"])

(deftest mundo-inicial-empieza-en-el-paso-0
  (let [g (grilla/desde-texto texto-chico)]
    (testing "enunciado 4.1.3 y arquitectura 4: {:grilla grilla :paso 0}"
      (is (= {:grilla g :paso 0} (simulacion/mundo-inicial g))))))

(deftest avanzar-suma-uno-al-paso-y-no-modifica-el-mundo-recibido
  (let [mundo (simulacion/mundo-inicial (grilla/desde-texto texto-chico))
        copia (simulacion/mundo-inicial (grilla/desde-texto texto-chico))
        siguiente (simulacion/avanzar mundo)]
    (testing "enunciado 4.1.3: el paso pasa de 0 a 1"
      (is (= 1 (:paso siguiente))))
    (testing "y otro avance lo lleva a 2"
      (is (= 2 (:paso (simulacion/avanzar siguiente)))))
    (testing "el mundo recibido sigue igual a una copia armada aparte"
      (is (= copia mundo)))
    (testing "la grilla del mundo recibido sigue con la arena en la fila 0"
      (is (= texto-chico (grilla/a-texto (:grilla mundo)))))))

(deftest avanzar-conserva-las-demas-claves-del-mundo
  (let [mundo (assoc (simulacion/mundo-inicial (grilla/desde-texto texto-chico)) :otra :valor)]
    (testing "arquitectura 4: una clave :otra sigue igual despues de avanzar"
      (is (= :valor (:otra (simulacion/avanzar mundo)))))
    (testing "y despues de avanzar-con map"
      (is (= :valor (:otra (simulacion/avanzar-con map mundo)))))))

(def simbolos-del-selector ".#ag^lfmpv")

(defn- grilla-cargada
  "Grilla de 40 x 30 sin azar: la celda (x, y) tiene el material de indice (7x + 13y) mod 10 en
  `simbolos-del-selector`. Como 7x mod 10 recorre los 10 restos, aparecen los 10 materiales."
  []
  (grilla/desde-texto
   (vec (for [y (range 30)]
          (apply str (for [x (range 40)]
                       (nth simbolos-del-selector (mod (+ (* 7 x) (* 13 y)) 10))))))))

(deftest avanzar-con-map-con-pmap-y-avanzar-dan-el-mismo-mundo
  (let [inicial (simulacion/mundo-inicial (grilla-cargada))
        pasos 20
        secuencial (vec (take (inc pasos) (iterate #(simulacion/avanzar-con map %) inicial)))
        paralelo (vec (take (inc pasos) (iterate #(simulacion/avanzar-con pmap %) inicial)))
        por-defecto (vec (take (inc pasos) (iterate simulacion/avanzar inicial)))]
    (testing "la grilla de prueba tiene los 10 materiales"
      (is (= (set simbolos-del-selector)
             (set (apply str (grilla/a-texto (:grilla inicial)))))))
    (testing "despues del paso 0 el agua de (0, 1) se deslizo a (1, 1)"
      (is (= [:aire :agua]
             (let [g (:grilla (secuencial 1))]
               [(grilla/material g 0 1) (grilla/material g 1 1)]))))
    (doseq [n (range 1 (inc pasos))]
      (testing (str "despues de " n " pasos, avanzar-con pmap da el mismo mundo que con map")
        (is (= (secuencial n) (paralelo n))))
      (testing (str "despues de " n " pasos, avanzar da el mismo mundo que avanzar-con map")
        (is (= (secuencial n) (por-defecto n)))))))