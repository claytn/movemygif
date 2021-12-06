(ns app.pages.search
  (:require
   [helix.core :refer [defnc]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   [app.gif :as gif]
   [cemerick.url :refer [url]]))


;; TODO: ACTUALLY IMPLEMENT THIS.
(defn valid-url? [url] (not (nil? url)))

(declare search handle-selection!)
(defnc search [{:keys [handle-selection!]}]
  (let [[search set-search!] (hooks/use-state "")
        [error set-error!] (hooks/use-state nil)
        load-gif! (fn [url]
                    (-> url
                        gif/fetch-gif
                        (.then (fn [frames]
                                 (set-error! nil)
                                 (handle-selection! {:url url :frames frames})))
                        (.catch (fn [err]
                                  (println err)
                                  (set-error! "We had trouble loading this GIF for motion detection. Please try another GIF.")))))]

    (hooks/use-effect
     []
     (let [query-params (:query (url (-> js/window .-location .-href)))
           gif-url (get query-params "gif")]

       (if (valid-url? gif-url)
         (do
           (set-search! gif-url)
           (load-gif! gif-url)))))

    (d/div
     (d/div {:class "interaction-tools"}
            (d/form
             {:on-submit (fn [e]
                           (.preventDefault e)

                           (if (not (empty? search))
                             (do
                               (load-gif! search)
                               (set-search! ""))))}

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
               (d/p {:class "error"} "Error fetching gif. Try another URL."))

             (d/p "Don't want to go find a GIF? Copy this URL to test it out:")
             (d/a {:href "https://media.giphy.com/media/3o7buijTqhjxjbEqjK/giphy.gif"} "https://media.giphy.com/media/3o7buijTqhjxjbEqjK/giphy.gif"))))))