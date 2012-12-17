#!/usr/bin/python

import columnify
import sys


# line = "00:00 - 02:00 unk unknown"
#  
# line2= "23:10 - 00:00 fun Photoshop at starbucks"
# delta, catgry = columnify.parseLineWTime(line2.split()) 
# print "below line has length " , delta , " for category  " , catgry
# print line2

# sys.exit()
# columnify.parseDay()

timesheet = "/Users/michal/Documents/timesheet parser/timesheet data/dec.1.2012.txt"

columnify.parse_timesheet(timesheet)


