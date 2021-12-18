(ns app.core
  (:require
   ["react-dom" :as rdom]
   [helix.core :refer [defnc $]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   [app.pages.search :refer [search]]
   [app.pages.display :refer [display]]))

(declare app)
(defnc app []
  (let [[url set-url!] (hooks/use-state nil)]

    (d/div
     (d/div
      {:style {:position "absolute" :right 10 :top 5}}
      (d/p  "movemygif v0.2")
      (d/a {:href "https://github.com/claytn/movemygif" :style {:float "right"}} "source"))

     ;; When GIF loaded, show display page. Otherwise, assume search page
     ;; A+ routing if you ask me
     (if url
       ($ display {:gif-url url})

       ($ search {:handle-selection! (fn [url]
                                       (set-url! url))})))))

(defn ^:dev/after-load start []
  (rdom/render ($ app) (js/document.getElementById "app")))


(defn ^:export init []
  (start))
