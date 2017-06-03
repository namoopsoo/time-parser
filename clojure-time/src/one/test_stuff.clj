

(ns one.test-stuff
  (:require
    [one.date-utils :as mydateutils]
    [clojure.test :as cljtest]
    [clj-time.core :as t]
    [clj-time.coerce :as c]
    [clj-time.format :as f]
    )
  )

; boot.user=> (use '[clojure.test :as cljtest])
; boot.user=> (cljtest/run-tests 'one.test-stuff)
;
; then make changes , and reload, 
; boot.user=> (use 'one.test-stuff :reload)
;


(cljtest/deftest addition
  (cljtest/is (= 4 (+ 2 2)))
  (cljtest/is (= 7 (+ 3 4))))

(cljtest/deftest subtraction
  (cljtest/is (= 1 (- 4 3)))
  (cljtest/is (= 3 (- 7 4))))

(cljtest/deftest test-this-week-date-range
  (cljtest/is (= (mydateutils/get-this-week-date-range (t/date-time 1986 10 14))
                 ["1986-10-13" "1986-10-13"]))
  (cljtest/is (= (mydateutils/get-this-week-date-range (t/date-time 2000 02 14))
                 nil) "an input of a Monday returns nil")
  (cljtest/is (= (mydateutils/get-this-week-date-range (t/date-time 2000 02 13))
                 ["2000-02-07" "2000-02-12"]))
  )


(defn make-date-milliseconds
  [date the-time]
  (c/to-long (f/parse
               mydateutils/date-formatter-date-hour-min
               (str date "T" the-time)))
  
  )


(cljtest/deftest test-find-time-length-delta
  (let [
        date "2017-05-01"
        start-time "23:00"
        end-time "00:00" 
        start-date (make-date-milliseconds date start-time)
        end-date (make-date-milliseconds date end-time)
        ](cljtest/is (= 3600000
                        (mydateutils/find-time-length-delta
                          start-date end-date end-time))
          
          )
    )

  (let [
        date "2017-05-01"
        start-time "23:00"
        end-time "23:01" 
        start-date (make-date-milliseconds date start-time)
        end-date (make-date-milliseconds date end-time)
        ](cljtest/is (= 60000
                        (mydateutils/find-time-length-delta
                          start-date end-date end-time))
          )
    )

  (let [
        date "2017-05-01"
        start-time "00:00"
        end-time "01:02" 
        start-date (make-date-milliseconds date start-time)
        end-date (make-date-milliseconds date end-time)
        ](cljtest/is (= 3720000
                        (mydateutils/find-time-length-delta
                          start-date end-date end-time))
          )
    )

  (let [
        date "2017-05-01"
        start-time "00:00"
        end-time "00:02" 
        start-date (make-date-milliseconds date start-time)
        end-date (make-date-milliseconds date end-time)
        ](cljtest/is (= 120000
                        (mydateutils/find-time-length-delta
                          start-date end-date end-time))
          )
    )
  )

