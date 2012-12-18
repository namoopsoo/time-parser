#!/usr/bin/python

import columnify
import sys
import lines_validate


# delta, catgry = columnify.parseLineWTime(line2.split()) 
# print "below line has length " , delta , " for category  " , catgry
# print line2

# sys.exit()
# columnify.parseDay()

timesheet = "/Users/michal/Documents/timesheet parser/timesheet data/dec.1.2012.txt"

lines_validate.validate_lines(timesheet)

columnify.parse_timesheet(timesheet)


