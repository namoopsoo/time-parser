

(ns one.test-stuff
  (:require
    [one.date-utils :as mydateutils]
    [clojure.test :as cljtest]
    [clj-time.core :as t]
    )
  )

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

