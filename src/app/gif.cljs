(ns app.gif
  (:require
   [helix.core :refer [defnc $]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   ["gifuct-js" :refer [parseGIF decompressFrames]]
   ["./js/gif-player.js" :refer [createGifPlayer]]))


(defn fetch-gif [url]
  (-> url
      (#(js/fetch % #js{:method "GET" :mode "cors" :cache "default"}))
      (.then (fn [res] (.arrayBuffer res)))
      (.then (fn [buf] (parseGIF buf)))
      (.then (fn [gif] (decompressFrames gif true)))))

(def frame-img-data (atom nil))

(defn paint-frame [frame canvas patch]
  (let [ctx (.getContext canvas "2d")
        patch-ctx (.getContext patch "2d")
        temp (.createElement js/document "canvas")
        temp-ctx (.getContext temp "2d")
        dims (.-dims frame)
        imageData (.createImageData ctx (.-width dims) (.-height dims))
        img-data @frame-img-data]

    (if (= (.-disposalType frame) 2)
      (.clearRect patch-ctx 0 0 (.-width canvas) (.-height canvas)))

    (if (or
         (nil? img-data)
         (not= (.-height dims) (.-height img-data))
         (not= (.-width dims) (.-width img-data)))

      (do (set! (.-height temp) (.-height dims))
          (set! (.-width temp) (.-width dims))
          (reset! frame-img-data (.createImageData temp-ctx (.-width dims) (.-height dims)))))

    ;; How else to update object state nested in atom??
    (swap! frame-img-data (fn [prev]
                            (.set (.-data prev) (.-patch frame))
                            prev))

    (.putImageData temp-ctx @frame-img-data 0 0)
    (.drawImage patch-ctx temp (.-left dims) (.-top dims))

    (let [more-image-data (.getImageData patch-ctx 0 0 (.-width patch) (.-height patch))]
      (.putImageData ctx more-image-data 0 0)
      (.drawImage ctx canvas 0 0))))


(defn get-next-frame [{:keys [current-frame action frames]}]
  (case action
    :play (mod (+ current-frame 2) (count frames))
    :pause current-frame
    :rewind (mod (- current-frame 2) (count frames))
                ;; default
    current-frame))

(declare gif-player)
(defnc gif-player [{:keys [src action]}]
  (let [canvas (hooks/use-ref nil)
        [player set-player!] (hooks/use-state nil)]

    (hooks/use-effect
     [src]
     (-> src
         fetch-gif
         (.then (fn [frames]
                  (let [gp (createGifPlayer frames (.-current canvas))]
                    (.start gp)
                    (set-player! gp))
                  ;; (let [canvas (.-current canvas)
                  ;;       patch (.-current patch-canvas)
                  ;;       frames (decompressFrames g true)]
                  ;;   (js/setInterval (fn []
                  ;;                     ))
                  ;;   (paint-frame canvas patch (get frames frame))
                  ;;   (set-frame! inc))
                  ))))

    (hooks/use-effect [action player]
                      (if player
                        (case action
                          :play (.play player)
                          :pause (.pause player)
                          :rewind (.rewind player)
                          nil)))

    ;; (hooks/use-effect
    ;;  [callback]
    ;;  (set! (.-current tick) callback))

    ;; (hooks/use-effect
    ;;  :once
    ;;  (do
    ;;    (println "use-effect called")
    ;;    (.current tick)))


    (d/div
     (d/canvas {:ref canvas}))))
