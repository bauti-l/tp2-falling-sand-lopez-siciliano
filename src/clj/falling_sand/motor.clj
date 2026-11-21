(ns falling-sand.motor
  "Motor de la aplicación: el estado de la simulación y la implementación de la
  interfaz Java `fallingsand.Motor` que usa la capa de JavaFX
  (docs/arquitectura.md, secciones 4, 5 y 6). Es el único namespace de Clojure
  con estado y efectos.

  El estado es un mapa con dos referencias:
    :mundo            agent con el mundo inmutable: el mapa de
                      `falling-sand.simulacion` (`:grilla` y `:paso`) más
                      `:en-pausa`.
    :pulso-pendiente  atom con true mientras haya un paso de pulso encolado
                      que todavía no empezó.

  Cada cambio del mundo (pintar, avanzar, limpiar, pausar) es una transición
  `mundo -> mundo` que se le envía al agent con `send`. Las transiciones son
  funciones puras; el pulso envuelve la suya en `atender-pulso`, que además
  baja la bandera `:pulso-pendiente`. El agent ejecuta sus acciones de a una,
  en el orden en que llegaron y fuera del hilo de JavaFX: no se pierde lo
  pintado ni el avance, y ningún método bloquea al que lo llama, que es el
  hilo de JavaFX. `cuadro()` lee el último mundo calculado sin esperar.

  `detener()` también se encola: le envía al agent `apagar`, que llama a
  `shutdown-agents` recién cuando terminaron las acciones anteriores. Así
  ningún paso queda a medio calcular cuando se apagan los hilos.

  El agent usa `:error-mode :continue`: si una transición lanza una excepción,
  se imprime en `*err*`, el mundo queda como estaba y las siguientes
  transiciones corren igual.

  No importa JavaFX."
  (:require [falling-sand.grilla :as grilla]
            [falling-sand.materiales :as materiales]
            [falling-sand.pincel :as pincel]
            [falling-sand.simulacion :as simulacion])
  (:import (fallingsand Cuadro Motor)
           (java.io PrintWriter Writer)))

(def ^:private nombres-de-materiales
  "Nombres de los materiales en el orden del selector de la interfaz
  (enunciado 3), como strings: el valor de `materiales()`. Es un vector, que ya
  es una `java.util.List`."
  (mapv name materiales/orden-selector))

(def ^:private color-de
  "Mapa de cada material a su color ARGB como `int` de 32 bits con signo, que
  es como lo guarda el arreglo `int[]` de `fallingsand.Cuadro`. Se calcula una
  sola vez a partir de los `:color` de `materiales/materiales`, para que
  `cuadro()` no convierta en cada llamada."
  (into {} (map (fn [[material {:keys [color]}]] [material (unchecked-int color)]))
        materiales/materiales))

;; ---------------------------------------------------------------------------
;; Transiciones: funciones puras mundo -> mundo
;; ---------------------------------------------------------------------------

(defn- alternar-pausa
  "Transición de Pausa (enunciado 3, \"Pausa / Reanudar\"). Recibe el mundo y
  lo devuelve en pausa si estaba corriendo y corriendo si estaba en pausa."
  [mundo]
  (update mundo :en-pausa not))

(defn- limpiar-grilla
  "Transición de Limpiar (enunciado 3, \"Limpiar\"). Recibe el mundo y lo
  devuelve con una grilla de aire del mismo tamaño. No cambia el número de
  paso (decisión D7)."
  [{:keys [grilla] :as mundo}]
  (assoc mundo :grilla (grilla/crear (grilla/ancho grilla) (grilla/alto grilla))))

(defn- pintar-trazo
  "Transición de pintar un trazo (enunciado 3, \"Pintar\"). Recibe el mundo, el
  material (keyword), el radio y las dos puntas del segmento, en celdas.
  Devuelve el mundo con la grilla pintada por `pincel/pintar`. Pinta tanto en
  pausa como con la simulación corriendo."
  [mundo material radio x0 y0 x1 y1]
  (update mundo :grilla pincel/pintar material radio x0 y0 x1 y1))

(defn- avanzar-si-corre
  "Transición de un pulso. Recibe el mundo y devuelve el siguiente
  (`simulacion/avanzar`) si la simulación corre, o el mismo si está en
  pausa."
  [mundo]
  (if (:en-pausa mundo) mundo (simulacion/avanzar mundo)))

(defn- avanzar-si-esta-en-pausa
  "Transición del botón Paso (enunciado 3, \"Paso\"). Recibe el mundo y
  devuelve el siguiente (`simulacion/avanzar`) si la simulación está en pausa,
  o el mismo si corre."
  [mundo]
  (if (:en-pausa mundo) (simulacion/avanzar mundo) mundo))

;; ---------------------------------------------------------------------------
;; Efectos
;; ---------------------------------------------------------------------------

(defn- atender-pulso
  "Acción del agent para un paso de pulso. Recibe el mundo y el atom
  `pulso-pendiente`. Primero lo pone en false, para que el pulso siguiente ya
  pueda encolar otro paso mientras se calcula este, y después aplica
  `avanzar-si-corre`. Devuelve el mundo siguiente."
  [mundo pulso-pendiente]
  (reset! pulso-pendiente false)
  (avanzar-si-corre mundo))

(defn- apagar
  "Última acción del agent, la que envía `detener()`. Recibe el mundo, llama a
  `shutdown-agents` y lo devuelve sin cambios. Como el agent ejecuta sus
  acciones en orden, corre después de las transiciones pendientes: ningún paso
  queda a medio calcular cuando se apagan los hilos de los agents, de los que
  también dependen `future` y `pmap`. Las acciones en curso terminan; las que
  se envíen después son rechazadas."
  [mundo]
  (shutdown-agents)
  mundo)

(defn- informar-error
  "Manejador de errores del agent. Recibe el agent y la excepción que lanzó una
  transición, e imprime la excepción con su traza en `*err*`. Devuelve nil. Con
  `:error-mode :continue` el agent conserva el mundo anterior a la transición
  que falló y sigue con las siguientes."
  [_agente ^Throwable error]
  (let [salida (PrintWriter. ^Writer *err* true)]
    (.println salida "Fallo una transicion del motor; el mundo queda como estaba antes de ella:")
    (.printStackTrace error salida)))

(defn- material-de-nombre
  "Material (keyword) de nombre `nombre`, uno de los strings de `materiales()`.
  Lanza `IllegalArgumentException` si no es el nombre de ningún material de la
  tabla."
  [nombre]
  (let [material (keyword nombre)]
    (if (contains? materiales/materiales material)
      material
      (throw (IllegalArgumentException. (str "Material desconocido: " nombre))))))

(defn- a-cuadro
  "Foto de un mundo para la interfaz. Recibe el mundo y devuelve un
  `fallingsand.Cuadro` con el color de cada celda, fila por fila desde la de
  arriba (ver `color-de`), el número de paso, la cantidad de partículas y si
  está en pausa."
  [{:keys [grilla paso en-pausa]}]
  (Cuadro. (int-array (map color-de (apply concat (grilla/filas grilla))))
           (long paso)
           (int (grilla/contar-particulas grilla))
           (boolean en-pausa)))


(defn crear-estado
  "Estado inicial del motor. Recibe el ancho y el alto de la grilla, en celdas.
  Devuelve el mapa `{:mundo agent :pulso-pendiente atom}` (ver el docstring
  del namespace) con una grilla de aire, en el paso 0, con la simulación
  corriendo y sin pulso pendiente."
  [ancho alto]
  {:mundo           (agent (assoc (simulacion/mundo-inicial (grilla/crear ancho alto))
                                  :en-pausa false)
                           :error-mode :continue
                           :error-handler informar-error)
   :pulso-pendiente (atom false)})

(defn esperar
  "Bloquea el hilo que la llama hasta que el agent del `estado` termine todas
  las transiciones que ese hilo ya le envió. Devuelve nil. Existe para que los
  tests sean deterministas; la interfaz no la usa."
  [estado]
  (await (:mundo estado)))

(defn como-motor
  "Implementación de `fallingsand.Motor` sobre `estado` (docs/arquitectura.md,
  sección 5). Recibe un estado de `crear-estado` y devuelve el objeto. Los
  métodos que cambian el mundo encolan una transición y vuelven enseguida:

    pintar         `pintar-trazo`, con el nombre pasado a keyword. Lanza
                   `IllegalArgumentException`, sin encolar nada, si el nombre
                   no es de ningún material.
    alternarPausa  `alternar-pausa`.
    avanzarUnPaso  `avanzar-si-esta-en-pausa`.
    limpiar        `limpiar-grilla`.
    pulso          `atender-pulso`, solo si el último mundo calculado está
                   corriendo y no hay otro paso de pulso pendiente: el
                   `compare-and-set!` sobre `:pulso-pendiente` deja encolar a
                   lo sumo uno.
    detener        `apagar`, detrás de las transiciones pendientes. Libera los
                   hilos de los agents de todo el proceso cuando terminan; se
                   llama una sola vez, porque después el agent ya no acepta
                   acciones.

  `cuadro()` arma la foto del último mundo calculado sin esperar a las
  transiciones encoladas."
  [{:keys [mundo pulso-pendiente]}]
  (let [{:keys [grilla]} @mundo
        ancho (grilla/ancho grilla)
        alto  (grilla/alto grilla)]
    (reify Motor
      (ancho [_] ancho)
      (alto [_] alto)
      (materiales [_] nombres-de-materiales)
      (pintar [_ material radio x0 y0 x1 y1]
        (send mundo pintar-trazo (material-de-nombre material) radio x0 y0 x1 y1))
      (alternarPausa [_]
        (send mundo alternar-pausa))
      (avanzarUnPaso [_]
        (send mundo avanzar-si-esta-en-pausa))
      (limpiar [_]
        (send mundo limpiar-grilla))
      (pulso [_]
        (when (and (not (:en-pausa @mundo))
                   (compare-and-set! pulso-pendiente false true))
          (send mundo atender-pulso pulso-pendiente)))
      (cuadro [_]
        (a-cuadro @mundo))
      (detener [_]
        (send mundo apagar)))))

(defn crear
  "Motor de `ancho` x `alto` celdas listo para usar. Recibe las dimensiones de
  la grilla y devuelve `(como-motor (crear-estado ancho alto))`."
  [ancho alto]
  (como-motor (crear-estado ancho alto)))
