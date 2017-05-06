(ns one.date-utils
  (:require
    [clj-time.core :as t]
    [clj-time.format :as f]
    ))



; func for getting this week date range
; - if today is Monday, should return something like nil, because nothing to do.

(defn date-to-string
  [date]
  (let [
        ;
        date-formatter (f/formatters :year-month-day)
        date-string (f/unparse date-formatter date)
        ]
    date-string
    )
  )

(defn get-this-week-date-range
  [todays-date]
  (let [
        ; formatters
        day-of-week-num (f/formatter "e")
        day-of-week-word (f/formatter "E")

        ; date vars
        ; todays-date (t/today-at 12 00)
        today-day-of-week-word (f/unparse day-of-week-word todays-date) 
        today-day-of-week-num (Integer/parseInt
                                (f/unparse day-of-week-num todays-date))

        ; today is Monday
        today-is-monday (= today-day-of-week-word "Mon")
        today-is-sunday (= today-day-of-week-word "Sun")
        day-delta (if today-is-sunday
                    ; peg this at 6.
                    6
                    ; otherwise, delta is today num minus 1.
                    (- today-day-of-week-num 1)
                    )

        last-monday (t/minus todays-date (t/days day-delta))
        last-monday-string (date-to-string last-monday)

        yesterday (t/minus todays-date (t/days 1))
        yesterday-string (date-to-string yesterday)
        ]

    (if today-is-monday
      nil
      [last-monday-string yesterday-string]
      )
    )
  )



