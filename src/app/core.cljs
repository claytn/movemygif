(ns app.core
  (:require
   ["react-dom" :as rdom]
   [helix.core :refer [defnc $]]
   [helix.hooks :as hooks]
   [app.pages.search :refer [search]]
   [app.pages.display :refer [display]]))

(declare app)
(defnc app []
  (let [[url set-url!] (hooks/use-state nil)]
     (if url
       ($ display {:gif-url url})

       ($ search {:handle-selection! (fn [url]
                                       (set-url! url))}))))

(defn ^:dev/after-load start []
  (rdom/render ($ app) (js/document.getElementById "app")))


(defn ^:export init []
  (start))
