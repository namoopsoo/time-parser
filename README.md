
### What
A simple time tracker lambda based service, built as a project to learn clojure and micro services. The initial inspiration came from reading the book [168 Hours](https://link-to-168-hours-book), in which Author Author asks us to really see what we are doing with our time. There are 168 hours in a week and her claim is that although we feel we never have enough time, journaling what we do is a means to bullshit test ourselves.

#### Input data
* We split our time between `personal` and `work` I think. 
* And furthermore we bring our attention to either specific *Projects*, which have a beginning and an end, such as the below `taxes-2017`--_hopefully one day I will finish my taxes_--and `adhoc` tasks which is meant to record time spent helping a colleague for example (here called `consulting`, below). 
* `routine` is a potentially useful or not distinction of things we do which do not have an *End*.
```
times 2018-02-10

10:00 - 10:30 ; personal ; taxes-2017 ; plan ; just starting out
10:30 - 11:05 ; personal ; routine ; yoga
11:35 - 12:20 ; work ; WRK-345-bugfix-foo ; code
12:20 - 13:05 ; work ; adhoc ; consulting 
13:05 - 13:35 ;  work ; WRK-345-bugfix-foo ; code
```

