(ns fractal.core
  (:require [fractal.canvas :as canvas]
            [fractal.complex :as complex]))

(def ctx (canvas/get-ctx-by-id "canvas"))

(defn calculate-iteration-nr [p q K]
  (loop [x p
         y q
         k 0]
    (if (or (> k K)
            (> (complex/abs x y) 2.2))
      k
      (recur (+ p (- (* x x) (* y y))) (+ q (* 2 x y)) (inc k)))))

(defn draw-fractal [{:keys [window-start-x window-start-y window-end-x window-end-y canvas-width canvas-heidth K step]}]
  (let [scale-x (/ canvas-width (- window-end-x window-start-x))
        scale-y (/ canvas-heidth (- window-end-y window-start-y))]
    (doseq [x (range window-start-x window-end-x step)
            y (range window-start-y window-end-y step)]
      (let [k (calculate-iteration-nr x y K)
            x-translated (- x window-start-x)
            y-translated (- y window-start-y)
            x-result (* x-translated scale-x)
            y-result (* y-translated scale-y)]
        (canvas/fill-rect
          ctx
          (canvas/rgb (* k (/ 250 K)) 0 0)
          x-result
          y-result
          (* step scale-x)
          (* step scale-y))))))

(draw-fractal {:window-start-x -2.5
               :window-start-y -1.5
               :window-end-x   0.5
               :window-end-y   1.5
               :canvas-width   300
               :canvas-heidth  300
               :K              25
               :step           0.01})
