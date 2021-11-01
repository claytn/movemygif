(ns app.core
  (:require
   ["react-dom" :as rdom]
   [helix.core :refer [$ defnc]]
   [helix.hooks :as hooks]
   [app.pages.home :refer [home]]))


(declare app)
(defnc app []
  
  (let [[page set-page!] (hooks/use-state :pick)]
    (case page
      :pick "pick page"
      :display "display page")))



(defn ^:dev/after-load start []
  (rdom/render ($ home) (js/document.getElementById "app")))


(defn ^:export init []
  (start))
