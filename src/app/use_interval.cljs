(ns app.use-interval
  (:require [helix.hooks :as hooks]))

(defn use-interval [callback delay]
  (let [saved-callback (hooks/use-ref nil)]
    (hooks/use-effect
     [callback]
     (set! (.-current saved-callback) callback))
    
    (hooks/use-effect
     [delay]
     (let [tick (fn [] (.current saved-callback))
           id (js/setInterval
               tick
               delay)]

       (fn []
         (js/clearInterval id))))))