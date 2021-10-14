(ns app.core
  (:require
   [helix.core :refer [$]]
   ["react-dom" :as rdom]
   [app.pages.home :refer [home]]))

(defn ^:dev/after-load start []
  (rdom/render ($ home) (js/document.getElementById "app")))



  ;; TODO: restructure away from any react related functionality. you don't need it
  ;; and it's slowing this thing down.

  ;; All you really need is a video element and a few canvas elements all of which
  ;; you can create on the fly when needed.

  ;; Basic page elements can be done with react if needed, but honestly, this should
  ;; be stupid simple.
  
(defn ^:export init []
  (start))
