(ns app.gif
  (:require
   ["gifuct-js" :refer [parseGIF decompressFrames]]
   ["./js/gif-player.js" :refer [createGifPlayer]]))

(defn fetch-gif [url]
  (-> (js/fetch url #js{:method "GET" :mode "cors" :cache "default"})
      (.then (fn [res]
               (if (not (.-ok res))
                 (throw (js/Error. "Invalid resource url"))
                 res)))
      (.then (fn [res] (.arrayBuffer res)))
      (.then (fn [buf] (parseGIF buf)))
      (.then (fn [gif] (decompressFrames gif true)))))

(defn create-gif-player [{:keys [frames canvas fit-to-screen?]}]
  (createGifPlayer #js {:frames frames :canvas canvas :fitToScreen fit-to-screen?}))