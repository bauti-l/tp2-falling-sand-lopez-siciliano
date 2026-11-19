(ns falling-sand.medicion
  "Herramienta del hito H3: mide cuántos pasos por segundo calcula la
  simulación en la grilla por defecto de 160 x 120 (enunciado 3,
  \"Velocidad\"), recorriendo las franjas de bloques en secuencial
  (`avanzar-con map`) y en paralelo (`avanzar`). Se corre con
  `lein run -m falling-sand.medicion`.

  No es parte de la simulación: es el único namespace que lee el reloj
  (docs/arquitectura.md, sección 1, regla 7)."
  (:require [falling-sand.grilla :as grilla]
            [falling-sand.materiales :as materiales]
            [falling-sand.simulacion :as simulacion]))

(def ^:private ancho
  "Ancho de la grilla medida, en celdas: el de la grilla por defecto."
  160)

(def ^:private alto
  "Alto de la grilla medida, en celdas: el de la grilla por defecto."
  120)

(def ^:private pasos-de-calentamiento
  "Pasos que se calculan con cada recorrido antes de medir, para que la JVM
  compile el código caliente y la medición no incluya ese tiempo."
  300)

(def ^:private pasos-medidos
  "Pasos que se calculan con cada recorrido para medir su velocidad. Los dos
  recorridos parten del mismo mundo y calculan los mismos pasos, así que hacen
  exactamente el mismo trabajo."
  600)

(defn- grilla-cargada
  "Grilla de prueba de `ancho` x `alto` celdas, sin azar: la celda `(x, y)`
  tiene el material de índice `(7x + 13y) mod 10` en
  `materiales/orden-selector`. Aparecen los 10 materiales en partes iguales:
  9 de cada 10 celdas son partículas, móviles y fijas mezcladas."
  [ancho alto]
  (grilla/desde-filas
   (for [y (range alto)]
     (for [x (range ancho)]
       (nth materiales/orden-selector (mod (+ (* 7 x) (* 13 y)) 10))))))

(defn- pasos-por-segundo
  "Calcula `pasos` pasos con la función `avanzar` (mundo -> mundo) a partir de
  `mundo` y devuelve cuántos pasos por segundo dio, como double."
  [avanzar mundo pasos]
  (let [inicio (System/nanoTime)]
    (reduce (fn [actual _] (avanzar actual)) mundo (range pasos))
    (/ (* pasos 1e9) (- (System/nanoTime) inicio))))

(defn -main
  "Imprime la cantidad de núcleos disponibles y los pasos por segundo del
  recorrido secuencial y del paralelo, después de calentar la JVM con los dos.
  Recibe los argumentos de la línea de comandos y los ignora. Al terminar
  llama a `shutdown-agents`, para que los hilos de `pmap` no demoren la salida
  del proceso. Devuelve nil."
  [& _args]
  (let [inicial    (simulacion/mundo-inicial (grilla-cargada ancho alto))
        secuencial #(simulacion/avanzar-con map %)
        paralelo   simulacion/avanzar]
    (println (str "Grilla de " ancho " x " alto ", "
                  (grilla/contar-particulas (:grilla inicial)) " particulas al empezar"))
    (println "Nucleos disponibles:" (.availableProcessors (Runtime/getRuntime)))
    (pasos-por-segundo secuencial inicial pasos-de-calentamiento)
    (pasos-por-segundo paralelo inicial pasos-de-calentamiento)
    (println (format "Secuencial (avanzar-con map): %.1f pasos por segundo"
                     (pasos-por-segundo secuencial inicial pasos-medidos)))
    (println (format "Paralelo (avanzar, pmap):     %.1f pasos por segundo"
                     (pasos-por-segundo paralelo inicial pasos-medidos)))
    (shutdown-agents)))
