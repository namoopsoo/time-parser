
### What
A simple time tracker lambda based service, built as a project to learn clojure and micro services. The initial inspiration came from reading the book [168 Hours](https://lauravanderkam.com/books/168-hours/), in which Laura Vanderkam asks us to really see what we are doing with our time. There are 168 hours in a week and her claim is that although we feel we never have enough time, journaling what we do is a means to bullshit test ourselves.

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

#### Output example

* Here is an example of an output stacked area chart of a `core-category:work` query, which is using the author's data from December of 2017. This uses the d3js so called _stacked_ layout. The _y-axis_ represents minutes.
![image](doc/images/example-stacked-2018-02-11.png)
* This particular layout only allows for a max of `20` layers before colors are re-used for other categories.
* [a lot can be said](http://leebyron.com/streamgraph/stackedgraphs_byron_wattenberg.pdf) about stacked area chart aesthetics.


