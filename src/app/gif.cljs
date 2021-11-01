(ns app.gif
  (:require
   ["gifuct-js" :refer [parseGIF decompressFrames]]
   ["./js/gif-player.js" :refer [createGifPlayer]]))

(defn- fetch-gif [url]
  (-> url
      (#(js/fetch % #js{:method "GET" :mode "cors" :cache "default"}))
      (.then (fn [res] (.arrayBuffer res)))
      (.then (fn [buf] (parseGIF buf)))
      (.then (fn [gif] (decompressFrames gif true)))))

(defn create-gif-player [src]
  (-> src
      fetch-gif
      (.then (fn [frames]
               (createGifPlayer
                frames 
                (js/document.getElementById "gif-display"))))))