#!/usr/bin/python

import columnify
import lines_validate
import pdb
import sys

# delta, catgry = columnify.parseLineWTime(line2.split()) 
# print "below line has length " , delta , " for category  " , catgry
# print line2

# sys.exit()
# columnify.parseDay()



# Previous nvalt storage: 
# 'timesheet' : "/Volumes/untitled/Notational Data/times sep-2-2013.rtf" ,

parameters =  { 
	'timesheet' : "timesheet data/may.2.2014.txt",
	'month' : 5 ,
	'start_day' : 16,
	'end_day' : 31 
}

lines_validate.validate_lines(parameters['timesheet'])

columnify.parse_timesheet(parameters)

