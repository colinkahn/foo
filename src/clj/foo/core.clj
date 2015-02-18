(ns foo.core
  (:use compojure.core)
  (:use ring.middleware.resource)
  (:require
    [compojure.route :as route]
    [compojure.handler :as handler]
    [org.httpkit.server :as httpkit]
    [ring.util.response :as response]
    [chord.http-kit :refer [wrap-websocket-handler]]
    [clojure.core.async :refer [<! >! put! close! go-loop]]))

(defn ws-handler [{:keys [ws-channel] :as req}]
  (println "Opened connection from" (:remote-addr req))
  (go-loop []
    (when-let [{:keys [message error] :as msg} (<! ws-channel)]
      (prn "Message received:" msg)
      (>! ws-channel (if error
                       (format "Error: '%s'." (pr-str msg))
                       {:received (format "You passed: '%s' at %s." (pr-str message) (java.util.Date.))}))
      (recur))))

(defroutes main-routes
  (GET "/" [] (response/file-response "index.html"  {:root "resources/public"}))
  (GET "/hello" [] "Hello world!")
  (GET "/ws" [] (-> ws-handler
                  (wrap-websocket-handler {:format :transit-json})))
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (handler/site main-routes))

(defonce !server
  (atom nil))

(defn start-server! []
  (swap! !server
    (fn [running-server]
      (or running-server
          (httpkit/run-server app {:port 8080})))))

(defn stop-server []
  (swap! !server
    (fn [running-server]
      (when running-server
        ;; call the server to stop it
        (running-server)
        nil))))

(start-server!)
