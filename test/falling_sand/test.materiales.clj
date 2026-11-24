(ns falling-sand.materiales-test
  (:require [clojure.test :refer [deftest is testing]]
            [falling-sand.materiales :as materiales]))

(def tabla-5-2
  [[:aire   \. 0xFF000000 :vacio    0 1]
   [:pared  \# 0xFF808080 :fijo     0 1]
   [:piedra \p 0xFF555560 :fijo     0 1]
   [:vidrio \v 0xFF9EDCE8 :fijo     0 1]
   [:madera \m 0xFF8B5A2B :fijo     0 1]
   [:arena  \a 0xFFE0C068 :polvo    4 1]
   [:agua   \g 0xFF2060E0 :liquido  2 1]
   [:lava   \l 0xFFE85010 :liquido  3 3]
   [:vapor  \^ 0xFFC8C8D8 :gas     -1 1]
   [:fuego  \f 0xFFFFC020 :gas     -2 1]])

(def materiales-que-decaen #{:vapor :fuego})

(deftest materiales-tiene-exactamente-los-diez-de-la-tabla-5-2
  (testing "las claves del mapa son los 10 materiales, ni uno mas ni uno menos"
    (is (= (set (map first tabla-5-2))
           (set (keys materiales/materiales))))))

(deftest cada-material-tiene-el-simbolo-el-color-y-el-tipo-de-la-tabla-5-2
  (doseq [[material simbolo color tipo _ _] tabla-5-2
          :let [props (get materiales/materiales material)]]
    (testing (str (name material) ": simbolo, un caracter")
      (is (= simbolo (:simbolo props))))
    (testing (str (name material) ": color ARGB, el numero de la tabla sin pasar a int")
      (is (= color (:color props))))
    (testing (str (name material) ": tipo")
      (is (= tipo (:tipo props))))))

(deftest densidad-y-periodo-valen-lo-de-la-tabla-5-2-con-sus-valores-por-defecto
  (doseq [[material _ _ _ densidad periodo] tabla-5-2
          :let [props (get materiales/materiales material)]]
    (testing (str (name material) ": densidad, 0 si no esta definida")
      (is (= densidad (get props :densidad 0))))
    (testing (str (name material) ": periodo de movimiento, 1 si no esta definido")
      (is (= periodo (get props :periodo 1))))))

(deftest solo-vapor-y-fuego-tienen-decaimiento
  (doseq [[material] tabla-5-2
          :let [decaimiento (get-in materiales/materiales [material :decaimiento])]]
    (if (materiales-que-decaen material)
      (testing (str (name material) ": tiene decaimiento definido")
        (is (some? decaimiento)))
      (testing (str (name material) ": no tiene decaimiento")
        (is (nil? decaimiento))))))

(deftest orden-selector-es-el-del-enunciado
  (testing "es un vector"
    (is (vector? materiales/orden-selector)))
  (testing "enunciado 3: aire, pared, arena, agua, vapor, lava, fuego, madera, piedra, vidrio"
    (is (= [:aire :pared :arena :agua :vapor :lava :fuego :madera :piedra :vidrio]
           materiales/orden-selector))))