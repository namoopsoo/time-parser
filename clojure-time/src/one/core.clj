(ns one.core
  (:require
    [one.dynamo :as db]
    [uswitch.lambada.core :refer [deflambdafn]]
    [cheshire.core :as json]
    [taoensso.timbre :as log]
    [clojure.java.io :as io]
    ;[clojure.string :only (split triml)] 
    [clojure.string :as strlib]

    [clj-time.core :as t]
    [clj-time.format :as f]
    [clj-time.coerce :as c]
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


(defn make-time-vec-to-put-dic
  "Take a vector of attributes and prepare a dict.
  Also incorporate the date.

  TODO: also, decide, should have a special project-identifier like 'adhoc',
      for doing things that don't really have a project. That would be way
      simpler than trying to handle both kinds of cases.
  "
  [vect]

  ; at this point may need to limit to only length 4 ? 
  ; (let [[time-vect core-category project-identifier sub-category note] vect]
  (let [
        [date time-vect core-category project-identifier sub-category note] vect

        [start-time end-time] (map clojure.string/trim (strlib/split time-vect #"-"))

        date-formatter (f/formatters :date-hour-minute)

        index-date (c/to-long (f/parse date-formatter (str date "T" start-time)))
        end-date (c/to-long (f/parse date-formatter (str date "T" end-time)))
        time-length-miliseconds (- end-date index-date)
        time-length (/ time-length-miliseconds 60000) ; minutes

        ; TODO verify that t1 < t2... 
        ;   if not, then send error...

        v {
           :index index-date
           :start-time start-time :end-time end-time
               :date date :core-category core-category
               :sub-category sub-category :project-identifier project-identifier
           :time-length time-length}
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
  
  in: array, like, ['blah;blah;blah;blah', 'blah;blah;blah',...]

  (  get-hash-es   in ..)   then do an insert for each hash.
  "
  [in]
  (let [
        time-vectors (parse-time-input in)   
        
        ; then make hashes from each one
        time-hashes-res (
                     try {:stat true :output (map make-time-vec-to-put-dic time-vectors)}
                     (catch java.lang.IllegalArgumentException ex
                       {:stat false :err (str "Error " (.getMessage ex))}
                       )
                     
                     )

        ; if time-hashes is false, we return an error.
        ]

    ; then insert them all
    (if (time-hashes-res :stat)

      ; all good
      (db/batch-write-times (time-hashes-res :output))

      ; else
      (println (str "Failed, because, " (time-hashes-res :err))))

     )
  )

(defn summarize-data-per-keys
  [time-data keywords]
  
  (
   map (
        fn
        [[k vs]]

        ; Add (+) up all the time-length's per each group.
        [k (apply + (map (fn [x] (x :time-length)) vs))])
   (group-by
     #(select-keys % keywords)
     time-data))
  )

; :core-category :project-identifier


; Every end of day: run summarize-time for that day, to see how that prior day was used 
; Every end of week: run summarize-time fpr that week, , to look at prior week
; Every end of month: run summarize-time fpr that month, to look at how that prior month was used.
; 


(defn one.core.summarize-time
  "take a time start and stop (range) and read what is there,
  and write to the summary table for all of the project
  and subcategory combinations "
  [start-date end-date]
  (let [
        query-times ""
        time-data (db/get-times-for-date-range start-date end-date)
        all-times-lengths (map (fn [x] (x :time-length)) time-data)
        all-sum (reduce + all-times-lengths)

        ;project-identifiers (map (fn [x] (x :project-identifier)) time-data)
        ; core-categories (set (map (fn [x] (x :core-category)) time-data))

        ; DateRange - CoreCategory - TimeLengthSum
        summary-core-categories (summarize-data-per-keys time-data 
                                          [:core-category])

        ; (map (fn [y] {:core-category y :time-length (reduce + (map (fn [z] (z :time-length)) (filter (fn [x] (= (x :core-category) y) ) time-data)))}) core-categories)


        ; DateRange - CoreCategory - ProjectIdentifier - TimeLengthSum
        summaries-core-cat-and-projects (summarize-data-per-keys time-data 
                                          [:core-category :project-identifier])
        
        ; DateRange - CoreCategory - ProjectIdentifier - SubCategory - TimeLengthSum
        summaries-core-cat-projects-sub-cat (summarize-data-per-keys time-data 
                                          [:core-category :project-identifier
                                           :sub-category])

        ; DateRange - CoreCategory - SubCategory - TimeLengthSum
        ; SummaryType ( day, week, month, year)


        ; do..
        summaries (conj [] summary-core-categories
                        summaries-core-cat-and-projects
                        summaries-core-cat-projects-sub-cat
                        )


        ; add an entry to projects for any new projects?
        ;   maybe not necessary, since the summary table will show the data.
        ;   or actually: thats written to at a different time, to describe projects,
        ;   per project identifier.
        ]

    summaries)
  )


