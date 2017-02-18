(ns one.core
  (:require
    [one.dynamo :as db]
    [uswitch.lambada.core :refer [deflambdafn]]
    [cheshire.core :as json]
    [taoensso.timbre :as log]
    [clojure.java.io :as io]
    ;[clojure.string :only (split triml)] 
    [clojure.string :as strlib]
    ))

(defn sieve-of-eratosthenes
  "Find all the prime numbers between 2 and <number>"
  [number]
  (if (< number 2)
    (str number " is too low")
    (let [prime-list (filter #(<= % number) (db/list-primes))
          largest-prime (if (zero? (count prime-list)) 2 (apply max prime-list))
          pre-filter (range largest-prime (inc number))]
      (loop [primes prime-list
             collection (filter #(not-any? (fn [p] (zero? (mod % p))) primes) pre-filter)]
        (if-not (seq collection)
          (vec (apply sorted-set primes))
          (let [prime (apply min collection)]
            (db/put-prime (count primes) prime)
            (recur (conj primes prime)
                   (remove #(zero? (mod % prime)) collection))))))))


(defn blahd
  "blarg"
  [number]
  number)

; The name of the lambda must include the full namespace in it's name.
(deflambdafn one.core.lambdafn
  [in out context]
  (log/info "Starting Lambda")
  (let [body (-> in io/reader (json/parse-stream keyword))
        result (sieve-of-eratosthenes (-> body :max num))]
    (with-open [w (io/writer out)]
      (json/generate-stream result w)
      (log/info "Lambda finished"))))

(defn parse-time-input
  "Take a vector of strings and for each, split by ;"
  [vect]
  (map (fn [x] (strlib/split x #";")) vect)
  
  )

(defn one.core.taketime
  "Take input time string and return parsed dictionary or fail. 
  The input is an array of dicts. The output should be helpful to say
  which dicts were successfully used and show useful errors for which werent"
  [in]
  (log/info "take time")
  (let [body (-> in io/reader (json/parse-stream keyword))
        result (parse-time-input body)]
    (result
     
     ))
  )


(defn one.core.taketime-non-stream
  "Take input time string and return parsed dictionary or fail. 
  The input is an array of dicts. The output should be helpful to say
  which dicts were successfully used and show useful errors for which werent"
  [in]
  (let [
        result (parse-time-input in)]
    result 
     )
  )




