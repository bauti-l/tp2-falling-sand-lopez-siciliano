(ns falling-sand.materiales
  "Datos de la simulación: la tabla de materiales (enunciado 5.2), la tabla de
  reacciones (enunciado 5.3) y el orden en que la interfaz lista los materiales
  (enunciado 3), más las funciones que leen esas tablas aplicando los valores
  por defecto del enunciado 5.1.

  Es parte del núcleo puro: solo define constantes inmutables y funciones
  puras. Agregar un material o una reacción es agregar una fila acá, sin tocar
  ninguna función (enunciado 4.1.5).")

(def materiales
  "Tabla de materiales del enunciado 5.2: mapa de material (keyword) a su mapa
  de propiedades.

  Propiedades que tienen todos los materiales:
    :simbolo  carácter con que se escribe el material en una grilla de texto.
    :color    color ARGB, el número tal como figura en la tabla (un entero no
              negativo, sin pasar a `int` con signo).
    :tipo     :vacio, :fijo, :polvo, :liquido o :gas (enunciado 5.1).

  Propiedades opcionales. La clave falta cuando la tabla no define el valor
  (lo muestra entre paréntesis o dice \"no\"); nunca vale nil:
    :densidad     entero. Si falta vale 0: ver `densidad`.
    :periodo      período de movimiento. Si falta vale 1: ver `periodo`.
    :decaimiento  mapa {:destino material, :periodo N}: el material pasa a ser
                  `destino` en una de cada N decisiones."
  {:aire   {:simbolo \. :color 0xFF000000 :tipo :vacio :densidad 0}
   :pared  {:simbolo \# :color 0xFF808080 :tipo :fijo}
   :piedra {:simbolo \p :color 0xFF555560 :tipo :fijo}
   :vidrio {:simbolo \v :color 0xFF9EDCE8 :tipo :fijo}
   :madera {:simbolo \m :color 0xFF8B5A2B :tipo :fijo}
   :arena  {:simbolo \a :color 0xFFE0C068 :tipo :polvo :densidad 4}
   :agua   {:simbolo \g :color 0xFF2060E0 :tipo :liquido :densidad 2}
   :lava   {:simbolo \l :color 0xFFE85010 :tipo :liquido :densidad 3 :periodo 3}
   :vapor  {:simbolo \^ :color 0xFFC8C8D8 :tipo :gas :densidad -1
            :decaimiento {:destino :agua :periodo 100}}
   :fuego  {:simbolo \f :color 0xFFFFC020 :tipo :gas :densidad -2
            :decaimiento {:destino :aire :periodo 10}}})
            
(def reacciones
  "Tabla de reacciones del enunciado 5.3: mapa del conjunto de los dos
  materiales que reaccionan a los datos de la reacción.

  La clave es un conjunto porque la reacción vale con los materiales en
  cualquiera de los dos órdenes. El valor es un mapa con:
    :periodo    período N de la decisión que habilita la reacción.
    :resultado  mapa de cada material de la clave al material en que se
                convierte. Un material que la reacción no cambia figura
                convertido en sí mismo.

  Se consulta con `reaccion`."
  {#{:fuego :agua}   {:periodo 2  :resultado {:fuego :aire   :agua :vapor}}
   #{:agua :lava}    {:periodo 2  :resultado {:agua :vapor   :lava :piedra}}
   #{:arena :lava}   {:periodo 20 :resultado {:arena :vidrio :lava :lava}}
   #{:fuego :madera} {:periodo 20 :resultado {:fuego :fuego  :madera :fuego}}
   #{:lava :madera}  {:periodo 5  :resultado {:lava :lava    :madera :fuego}}})

(def orden-selector
  "Vector con los diez materiales en el orden en que los muestra el selector de
  la interfaz (enunciado 3): aire, pared, arena, agua, vapor, lava, fuego,
  madera, piedra, vidrio."
  [:aire :pared :arena :agua :vapor :lava :fuego :madera :piedra :vidrio])
