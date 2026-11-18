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
