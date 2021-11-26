(ns app.core
  (:require
   ["react-dom" :as rdom]
   [helix.core :refer [$]]
   [app.pages.home :refer [home]]))


(defn ^:dev/after-load start []
  (rdom/render ($ home) (js/document.getElementById "app")))


(defn ^:export init []
  (start))
