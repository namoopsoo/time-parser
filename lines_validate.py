import re

def validate_lines(filename):
	
	with open (filename) as o:
	
		hour_line_re = re.compile ( r'^\d\d:\d\d - \d\d:\d\d ')
		date_re = re.compile(r'^\d\d\/\d\d$')
		comment_re = re.compile(r'^#')
		blank_re = re.compile(r'^$')
		
		lines = o.read().split('\n') 
		
		total = len ( lines ) 
		
		count = 0 
		for line in lines:
			if not hour_line_re.match(line) and \
				not date_re.match(line) and \
				not comment_re.match(line) and \
				not blank_re.match(line):
	
				raise Exception, "line %d not good format:\n%s" % (count,line)
			count += 1

