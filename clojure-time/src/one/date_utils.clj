(ns one.date-utils
  (:require
    [clj-time.core :as t]
    [clj-time.format :as f]
    ))


(def date-formatter-date-hour-min (f/formatters :date-hour-minute))


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



(defn get-yesterday
  [todays-date]
  (let [
        yesterday (t/minus todays-date (t/days 1))
        yesterday-string (date-to-string yesterday)

        ]
    yesterday-string)
  )


(defn find-time-length-delta
  "
  There are 60*24 = 1440 minutes in a day, so if end-time is 00:00,
  then delta is ...

  How many milliseconds in a day:
  boot.user=> (* 60000 60 24)
  86400000
  "
  [start-date end-date end-time]
  (if (= end-time "00:00")
    (- 86400000 (- start-date end-date))
    (- end-date start-date))
  )


(defn get-dates-in-range
  [start-date-str end-date-str]
  (let [
        start-date (f/parse date-formatter-date-hour-min
                                (str start-date-str "T" "12:00"))
        end-date (f/parse date-formatter-date-hour-min
                                (str end-date-str "T" "12:00"))
        delta (t/in-days (t/interval start-date end-date))

        date-range (map (fn [x] (t/plus start-date (t/days x))) (range 0 (+ delta 1)))
        date-range-str (map (fn [x] (date-to-string x)) date-range)

        ]
    date-range-str
    )

  ; take date-str into parsed ...

  ;(f/parse one.date-utils/date-formatter-date-hour-min (str start-date-str "T" "12:00"))


  )


