(ns falling-sand.bloque-test
  (:require [clojure.test :refer [deftest is testing]]
            [falling-sand.bloque :as bloque]))

(def ejemplo-4-3
  [[0 3 0 true]
   [4 7 1 false]
   [8 11 2 false]
   [12 15 3 true]
   [16 19 4 false]
   [20 23 5 false]
   [24 27 6 true]])

(def fases-derivadas
  [[0 0 0 0 "0 + 0 + 0"]
   [3 0 0 0 "3 / 4 = 0: los pasos 0 a 3 comparten el cociente"]
   [4 0 0 1 "4 / 4 = 1"]
   [7 0 0 1 "7 / 4 = 1"]
   [8 0 0 2 "8 / 4 = 2"]
   [0 1 0 3 "3*1: bx pesa 3"]
   [0 0 1 5 "5*1: by pesa 5"]
   [0 2 3 21 "3*2 + 5*3 = 6 + 15"]
   [1234 79 59 840 "1234 / 4 = 308; 308 + 3*79 + 5*59 = 308 + 237 + 295"]])

(def decisiones-del-enunciado
  [[3 1 10 true "4.4 etapa 1: fuego en la posicion 1, 3 + 7 = 10 es multiplo de 10"]
   [0 1 10 false "4.4 etapa 1: 7 no es multiplo de 10"]
   [93 1 100 true "4.4 etapa 1: vapor en la posicion 1, 93 + 7 = 100"]
   [92 1 100 false "4.4 etapa 1: con fase 92 no"]
   [2 4 2 true "4.4 etapa 2: fuego-agua, k = 4, se cumple si fase + 28 es par"]
   [3 4 2 false "4.4 etapa 2: con fase 3 no se cumple"]
   [12 4 20 true "4.4 etapa 2: fuego-madera, 12 + 28 = 40"]
   [11 4 20 false "4.4 etapa 2: con fase 11 no"]
   [0 12 3 true "4.4 etapa 3: lava en la posicion 2, 0 + 84 = 84 es multiplo de 3"]
   [1 12 3 false "4.4 etapa 3: 1 + 84 = 85 no es multiplo de 3"]
   [0 16 2 true "4.4 etapa 3: fila inferior, 0 + 112 es par"]
   [1 16 2 false "4.4 etapa 3: 113 es impar"]
   [1 15 2 true "4.4 etapa 3: fila superior, 1 + 105 = 106 es par"]
   [0 14 2 true "4.4 ejemplo completo: diagonales, 0 + 98 es par"]])

(deftest fase-del-bloque-del-ejemplo-de-4-3
  (doseq [[desde hasta cociente _] ejemplo-4-3
          paso (range desde (inc hasta))]
    (testing (str "bloque (1, 2), paso " paso ": fase = " cociente " + 13")
      (is (= (+ cociente 13) (bloque/fase paso 1 2))))))

(deftest fase-suma-el-cociente-del-paso-y-el-indice-del-bloque
  (doseq [[paso bx by esperada cuenta] fases-derivadas]
    (testing (str "paso " paso ", bloque (" bx ", " by "): " cuenta)
      (is (= esperada (bloque/fase paso bx by))))))

(deftest cumple-en-el-ejemplo-de-4-3
  (doseq [[desde hasta cociente esperado] ejemplo-4-3]
    (testing (str "k = 5, N = 3, fase " cociente " + 13 (pasos " desde " a " hasta ")")
      (is (= esperado (boolean (bloque/cumple? (+ cociente 13) 5 3)))))
    (doseq [paso (range desde (inc hasta))]
      (testing (str "k = 5, N = 3, bloque (1, 2), paso " paso ", con la fase que da `fase`")
        (is (= esperado (boolean (bloque/cumple? (bloque/fase paso 1 2) 5 3))))))))

(deftest cumple-en-las-decisiones-que-el-enunciado-resuelve
  (doseq [[fase k n esperado pasaje] decisiones-del-enunciado]
    (testing (str "fase " fase ", k = " k ", N = " n ". " pasaje)
      (is (= esperado (boolean (bloque/cumple? fase k n)))))))

(deftest con-n-igual-a-1-la-decision-se-cumple-siempre
  (testing "fases 0 a 299 y los k de 4.4 (0 a 16): pares [fase k] en que no se cumple"
    (is (= []
           (vec (for [fase (range 300)
                      k (range 17)
                      :when (not (bloque/cumple? fase k 1))]
                  [fase k]))))))

(def materiales-5-2
  #{:aire :pared :piedra :vidrio :madera :arena :agua :lava :vapor :fuego})

(def casos-6-1
  [{:tabla "6.1 tabla principal"
    :casos
    [[[:arena :aire :aire :aire] 0 [:aire :aire :arena :aire] "La arena cae"]
     [[:aire :arena :aire :aire] 0 [:aire :aire :aire :arena] "Cae por la columna derecha"]
     [[:arena :aire :pared :aire] 0 [:aire :aire :pared :arena]
      "Cae en diagonal porque abajo hay pared"]
     [[:arena :aire :agua :aire] 0 [:aire :aire :arena :agua]
      "La arena se hunde: intercambia con el agua"]
     [[:aire :aire :agua :aire] 0 [:aire :aire :aire :agua] "El agua apoyada se desliza"]
     [[:aire :aire :agua :aire] 1 [:aire :aire :agua :aire]
      "En esta fase no se desliza (1 de cada 2)"]
     [[:aire :aire :vapor :aire] 0 [:vapor :aire :aire :aire] "El vapor sube"]
     [[:agua :aire :lava :aire] 0 [:vapor :aire :piedra :aire] "Reaccion agua-lava (par 0 y 2)"]
     [[:agua :aire :lava :aire] 1 [:aire :aire :lava :agua]
      "Sin reaccion en esta fase; el agua baja en diagonal"]
     [[:lava :aire :aire :aire] 0 [:lava :aire :aire :aire] "Lava viscosa: no se mueve"]
     [[:lava :aire :aire :aire] 1 [:lava :aire :aire :aire] "Idem"]
     [[:lava :aire :aire :aire] 2 [:aire :aire :aire :lava]
      "Se mueve: cae y luego se desliza (ver nota)"]
     [[:fuego :aire :aire :aire] 0 [:aire :aire :aire :aire]
      "El fuego se apaga (decaimiento con N = 10)"]]}

   {:tabla "6.1 casos borde, bloques que no cambian"
    :casos
    [[[:aire :aire :aire :aire] 0 [:aire :aire :aire :aire] "Bloque vacio: no cambia"]
     [[:pared :pared :pared :pared] 0 [:pared :pared :pared :pared] "Bloque fijo: no cambia"]
     [[:pared :aire :aire :aire] 0 [:pared :aire :aire :aire]
      "Un material fijo no se mueve y el aire no lo desplaza"]
     [[:arena :arena :pared :pared] 0 [:arena :arena :pared :pared]
      "Apoyada sobre material fijo: no se mueve"]
     [[:arena :aire :pared :pared] 0 [:arena :aire :pared :pared]
      "La arena no se desliza (es polvo)"]
     [[:lava :lava :piedra :piedra] 0 [:lava :lava :piedra :piedra]
      "Lava apoyada sobre material fijo"]
     [[:agua :agua :agua :agua] 0 [:agua :agua :agua :agua]
      "Misma densidad: no se intercambian"]]}

   {:tabla "6.1 casos borde, caida"
    :casos
    [[[:arena :aire :aire :pared] 0 [:aire :aire :arena :pared] "Cae recto"]
     [[:arena :aire :pared :aire] 0 [:aire :aire :pared :arena]
      "Cae en diagonal: abajo hay pared"]
     [[:arena :aire :pared :aire] 1 [:aire :aire :pared :arena] "Idem con otra fase"]
     [[:aire :arena :pared :aire] 1 [:aire :aire :pared :arena]
      "Idem con la arena en la columna derecha"]
     [[:arena :aire :piedra :aire] 0 [:aire :aire :piedra :arena] "Cae en diagonal sobre piedra"]
     [[:vidrio :arena :piedra :aire] 0 [:vidrio :aire :piedra :arena]
      "Cae en diagonal; el vidrio no se mueve"]
     [[:arena :arena :pared :aire] 0 [:arena :aire :pared :arena]
      "Solo una arena puede bajar; la otra queda apoyada"]
     [[:arena :arena :aire :aire] 0 [:aire :aire :arena :arena] "Caen las dos"]
     [[:agua :agua :aire :aire] 0 [:aire :aire :agua :agua] "Caen las dos"]
     [[:agua :aire :aire :aire] 1 [:aire :aire :agua :aire] "Cae y no se desliza (fase 1)"]
     [[:agua :aire :aire :aire] 0 [:aire :aire :aire :agua] "Cae y se desliza (fase 0)"]]}

   {:tabla "6.1 casos borde, hundimiento por densidad"
    :casos
    [[[:arena :aire :agua :aire] 1 [:aire :aire :arena :agua] "La arena se hunde en el agua"]
     [[:arena :arena :agua :agua] 1 [:agua :agua :arena :arena]
      "Las dos arenas se hunden y el agua sube"]
     [[:arena :aire :lava :aire] 0 [:aire :aire :arena :lava] "La arena se hunde en la lava"]
     [[:lava :aire :agua :aire] 1 [:lava :aire :agua :aire]
      "La lava no tiene permiso de movimiento en esta fase: no se hunde"]
     [[:lava :aire :agua :aire] 5 [:aire :aire :lava :agua]
      "La lava tiene permiso y no hay reaccion en esta fase: se hunde en el agua"]]}

   {:tabla "6.1 casos borde, deslizamiento horizontal"
    :casos
    [[[:agua :aire :pared :pared] 0 [:agua :aire :pared :pared]
      "Un liquido en la fila superior no se desliza"]
     [[:agua :aire :pared :pared] 1 [:agua :aire :pared :pared] "Idem con otra fase"]
     [[:pared :pared :agua :aire] 0 [:pared :pared :aire :agua]
      "Liquido apoyado: se desliza (fase 0)"]
     [[:pared :pared :agua :aire] 1 [:pared :pared :agua :aire]
      "Liquido apoyado: no se desliza (fase 1)"]
     [[:pared :pared :agua :agua] 0 [:pared :pared :agua :agua]
      "Sin vacio al lado: no hay deslizamiento"]
     [[:aire :aire :pared :agua] 0 [:aire :aire :pared :agua]
      "Hay pared al lado: no hay deslizamiento"]
     [[:aire :aire :lava :aire] 1 [:aire :aire :lava :aire]
      "Lava sin permiso de movimiento: no se desliza"]
     [[:aire :aire :lava :aire] 0 [:aire :aire :aire :lava] "Lava con permiso: se desliza"]]}

   {:tabla "6.1 casos borde, gases"
    :casos
    [[[:aire :aire :vapor :vapor] 0 [:vapor :vapor :aire :aire] "Los dos suben"]
     [[:vapor :vapor :aire :aire] 0 [:aire :vapor :aire :agua]
      (str "El vapor de la posicion 0 se condensa (k = 0), cae y se desliza; "
           "el otro no cambia")]
     [[:vapor :aire :pared :pared] 1 [:aire :vapor :pared :pared]
      "Un gas se desliza en la fila superior"]
     [[:aire :aire :fuego :aire] 0 [:fuego :aire :aire :aire] "El fuego sube"]
     [[:aire :aire :fuego :aire] 1 [:aire :fuego :aire :aire] "Sube por la otra columna"]
     [[:aire :aire :vapor :fuego] 1 [:vapor :fuego :aire :aire] "Los dos suben"]
     [[:aire :aire :fuego :vapor] 0 [:fuego :vapor :aire :aire] "Los dos suben"]
     [[:vapor :fuego :pared :pared] 1 [:vapor :fuego :pared :pared]
      "No hay vacio ni material menos denso debajo: no cambia"]
     [[:fuego :fuego :pared :pared] 3 [:aire :fuego :pared :pared]
      "La posicion 1 decae a aire (k = 1) y el otro fuego se desliza hacia ella"]]}

   {:tabla "6.1 casos borde, reacciones"
    :casos
    [[[:fuego :agua :pared :pared] 2 [:aire :vapor :pared :pared] "Fuego-agua (N = 2) se cumple"]
     [[:fuego :agua :pared :pared] 3 [:fuego :agua :pared :pared] "No se cumple"]
     [[:agua :lava :pared :pared] 2 [:vapor :piedra :pared :pared] "Agua-lava (N = 2)"]
     [[:arena :lava :pared :pared] 12 [:vidrio :lava :pared :pared]
      "Arena-lava (N = 20): se cumple"]
     [[:arena :lava :pared :pared] 11 [:arena :lava :pared :pared] "No se cumple"]
     [[:lava :madera :pared :pared] 2 [:lava :fuego :pared :pared]
      "Lava-madera (N = 5): se cumple"]
     [[:lava :madera :pared :pared] 1 [:lava :madera :pared :pared] "No se cumple"]
     [[:fuego :madera :pared :pared] 12 [:fuego :fuego :pared :pared]
      "Fuego-madera (N = 20): se cumple"]
     [[:fuego :madera :pared :pared] 11 [:fuego :madera :pared :pared] "No se cumple"]
     [[:pared :pared :lava :madera] 0 [:pared :pared :lava :fuego]
      "Par de la fila inferior (q = 1, k = 5): se cumple"]
     [[:fuego :agua :lava :pared] 2 [:aire :vapor :lava :pared]
      (str "Las reacciones usan los materiales actuales: el agua ya es vapor cuando se "
           "evalua el par con la lava, y agua-lava no ocurre")]]}

   {:tabla "6.1 casos borde, decaimiento"
    :casos
    [[[:pared :fuego :pared :pared] 3 [:pared :aire :pared :pared]
      "Fuego -> aire (N = 10, k = 1)"]
     [[:pared :fuego :pared :pared] 0 [:pared :fuego :pared :pared] "No se cumple"]
     [[:pared :vapor :pared :pared] 93 [:pared :agua :pared :pared]
      "Vapor -> agua (N = 100, k = 1)"]
     [[:pared :vapor :pared :pared] 92 [:pared :vapor :pared :pared] "No se cumple"]]}])

(defn- bloque-valido?
  "Verdadero si `b` es un vector de 4 materiales de la tabla 5.2."
  [b]
  (and (vector? b) (= 4 (count b)) (every? materiales-5-2 b)))

(defn- verificar-casos
  "Aplica la regla del bloque a cada fila de cada tabla de `tablas` y la compara con el
  resultado esperado. La etiqueta de cada asercion dice la tabla, el numero de fila, el bloque,
  la fase y el comentario. No devuelve nada util."
  [tablas]
  (doseq [{:keys [tabla casos]} tablas
          [indice [entrada fase esperado comentario]] (map-indexed vector casos)]
    (testing (str tabla ", fila " (inc indice) ": " entrada " con fase " fase ". " comentario)
      (is (= esperado (bloque/aplicar-regla entrada fase))))))

(deftest la-transcripcion-de-6-1-esta-completa
  (testing (str "filas por tabla: principal, no cambian, caida, hundimiento, deslizamiento, "
                "gases, reacciones, decaimiento")
    (is (= [13 7 11 5 8 9 11 4] (mapv (comp count :casos) casos-6-1))))
  (testing "en total son 68"
    (is (= 68 (count (mapcat :casos casos-6-1)))))
  (testing "filas mal escritas: bloques que no son 4 materiales de la tabla 5.2, o fase invalida"
    (is (= []
           (vec (remove (fn [[entrada fase esperado comentario]]
                          (and (bloque-valido? entrada)
                               (bloque-valido? esperado)
                               (nat-int? fase)
                               (string? comentario)))
                        (mapcat :casos casos-6-1)))))))

(deftest aplicar-regla-reproduce-los-68-casos-de-6-1
  (verificar-casos casos-6-1))

(deftest aplicar-regla-devuelve-un-bloque
  (let [resultado (bloque/aplicar-regla [:arena :aire :aire :aire] 0)]
    (testing "el resultado es un vector (arquitectura 3)"
      (is (vector? resultado)))
    (testing "tiene las 4 posiciones"
      (is (= 4 (count resultado))))))

(def fases-de-un-ciclo (range 300))

(defn- fases-en-que-cambia
  "Vector con las fases de `fases-de-un-ciclo` en las que la regla del bloque modifica a
  `entrada`. Vacio si no cambia con ninguna."
  [entrada]
  (vec (remove #(= entrada (bloque/aplicar-regla entrada %)) fases-de-un-ciclo)))

(deftest un-liquido-en-la-fila-superior-no-se-desliza-con-ninguna-fase
  (testing "4.4 etapa 3: [agua aire pared pared], fases 0 a 299 en las que cambia"
    (is (= [] (fases-en-que-cambia [:agua :aire :pared :pared])))))

(deftest un-bloque-de-materiales-fijos-no-cambia-con-ninguna-fase
  (testing "5.1: [pared piedra vidrio madera], fases 0 a 299 en las que cambia"
    (is (= [] (fases-en-que-cambia [:pared :piedra :vidrio :madera])))))

(def casos-derivados
  [{:tabla "4.4 etapa 2, materiales en el orden inverso al de la tabla 5.3"
    :casos
    [[[:agua :fuego :pared :pared] 2 [:vapor :aire :pared :pared]
      "Fuego-agua con el agua primero: cada posicion cambia segun lo que contenia"]
     [[:lava :agua :pared :pared] 2 [:piedra :vapor :pared :pared]
      "Agua-lava con la lava primero"]
     [[:lava :arena :pared :pared] 12 [:lava :vidrio :pared :pared]
      "Arena-lava con la lava primero"]
     [[:madera :fuego :pared :pared] 12 [:fuego :fuego :pared :pared]
      "Fuego-madera con la madera primero"]
     [[:madera :lava :pared :pared] 2 [:fuego :lava :pared :pared]
      "Lava-madera con la madera primero"]]}

   {:tabla "4.4 etapa 2, pares q = 3, 4 y 5 (6.1 solo ejercita q = 0, 1 y 2)"
    :casos
    [[[:pared :agua :pared :lava] 1 [:pared :vapor :pared :piedra]
      "Par 1 y 3 (q = 3, k = 7): se cumple"]
     [[:pared :agua :pared :lava] 0 [:pared :agua :pared :lava]
      "Par 1 y 3 (q = 3, k = 7): no se cumple"]
     [[:agua :pared :pared :lava] 0 [:vapor :pared :pared :piedra]
      "Par 0 y 3 (q = 4, k = 8): se cumple"]
     [[:agua :pared :pared :lava] 1 [:agua :pared :pared :lava]
      "Par 0 y 3 (q = 4, k = 8): no se cumple"]
     [[:pared :agua :lava :pared] 1 [:pared :vapor :piedra :pared]
      "Par 1 y 2 (q = 5, k = 9): se cumple"]
     [[:pared :agua :lava :pared] 0 [:pared :agua :lava :pared]
      "Par 1 y 2 (q = 5, k = 9): no se cumple"]]}

   {:tabla "4.4 etapa 2, numero de decision de los pares q = 2 a 5 con lava-madera (N = 5)"
    :casos
    [[[:lava :pared :madera :pared] 3 [:lava :pared :fuego :pared]
      "Par 0 y 2 (q = 2, k = 6): se cumple, y con el k del par 0 y 3 no"]
     [[:pared :lava :pared :madera] 6 [:pared :lava :pared :fuego]
      "Par 1 y 3 (q = 3, k = 7): se cumple, y con el k del par 1 y 2 no"]
     [[:lava :pared :pared :madera] 4 [:lava :pared :pared :fuego]
      "Par 0 y 3 (q = 4, k = 8): se cumple, y con el k del par 0 y 2 no"]
     [[:pared :lava :madera :pared] 2 [:pared :lava :fuego :pared]
      "Par 1 y 2 (q = 5, k = 9): se cumple, y con el k del par 1 y 3 no"]]}

   {:tabla "4.4 etapa 2, dos pares cuya decision se cumple: el segundo usa los materiales actuales"
    :casos
    [[[:agua :fuego :pared :lava] 0 [:vapor :aire :pared :lava]
      "El par 0 y 3 ya no encuentra agua: la lava no pasa a piedra"]]}

   {:tabla "4.4 etapa 1, decaimiento en las posiciones 2 y 3 (6.1 solo lo ejercita en 0 y 1)"
    :casos
    [[[:pared :pared :fuego :pared] 6 [:pared :pared :aire :pared]
      "Fuego en la posicion 2 (k = 2): decae"]
     [[:pared :pared :fuego :pared] 3 [:pared :pared :fuego :pared]
      "Fuego en la posicion 2 (k = 2): no decae"]
     [[:pared :pared :pared :fuego] 9 [:pared :pared :pared :aire]
      "Fuego en la posicion 3 (k = 3): decae"]
     [[:pared :pared :pared :fuego] 6 [:pared :pared :pared :fuego]
      "Fuego en la posicion 3 (k = 3): no decae"]]}

   {:tabla "4.4 etapa 3a, permiso en las posiciones 1 y 3 (6.1 usa lava solo en 0 y 2)"
    :casos
    [
     [[:aire :lava :aire :aire] 1 [:aire :aire :aire :lava]
      "Lava en la posicion 1 (k = 11): con permiso, cae"]
     [[:aire :lava :aire :aire] 0 [:aire :lava :aire :aire]
      "Lava en la posicion 1 (k = 11): sin permiso, no cae"]
     [[:aire :aire :aire :lava] 2 [:aire :aire :lava :aire]
      "Lava en la posicion 3 (k = 13): con permiso, se desliza hacia la izquierda"]
     [[:aire :aire :aire :lava] 0 [:aire :aire :aire :lava]
      "Lava en la posicion 3 (k = 13): sin permiso, no se desliza"]]}

   {:tabla "4.4 etapa 3c, deslizamiento hacia la izquierda y gas en la fila inferior"
    :casos
    [[[:pared :pared :aire :agua] 0 [:pared :pared :agua :aire]
      "Liquido apoyado con el vacio a la izquierda: se desliza (fase 0)"]
     [[:pared :pared :aire :agua] 1 [:pared :pared :aire :agua]
      "Liquido apoyado con el vacio a la izquierda: no se desliza (fase 1)"]
     [[:aire :vapor :pared :pared] 1 [:vapor :aire :pared :pared]
      "Gas en la fila superior con el vacio a la izquierda: se desliza"]
     [[:pared :pared :vapor :aire] 0 [:pared :pared :aire :vapor]
      "Gas en la fila inferior: se desliza (fase 0)"]
     [[:pared :pared :vapor :aire] 1 [:pared :pared :vapor :aire]
      "Gas en la fila inferior: no se desliza (fase 1)"]]}])

(deftest aplicar-regla-en-reglas-de-4-4-sin-fila-en-6-1
  (verificar-casos casos-derivados))

(def fases-de-cambio-por-periodo
  [[[:pared :vapor :pared :pared] [93 193 293]
    "Decaimiento del vapor, N = 100"]
   [[:arena :lava :pared :pared] (range 12 300 20)
    "Arena-lava, N = 20"]
   [[:fuego :agua :pared :pared] (range 0 300 2)
    "Fuego-agua, N = 2: cambia con toda fase par y con ninguna impar"]
   [[:madera :fuego :pared :pared] (sort (concat (range 3 300 10) (range 12 300 20)))
    "Fuego-madera, N = 20, mas el decaimiento del fuego, N = 10"]])

(deftest los-periodos-de-5-2-y-5-3-fijan-las-fases-en-que-cambia-cada-bloque
  (doseq [[entrada esperadas periodo] fases-de-cambio-por-periodo]
    (testing (str entrada ", fases 0 a 299 en las que cambia. " periodo)
      (is (= (vec esperadas) (fases-en-que-cambia entrada))))))