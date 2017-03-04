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

(defn vec-strange-trime-them
  "Take a vector of strings and for each, split by ;"
  [vect]
  (map (fn [x] (clojure.string/trim x)) vect)
  
  )


(defn parse-time-input
  "Take a vector of strings and for each, split by ;"
  [vect]
  (
   map vec-strange-trime-them

   (map (fn [x] (strlib/split x #";")) vect)

  )
  
  )


; (defn   ; boot.user=> (clojure.string/trim " a sdfd ")

(defn make-time-vec-to-put-dic
  "Take a vector of attributes and prepare a dict.
  Also incorporate the date.
 
  TODO : strip out the white space.

  TODO: also, decide, should have a special project-identifier like 'adhoc',
      for doing things that don't really have a project. That would be way
      simpler than trying to handle both kinds of cases.
  "
  [vect date]

  ; at this point may need to limit to only length 4 ? 
  ; (let [[time-vect core-category project-identifier sub-category note] vect]
  (let [
        [time-vect core-category project-identifier sub-category note] vect

        [start-time end-time] (strlib/split time-vect #"-")

        v {:start-time start-time :end-time end-time
               :date date :core-category core-category
               :sub-category sub-category :project-identifier project-identifier}
        ]
    v

       ;(println start-time "," end-time "," core-category "," project-identifier
       ;     "," sub-category ","  ".")

       )
  )


;    (apply merge '(
;                   {}
;                   {:core_category }
;                   {}))
;    )

  ;(if ((count vect) 5)
    ; 14:05 - 14:55 ; work    ; consulting               ; eng support 
    ; 18:25 - 19:25 ; work    ; github-lms-platform-1151 ; code 
    ; 14:10 - 16:50 ; work    ; BE-427                   ; QA 
    ; 19:50 - 20:00 ; personal; time capture project     ; code 
    ; time_start - time_end ; core_category ; project_identifier ; sub_category ; notes
    ; discussion: (can often get into random discussions)

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
  which dicts were successfully used and show useful errors for which werent
  
  Also need dates for each array of dicts. 
  "
  [in]
  (let [
        result (parse-time-input in)
        
        
        
        
        ]
    result 
     )
  )




