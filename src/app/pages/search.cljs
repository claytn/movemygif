(ns app.pages.search
  (:require
   [helix.core :refer [defnc]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   [cemerick.url :refer [url]]))

(def url-pattern #"(?i)^(?:(?:https?|ftp)://)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,}))\.?)(?::\d{2,5})?(?:[/?#]\S*)?$")

(defn valid-url? [url]
  (if (nil? url)
    false
    (re-matches url-pattern url)))

(declare search handle-selection!)
(defnc search [{:keys [handle-selection!]}]
  (let [[search set-search!] (hooks/use-state "")
        [error set-error!] (hooks/use-state nil)
        load-gif! (fn [url]
                    (handle-selection! url))]

    (hooks/use-effect
     []
     (let [query-params (:query (url (-> js/window .-location .-href)))
           gif-url (get query-params "gif")]

       (if (valid-url? gif-url)
         (do
           (set-search! gif-url)
           (load-gif! gif-url)
           (set-error! nil)))))
    (d/div
     (d/div
      {:style {:position "absolute" :right 10 :top 5}}
      (d/p  "movemygif v0.3")
      (d/a {:href "https://github.com/claytn/movemygif" :style {:float "right"}} "source"))

     (d/div
      {:class "search-container"}
      (d/form
       {:on-submit (fn [e]
                     (.preventDefault e)

                     (if (valid-url? search)
                       (do
                         (load-gif! search)
                         (set-error! nil)
                         (set-search! ""))

                       (set-error! "Invalid url. Please try another.")))}

       (d/h1 {:class "gif-search-heading"} "Paste GIF URL below:")
       (d/div {:class "gif-search-wrapper"}
              (d/input {:class "gif-search"
                        :name "url"
                        :value search
                        :on-change #(set-search! (.. %
                                                     -target
                                                     -value))})
              (d/button {:class "search-submit" :type "submit"} "Search"))


       (if error
         (d/p {:class "error"} error))

       (d/p "Don't want to go find a GIF? Try one of these!")
       (d/a {:href "/?gif=https://media.giphy.com/media/3o7buijTqhjxjbEqjK/giphy.gif"} "https://media.giphy.com/media/3o7buijTqhjxjbEqjK/giphy.gif")
       (d/br)
       (d/a {:href "/?gif=https://media.giphy.com/media/k2wSW3qZunETTq63e9/giphy.gif"} "https://media.giphy.com/media/k2wSW3qZunETTq63e9/giphy.gif")
       (d/br)
       (d/a {:href "/?gif=https://media.giphy.com/media/Vfhj19PusenfO/giphy.gif"} "https://media.giphy.com/media/Vfhj19PusenfO/giphy.gif"))))))

