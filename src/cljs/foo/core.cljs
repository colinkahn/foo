(ns foo.core
  (:require [chord.client :refer  [ws-ch]]
            [clojure.browser.repl :as repl]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [chord.http :as http]
            [cljs.core.async :as a])
  (:require-macros  [cljs.core.async.macros :refer  [go]]))

;; (repl/connect "http://localhost:9000/repl")

(defn by-id [id]
    (.getElementById js/document id))

(defn react-id [node]
  (.getAttribute node "data-reactid"))

(enable-console-print!)

(go
  (let [resp (a/<! (http/get "/hello"))]
    (println (:body resp))))

(defn flag [cursor owner]
  (reify
    om/IWillUnmount
    (will-unmount [_]
      (println "unmounting"))
    om/IRenderState
    (render-state [_ _]
      (dom/div nil "FLAG"))))

(defn toggle-state [owner prop]
  (let [v (om/get-state owner prop)]
    (om/set-state! owner prop (not v))))

(defn root [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:message "Hello World!"})
    om/IDidMount
    (did-mount [_]
      (println (react-id (om/get-node owner))))
    om/IRenderState
    (render-state [_ {:keys [message]}]
      (let [flagged (om/get-state owner :flagged)]
        (dom/div #js {:onClick #(toggle-state owner :flagged)}
                 "Hello World!"
                (when flagged
                  (om/build flag nil)))))))

(go
    (let [{:keys [ws-channel error]} (<! (ws-ch "ws://localhost:3000/ws"
                                                {:format :transit-json}))]))

(def app-state (atom {}))

(om/root root app-state {:target js/document.body})
