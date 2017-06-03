

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


(cljtest/deftest test-get-yesterday
  (cljtest/is (= (mydateutils/get-yesterday (t/date-time 1986 10 14))
                 "1986-10-13"
                 ))

  (cljtest/is (= (mydateutils/get-yesterday (t/date-time 2015 1 1))
                 "2014-12-31"
                 ))

  (cljtest/is (= (mydateutils/get-yesterday (t/date-time 2015 2 1))
                 "2015-01-31"
                 ))
  )


(cljtest/deftest test-get-dates-in-range
  (cljtest/is (= (mydateutils/get-dates-in-range "2017-05-31" "2017-06-20")
                 ["2017-05-31" "2017-06-01" "2017-06-02" "2017-06-03" "2017-06-04" "2017-06-05" "2017-06-06" "2017-06-07" "2017-06-08" "2017-06-09" "2017-06-10" "2017-06-11" "2017-06-12" "2017-06-13" "2017-06-14" "2017-06-15" "2017-06-16" "2017-06-17" "2017-06-18" "2017-06-19" "2017-06-20"]
                 ))

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

