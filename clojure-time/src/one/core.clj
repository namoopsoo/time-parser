(ns one.core
  (:require
    [one.dynamo :as db]
    [one.date-utils :as mydateutils]
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


; The name of the lambda must include the full namespace in it's name.
(deflambdafn one.core.lambdafn
  [in out context]
  (log/info "Starting Lambda")
  (let [body (-> in io/reader (json/parse-stream keyword))
        result (sieve-of-eratosthenes (-> body :max num))]
    (with-open [w (io/writer out)]
      (json/generate-stream result w)
      (log/info "Lambda finished")
      )
    )
  )

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

        index-date (c/to-long (f/parse
                                mydateutils/date-formatter-date-hour-min
                                (str date "T" start-time)))
        end-date (c/to-long (f/parse
                              mydateutils/date-formatter-date-hour-min
                              (str date "T" end-time)))
        time-length-miliseconds (mydateutils/find-time-length-delta
                                  index-date end-date end-time)
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


(defn process-times-into-storage
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


;  "Take input time json strings and write to dynamo db or fail.
;  The input is an array of strings. The output should be helpful to say
;  which dicts were successfully used and show useful errors for which werent"
(deflambdafn one.core.taketime
  [in out context]
  (log/info "Starting Lambda")
  (let [body (-> in io/reader (json/parse-stream keyword))
        result (process-times-into-storage (body :data))]
    (with-open [w (io/writer out)]
      (json/generate-stream result w)
      (log/info "Lambda finished")
      )
    )
  )


(defn combine-summary-keywords
  [keywords]
  (apply str  (map (fn [x] (str x)) keywords))
  )


(defn make-summary-index
  "A summary entry is uniquely identified by, the range
  that it describes and the keywords used to do the summarizing.
  
  There may need to be a preservation of the original ordering 
  of those keywords:
 
  core-category - project-identifier - sub-category 

  Also, do we want the index to contain the summary length, to make it easier?
  
  "
  [start-date end-date keywords]
  (str start-date "." end-date "."
       (combine-summary-keywords keywords)
       )
  )


(defn summarize-data-per-keys
  [time-data start-date end-date keywords]
  
  (
   map (
        fn
        [[k vs]]

        ; Add (+) up all the time-length's per each group.
        (merge
          {:time-length (apply + (map (fn [x] (x :time-length)) vs))}

          ; New index.
          {:index  (make-summary-index start-date end-date
                                       ; core-category - project-identifier - sub-category 
                                       [
                                         (:core-category k "")
                                         (:project-identifier k "")
                                         (:sub-category k "")
                                            ]
                                       )}

          ; Type of summary.
          {:type (combine-summary-keywords keywords)}

          ; Time range. This will be important for querying.
          {:start-date start-date :end-date end-date}

          ; Specific values of those keywords representing this summary.
          k
          )
        )
   (group-by
     #(select-keys % keywords)
     time-data))
  )

; :core-category :project-identifier


; Every end of day: run summarize-time for that day, to see how that prior day was used 
; Every end of week: run summarize-time fpr that week, , to look at prior week
; Every end of month: run summarize-time fpr that month, to look at how that prior month was used.
; 


(defn summarize-time
  "take a time start and stop (range) and read what is there,
  and find, for all of the project and subcategory combinations,
  the time length aggregations."
  [start-date end-date]
  (let [
        query-times ""
        time-data (db/get-times-for-date-range start-date end-date)
        all-times-lengths (map (fn [x] (x :time-length)) time-data)
        all-sum (reduce + all-times-lengths)

        ;project-identifiers (map (fn [x] (x :project-identifier)) time-data)
        ; core-categories (set (map (fn [x] (x :core-category)) time-data))

        ; DateRange - CoreCategory - TimeLengthSum
        summary-core-categories (summarize-data-per-keys
                                  time-data
                                  start-date end-date
                                  [:core-category])

        ; (map (fn [y] {:core-category y :time-length (reduce + (map (fn [z] (z :time-length)) (filter (fn [x] (= (x :core-category) y) ) time-data)))}) core-categories)


        ; DateRange - CoreCategory - ProjectIdentifier - TimeLengthSum
        summaries-core-cat-and-projects (summarize-data-per-keys
                                          time-data 
                                          start-date end-date
                                          [:core-category :project-identifier])

        ; DateRange - CoreCategory - ProjectIdentifier - SubCategory - TimeLengthSum
        summaries-core-cat-projects-sub-cat (summarize-data-per-keys
                                              time-data 
                                              start-date end-date
                                              [:core-category :project-identifier
                                               :sub-category])

        ; DateRange - CoreCategory - SubCategory - TimeLengthSum
        ; SummaryType ( day, week, month, year)


        ; do..
        summaries (concat summary-core-categories
                          summaries-core-cat-and-projects
                          summaries-core-cat-projects-sub-cat
                          )


        ; add an entry to projects for any new projects?
        ;   maybe not necessary, since the summary table will show the data.
        ;   or actually: thats written to at a different time, to describe projects,
        ;   per project identifier.
        ]
    summaries
    )
  )


(defn summarize-time-and-write
  "take a time start and stop (range) and read what is there,
  and write to the summary table for all of the project
  and subcategory combinations "
  [start-date end-date]
  (let [
        summaries (summarize-time start-date end-date)
        _ (log/info (str "summaries len: " (count summaries)
                         ", and first is: " (first summaries)))
        ]

    ; Finally, write the summaries to the summaries table.
    ; May need to deal with how to over-write? Is over-writing free?
    (db/batch-write-summaries summaries)

    )
  )


(deflambdafn one.core.summarize-time-lambda
  [in out context]
  (log/info "Starting Lambda")
  (let [body (-> in io/reader (json/parse-stream keyword))
        result (summarize-time-and-write (body :start-date) (body :end-date))]
    (with-open [w (io/writer out)]
      (json/generate-stream result w)
      (log/info "Lambda finished")
      )
    )
  )

(defn foo
  [a b]
  (log/info "blah 1")
  (log/info "blah 2")
  (let [
        c (+ a b)
        d (log/info "blah 4a")
        d (log/info "blah 4b")
        e (log/info "blah 5, d:" d)
        ]
    ;
    (log/info (str "blah 6, and c is " c))
    (log/info "blah 7")
    (str "foo" c)
          )
  )


(defn do-this-week-summary
  [date-range]
  (if (not (= nil date-range))
    (let [
          [start end] date-range
          result (summarize-time-and-write start end)
          ]
      result
      )
    )
  )


(deflambdafn one.core.summarize-today-lambda
  [in out context]
  (log/info "Starting Lambda")
  (let [
        today-var (t/today-at 12 00)
        _ (log/info (str "today: " today-var))

        this-week-date-range (mydateutils/get-this-week-date-range today-var)
        _ (log/info (str "this-week-date-range: " this-week-date-range))

        yesterday (mydateutils/get-yesterday today-var)
        _ (log/info (str "yesterday: " yesterday))
        result-daily (summarize-time-and-write yesterday yesterday)

        result-this-week (do-this-week-summary this-week-date-range)

        result {:result-daily result-daily :result-this-week result-this-week}
        ]
    (with-open [w (io/writer out)]
      (json/generate-stream result w)
      (log/info "Lambda finished")
      )
    )
  )


(deflambdafn one.core.get-summary-data
  [in out context]
  (log/info "Starting Lambda")
  (let [body (-> in io/reader (json/parse-stream keyword))
        period (body :period)
        start-date-str (body :start-date)
        end-date-str (body :end-date)
        summary-type (body :summary-type)
        core-category (body :core-category)

        _ (log/info (str "input body: " body))

        ; if period is "daily"
        ; then run get-summaries for each day from start to end...
        ; and combine that output.
        result (if (= period "daily")

                 ; summaries
                 (apply concat (map (fn [x] (db/get-summaries x x
                                       summary-type  core-category
                                                )
                        )
                      (mydateutils/get-dates-in-range start-date-str end-date-str)
                      ))

                 ; else
                 ; else if period is nil...
                 (db/get-summaries start-date-str end-date-str
                                   summary-type  core-category)
                 
                 )

        ]
    (with-open [w (io/writer out)]
      (json/generate-stream result w)
      (log/info "Lambda finished")
      )
    )
  )


