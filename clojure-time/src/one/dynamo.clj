(ns one.dynamo
  (:require
    [environ.core :refer [env]]
    [taoensso.faraday :as dynamo]))

; When we are in local development, we feed some fake keys and the endpoint
; for the local DynamoDB server. When we run in Lambda, :development will be
; `nil` and Faraday will grab the real configuration details.
(def client-config
  (if (:development env)
    {:access-key "OMGDEVELOPMENT"
     :secret-key "I_SHOULD_KEEP_THIS_SECRET!"

     ; Point the configuration at the DynamoDB Local
     :endpoint "http://localhost:8000"}

    {:endpoint "http://dynamodb.us-east-1.amazonaws.com"}
    )
)

(def table-name :times)

(defn put-prime
  "Place a single prime into our list"
  [index prime]
  (dynamo/put-item client-config table-name
                   {:index index
                    :prime prime}))

(defn list-primes
  "Get the entire list of primes.

  Note: Amazon discourages Scans on DynamoDB because it's expensive for read
  operations. This is only for demonstration purposes."
  []
  (->> (dynamo/scan client-config table-name)
       (map (comp int :prime))
       (apply sorted-set)
       vec))

(defn batch-write-times
  "Write many hashes, each as a new row."
  [items]
  (dynamo/batch-write-item client-config
                           {:times {
                                    :put items
                                    :delete []
                                    }
                            })
  )


(defn batch-write-summaries
  "Write many summaries, each as a new row."
  [items]
  (dynamo/batch-write-item client-config
                           {:summary {
                                    :put items
                                    :delete []
                                    }
                            })
  )


(defn get-times-for-date-range
  [start-date end-date]

  (
   dynamo/scan client-config :times {
                                    ;
                                    :attr-conds {
                                                 :date [
                                                        :between  [start-date
                                                                   end-date]
                                                        ]}})
  
  )

(defn get-summaries
  [start-date end-date summary-type core-category]
  (let [
        given-core-category (not (or (nil? core-category)
                                     (= "" core-category)))
        
        ] (if given-core-category
            ; then
            (
             dynamo/scan client-config
             :summary {
                       :attr-conds {
                                    :start-date [:eq start-date]
                                    :end-date [:eq end-date]
                                    :type [:eq summary-type]
                                    :core-category [:eq
                                                    core-category]
                                    }
                       })

            ; else
            (
             dynamo/scan client-config :summary {
                                                 ;
                                                 :attr-conds {
                                                              :start-date [:eq start-date]
                                                              :end-date [:eq end-date]
                                                              :type [:eq summary-type]
                                                              }
                                                 })

            )
    )

  

  )


(defn get-prime
  "Get a specific prime from our list"
  [index]
  (int (:prime (dynamo/get-item client-config table-name {:index index}))))

; Here we can programmatically create the table for DynamoDB Local
#_(dynamo/create-table client-config table-name
                       [:index :n]
                       {:throughput {:read 5 :write 5}
                        :block? true})


; table one: projects

#_(dynamo/create-table db/client-config :projects
                       [:index :s]  ; index: project id
                       {:throughput {:read 5 :write 5}
                        :block? true})

#_(dynamo/create-table db/client-config :times
                       [:index :n]  ; index: unix time of start of an event
                                    ; , since no two events can start at same time.
                       {:throughput {:read 5 :write 5}
                        :block? true})

#_(dynamo/create-table db/client-config :summary
                       [:index :s]  ; index: combination of start time and summary type.
                                    ; , because we can have as many summaries which
                                    ; , start at same time as there are kinds.
                       {:throughput {:read 5 :write 5}
                        :block? true})


