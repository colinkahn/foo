(ns foo.core
  (:require [chord.client :refer  [ws-ch]]
            [clojure.browser.repl :as repl]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [chord.http :as http]
            [cljs.core.async :as a])
  (:require-macros  [cljs.core.async.macros :refer  [go go-loop]]))

;; (repl/connect "http://localhost:9000/repl")

(defn by-id [id]
    (.getElementById js/document id))

(defn react-id [node]
  (.getAttribute node "data-reactid"))

(enable-console-print!)

(def ws-chan-rec (a/chan))
(def ws-chan-snd (a/chan))

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

(defn change [e owner k]
  (om/set-state! owner k (.. e -target -value)))

(defn root [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:flagged true
       :msg ""
       :text "Test!"})
    om/IDidMount
    (did-mount [_]
      (let [ws-chan-rec (:ws-chan-rec (om/get-shared owner))]
        (go-loop []
          (let [msg (<! ws-chan-rec)]
            (om/set-state! owner :msg (-> msg :message :received)))
          (recur))))
    om/IRenderState
    (render-state [_ _]
      (let [flagged (om/get-state owner :flagged)
            msg (om/get-state owner :msg)
            text (om/get-state owner :text)]
        (dom/div #js {:onClick #(toggle-state owner :flagged)}
                "Hello World!"
                (dom/div nil msg)
                (dom/input
                  #js {:type "text"
                       :value text
                       :onChange #(change % owner :text)})
                 (dom/button
                   #js {:onClick #(a/put! (:ws-chan-snd (om/get-shared owner)) text)}
                   "Click Me!")
                (when flagged
                  (om/build flag nil)))))))

(go
  (let [{:keys [ws-channel error]} (<! (ws-ch "ws://localhost:8080/ws"
                                                  {:format :transit-json}))]
    ;; Recieve
    (go-loop []
      (when-let [msg (<! ws-channel)]
        (>! ws-chan-rec msg))
          (recur))
    ;; Sending
    (go-loop []
      (when-let [msg (<! ws-chan-snd)]
        (>! ws-channel msg))
          (recur))))

(def app-state (atom {}))

(om/root root app-state {:target js/document.body
                         :shared {:ws-chan-snd ws-chan-snd
                                  :ws-chan-rec ws-chan-rec}})
