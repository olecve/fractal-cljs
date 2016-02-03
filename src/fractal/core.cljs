(ns fractal.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [fractal.canvas :as canvas]
            [fractal.complex :as complex]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <! >! alts! merge]])
  (:import [goog.events EventType]))

(def ctx (canvas/get-ctx-by-id "canvas"))

(defn calculate-iteration-nr [p q K]
  (loop [x p
         y q
         k 0]
    (if (or (> k K)
            (> (complex/abs x y) 2.2))
      k
      (recur (+ p (- (* x x) (* y y))) (+ q (* 2 x y)) (inc k)))))

(defn calc-interval [begin end length]
  (/ (- end begin) length))

(defn draw-fractal [{:keys [window-begin-x window-begin-y window-end-x window-end-y canvas-width canvas-height K]}]
  (let [step-x (calc-interval window-begin-x window-end-x canvas-width)
        scale-x (/ 1 step-x)
        step-y (calc-interval window-begin-y window-end-y canvas-height)
        scale-y (/ 1 step-y)]
    (doseq [x (range window-begin-x window-end-x step-x)
            y (range window-begin-y window-end-y step-y)]
      (let [k (calculate-iteration-nr x y K)
            color (canvas/rgb (* k (/ 250 K)) 0 0)
            x-canvas (* (- x window-begin-x) scale-x)
            y-canvas (* (- y window-begin-y) scale-y)]
        (canvas/draw-point ctx color x-canvas y-canvas)))))

(defn events->chan
  ([el type] (events->chan el type (chan)))
  ([el type chan] (events/listen el type #(put! chan %))
   chan))

(defn mouse-drag-chan [el]
  (let [out (chan 1 (map (fn [event] {:x    (.-clientX event)
                                      :y    (.-clientY event)
                                      :type (.-type event)})))
        mouse-down (events->chan el EventType.MOUSEDOWN)
        mouse-move (events->chan js/window EventType.MOUSEMOVE)
        mouse-up (events->chan js/window EventType.MOUSEUP)]
    (go-loop [dragging? false]
             (let [[val channel] (alts! [mouse-down mouse-move mouse-up])]
               (condp = channel
                 mouse-down (do (>! out val)
                                (recur true))
                 mouse-move (if dragging?
                              (do (>! out val)
                                  (recur true))
                              (recur false))
                 mouse-up (do (>! out val)
                              (recur false)))))
    out))

(let [canvas (canvas/get-canvas-by-id "canvas")
      canvas-width 300
      canvas-height 300
      rect (.getBoundingClientRect canvas)
      left (.-left rect)
      top (.-top rect)
      zoom (events->chan canvas EventType.DBLCLICK)
      mouse-drag (mouse-drag-chan canvas)]
  (go-loop [window-begin-x -2.5
            window-begin-y -1.5
            window-end-x 0.5
            window-end-y 1.5
            dragging? false
            image-data nil
            start-dragging-x 0
            start-dragging-y 0]
           (when-not dragging? (draw-fractal {:window-begin-x window-begin-x
                                              :window-begin-y window-begin-y
                                              :window-end-x   window-end-x
                                              :window-end-y   window-end-y
                                              :canvas-width   canvas-width
                                              :canvas-height  canvas-height
                                              :K              25}))
           (if dragging?
             (let [value (<! mouse-drag)]
               (canvas/put-image-data ctx image-data (- (:x value) start-dragging-x) (- (:y value) start-dragging-y))
               (condp = (:type value)
                 EventType.MOUSEMOVE (recur window-begin-x window-begin-y window-end-x window-end-y true image-data start-dragging-x start-dragging-y)
                 EventType.MOUSEUP (recur (- window-begin-x (* (- (:x value) start-dragging-x) (calc-interval window-begin-x window-end-x canvas-width)))
                                          (- window-begin-y (* (- (:y value) start-dragging-y) (calc-interval window-begin-y window-end-y canvas-height)))
                                          (- window-end-x (* (- (:x value) start-dragging-x) (calc-interval window-begin-x window-end-x canvas-width)))
                                          (- window-end-y (* (- (:y value) start-dragging-y) (calc-interval window-begin-y window-end-y canvas-height)))
                                          false
                                          image-data
                                          start-dragging-x
                                          start-dragging-y)))
             (let [[value chanel] (alts! [mouse-drag zoom])]
               (condp = chanel
                 mouse-drag (recur window-begin-x
                                   window-begin-y
                                   window-end-x
                                   window-end-y
                                   true
                                   (canvas/get-image-data ctx 0 0 canvas-width canvas-height)
                                   (:x value)
                                   (:y value))
                 zoom (let [x-mouse-click (- (.-clientX value) left)
                            y-mouse-click (- (.-clientY value) top)
                            x-complex (+ window-begin-x (* x-mouse-click (calc-interval window-begin-x window-end-x canvas-width)))
                            y-complex (+ window-begin-y (* y-mouse-click (calc-interval window-begin-y window-end-y canvas-height)))
                            delta (/ (- window-end-x window-begin-x) 4)]
                        (recur
                          (- x-complex delta)
                          (- y-complex delta)
                          (+ delta x-complex)
                          (+ delta y-complex)
                          dragging?
                          (canvas/get-image-data ctx 0 0 canvas-width canvas-height)
                          start-dragging-x
                          start-dragging-y)))))))
