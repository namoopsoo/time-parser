#!/usr/bin/python

import settings

import pdb
import sys
import math
import re
from datetime import timedelta
from time import gmtime, strftime, localtime, mktime
from collections import OrderedDict


class Columnify ():

	

	def __init__ (self, cols=[],filename  ="" ):

		self.d_init = {"unk":0, "cor":0, "corbiz":0, "corover":0, "sup":0, "fun":0, "prj":0,"gym":0, "red":0, "fin":0, "pgm":0, "hol":0, "vac":0, "coruserollover":0, "corsick":0}


		if filename   != "":
			self.refColsFromFile ( filename  ) 

			self.datasets = {}
			return

		if cols != []:
			self.ref_columns = cols

	# take list of words , output column data
	# column data says what columns match per input list
	def organizeWords (self, line=[] ):

		out_str = ""
		
		if line == []:
			out_str = "\t"*len( self.ref_columns )
			return out_str
	
		for w in self.ref_columns :

			true = False
			for x in line:
				if x == w :
					true = True
					break

			if true == True :
				out_str = out_str + "y\t"
			else:
				out_str = out_str + "n\t"

		return out_str
	def classifyData ( self):
		pass

	def makeTabData (self,datatype=None, dset_name=None ):

		if dset_name == None:
			return
		try:
			self.datasets[dset_name]	
		except KeyError:
			return

		k=1

		out_str = "category\t"*k

		if self.datasets[dset_name]	== []: 
			return

		# line 1 should be col names
		for word in self.ref_columns :
			out_str +=  word + "\t"

		out_str += "\n" + "d\t"*(len(self.ref_columns) + k) + "\n"  # line 2
		out_str += "class"*k + "\t"*(k + len(self.ref_columns)) + "\n" # line 3

		for line in self.datasets[dset_name]	: 
			if line != "":
				line_parts = line.split(None, k)

				category = line_parts [0]
				remainder = line_parts [k]
	
				out_str +=  category*k + "\t"*k + self.organizeWords 	( remainder.split() ) + "\n"
		return out_str

	# oobtain the col reference from a file   of sorted words
	# apple
	# betty 
	# creppe 
	# manage 
	# zeppy 
	# 
	def refColsFromFile ( self , filename ) :

		try:
			f = open ( filename   ) 
		except IOError:
			return		

		self.ref_columns =  f.read().split("\n")
		
		# cut off last 'empty' element, which is there for some reason 
		self.ref_columns = self.ref_columns [ : ( len( self.ref_columns ) -1 ) ]

	# read a timesheet data file
	def readDataFile (self,filename  = "",datatype=None,  dset_name=None  ):
		if dset_name == None:
			return
		try:
			f = open ( filename  )
		except IOError:
			return		

		self.datasets[dset_name]	= 	f.read().split("\n")

def _reset_totals(totals):
	for elem in totals:
		totals[elem] = 0

def retrieveWord ( orange_data , line_num ) :
	line = ""

	for i in range(len(orange_data.domain.attributes)):
		if orange_data[ line_num ][i].value == 'y':

			line += orange_data.domain.attributes[i].name + " "

	return line

def testClassifier ( classifier , orange_data ) :
	for i in range(len(orange_data)):
		c = classifier ( orange_data[i] )
		print retrieveWord ( orange_data , i )   
		print "		, classified as " , c , " ( " , orange_data[i].getclass() , " )"
		print ""

# data1 = orange.ExampleTable( "work")
#  data2 = orange.ExampleTable( "work-test") 
#  classifier = orange.BayesLearner (data1)


# prepare timesheet file for training
def stripTimeFromFile(filename, filename_new) :
	try:
		fin = open ( filename ) 
		fout = open ( filename_new ,"w") 
	except IOError:
		print "prob w files"
		return

	for line in  fin.readlines() : 
		fout.write ( line.split(' ',3)[3] ) 



def parseLineWTime ( tupl ): 
	"""Take in one timesheet line and output delta hours and category

		Use the following category abbreviations
		cor : cortix work
		corbiz : cortix business development hours
		, "corover":0 overtime
		, "coruserollover":0
		corsick : cor sick time
		fun : fun , e.g. with friends, entertainment
		prj : other projects besides cortix
		sup : supportive , e.g. sleep, cleaning, transport
		fin : financials, paying bills etc,
		gym : any working out, biking
		red : reading 
		unk : unknown , unaccounted for , will be assigned to supportive

	"""

	try:
		h1,m1 = tupl[0].split(":")
		h2,m2 = tupl[2].split(":")
		activity = tupl[3] 	
		notes = tupl[4]
	except IndexError:
		return 0.0, "unk", []

	t1 = timedelta(hours=int(h1), minutes=int(m1))
	t2 = timedelta(hours=int(h2), minutes=int(m2))

	delta = t2 - t1

	return delta.seconds / (3600.0) , activity, notes

def displayHours (hours):
	return "{%s}\n" % \
		reduce(lambda x, y: x + y, 
				["%s: %.2f, " % (elem, hours[elem]) for elem in hours] 
		)

def aggregHours (a ,b):
	for elem in b:
		a[elem] += b[elem]


def checkTwentyFour (hours):
	total=0.0
	for elem in hours:
		total += hours[elem]
	return total

def weekday(mo,day):
	b = (settings.YEAR,int(mo),int(day),00,00,00,0,0,0)
	d = localtime(mktime(b))
	return d.tm_wday

def round_quarter(time):
	"""Rounds to nearest quarter hour.

	7.33 rounds to 7.25
	"""

	x = time - math.floor(time)	

	b = math.floor( (x-.0001)/.25) * .25
	d = x/.25
	if math.ceil(d) - d < .5:
		return b + math.floor(time) +.25
	return math.floor(time) + b

def round_difference (original, rounded):
	"""Returns categorization of a rounding result."""

	delta = original - rounded
	if  original - rounded > 0:
		return ("fewer",delta)
	else:
		return ("more",-delta)


def filter_line(line):
	for seq in settings.GARBAGE_PREPENDING_SEQUENCES:
		try:
			return re.search(seq, line).group(1)
		except AttributeError:
			pass
	else:
		return line

def generate_output_keys(month, start_day, end_day):

	keys_ = []
	for day in range(int(start_day), int(end_day) + 1):
		keys_.append("%02d/%02d" % (int(month), day))
	return keys_

def parse_timesheet (parameters):
	'''Take a timesheet file and spit out a report.

	Produces project hours breakdown per day and weekly aggregation. 
	Also creates a report with work comments per day.
	'''

	hours = {"unk":0, "cor":0, "corbiz":0, "corover":0,  "sup":0, "fun":0, "prj":0,"gym":0, "red":0, "fin":0, "pgm":0, "hol":0, "vac":0, "coruserollover":0, "corsick":0}
	total_hours = {"unk":0, "cor":0, "corbiz":0, "corover":0,  "sup":0, "fun":0, "prj":0,"gym":0, "red":0, "fin":0, "pgm":0, "hol":0, "vac":0, "coruserollover":0, "corsick":0}
	week_total = {"unk":0, "cor":0, "corbiz":0, "corover":0,  "sup":0, "fun":0, "prj":0,"gym":0, "red":0, "fin":0, "pgm":0, "hol":0, "vac":0, "coruserollover":0, "corsick":0}
	rounding_totals = {"more":0, "fewer":0}
	date_re = re.compile(r"([0-9][0-9]/[0-9][0-9]).*")
	hours_re = re.compile(r"[0-9][0-9]:[0-9][0-9]")
	cor_re = re.compile(r"cor")
	timesh_re = re.compile(r"(timesheet|time sheet)")	
	comment_re = re.compile(r"^#")	
	business_days_count = 0
	cortix_output = ''
	vac_output = ''
	userollover_output = ''
	cortix_hours = OrderedDict() ; cortix_hours_file = settings.CORTIX_REPORT_FILE + strftime("%m%d%y_%H%M.csv")
	corbiz_hours = OrderedDict()
	vac_hours = OrderedDict() 
	sick_hours = OrderedDict() 

	use_rollover_cortix_hours = OrderedDict()
 
	today = int(strftime("%d"))



	try:
		f = open(parameters['timesheet'])
	except IOError:
		sys.exit( "can't open " + parameters['timesheet'] ) 

	r = open(settings.REPORT_FILE + strftime("%m%d%y_%H%M.txt"), "w")
	r_clean = open(settings.REPORT_FILE_CLEAN  + strftime("%m%d%y_%H%M.txt"), "w")
	didntparse_file = settings.DIDNTPARSE_FILE + strftime("%m%d%y_%H%M.txt")
	didntparse = open(didntparse_file, "w")

 

 

	
	lines = f.readlines()
	batch = ""; mo=""; day=""; n_mo=""; n_day="" ; notes =""

	# should filter out character sequences from lines so that data lines can still be used 
	lines = map(lambda x:filter_line(x), lines)


	for line in lines:


		# perform a filter to remove some char sequences appearing in front of date and hour lines



		if hours_re.match (line) :
			delta, activity, notes = parseLineWTime (line.split(' ',4))
		
			# keep track of what is getting rounded 
			round_how, round_diff = round_difference (delta, round_quarter (delta))
			rounding_totals [round_how] += round_diff

 			try:
 				# hours[activity] += delta
 				hours[activity] += round_quarter (delta)
 			except KeyError:
				print line.split()
 				sys.exit( "key '" + activity + "' not found, delta:'"+str(delta)+"', line: '"+line+"'")

			# Add some data to the cor report file 
			if cor_re.match(activity):
				r.write( str("%.2f" % delta) + "\t" + str(notes) )

				if timesh_re.search(notes) is None: 
					r_clean.write( str(notes) )
			
		elif date_re.match(line):

			n_mo, n_day = date_re.match(line).group(1).split("/"); n_day = str(int(n_day))
			# assert int(n_mo) == int(parameters['month'])
			# print '%s\nfailed with line:%s' % (e,line)

			w = weekday(n_mo,n_day)

			r.write( "\n" + line.strip() +":\n")
			r_clean.write( "\n" + line.strip() +":\n")

			cortix_output += "(%s/%s) %s, " % (mo, day, hours['cor'])
			vac_output += "(%s/%s) %s, " % (mo, day, hours['vac'])
			userollover_output += "(%s/%s) %s, " % (mo, day, hours['coruserollover'])
			try:
				cortix_hours["%02d/%02d"%(int(mo),int(day))] = hours['cor']
			except ValueError:
				pass
			corbiz_hours["%s/%s"%(mo,day)] = hours['corbiz']
			vac_hours["%s/%s"%(mo,day)] = hours['vac']
			sick_hours["%s/%s"%(mo,day)] = hours['corsick']
			use_rollover_cortix_hours["%s/%s"%(mo,day)] = hours['coruserollover']

			if w is 0:  	
				batch +=  mo + "/"+ day  + ": " + displayHours(hours)
				aggregHours(week_total, hours);aggregHours(total_hours, hours);
				cortix_output += "\n"
				vac_output += "\n"
				userollover_output += "\n"

				deficit = "(cor deficit: " + str(40.0 - week_total["cor"]) + "/40 )\n"

				# week_total["unk"] += checkTwentyFour 

				# Be sure when writing output that use consistent values here. Not rounded ?  
				sys.stdout.write(displayHours(week_total) + deficit + batch + "\n")
				mo, day = n_mo, n_day 

				batch = "";_reset_totals(week_total)
				_reset_totals(hours)

				if int(day) <= today:
					business_days_count += 1

			elif w > 0 and w <= 6:  	
				aggregHours(week_total,hours);aggregHours(total_hours,hours);

				batch +=  mo + "/"+ day  + ": " + displayHours(hours)
				mo, day = n_mo, n_day 
				_reset_totals(hours)

				# if Tue, Wed, Thu, Fri
				if w in [1,2,3,4] and int(day) <= today:
					business_days_count += 1


			else:
				sys.exit("date nonmatch: \'" + n_mo + "\', \'" + n_day + "\', w="+str(w))

		elif comment_re.match(line):
			pass
		elif line == "" or re.match(r'\\$',line):
			pass
		else:
			if hours_re.search(line) or date_re.search(line):
				errout = open(settings.ERROR_LOG,"a")
				errout.write( strftime("[%m%d%y_%H%M]") + " Warning: not able to parse: \""+ line + "\"") 
				errout.close()
			didntparse.write(line)
	# display totals...
	print 'month totals:\n' + displayHours(total_hours)
	sum_total = sum([total_hours['cor'], total_hours['corbiz'], total_hours['vac'], total_hours['corsick'], total_hours['hol'], total_hours['coruserollover']])
	sum_total_text = 'sum total: cor(%s) + corbiz(%s) + vac(%s) + sick(%s) + hol(%s) + coruserollover(%s) = %s\n' % (total_hours['cor'], total_hours['corbiz'], total_hours['vac'], total_hours['corsick'], total_hours['hol'], total_hours['coruserollover'], sum_total)
	print sum_total_text

	goal_hours_so_far = business_days_count * 8
	print 'By the end of %s/%s, want to finish about %d hours for this month' \
					% (str(mo), str(today), goal_hours_so_far )
#					% (str(mo), settings.TIMESHEET_PERIOD_END_DAY, goal_hours_so_far )


		
	r.close()
	r_clean.close()
	didntparse.close()

	def real_hour(_h):
		translation = {0:'00', 25:'15', 5: '30', 75:'45'}
		try:
			main, fraction = str(_h).split('.')
		except ValueError:
			main = str(_h) ; fraction = '0'
		
		return '%s:%s' % (main, translation[int(fraction)])
	
	print '(rounding_totals: ', rounding_totals , ' )'

	print 'cortix hours summary:\n%s' % cortix_output 
	#print 'cortix_hours : \n%s' % cortix_hours

	print 'vac hours summary:\n%s' % vac_output 
	print 'cortix using rollover hours summary:\n%s' % userollover_output 


	# Generate expected keys 
	output_keys = generate_output_keys(
		parameters['month'], parameters['start_day'], parameters['end_day']
	)

	#pdb.set_trace()


	# Write out Hours into a CSV file
	with open(cortix_hours_file, 'w') as cortxf:
		cortxf.write('type,' + reduce(lambda x,y: x+','+y, output_keys) + '\n')
		cortxf.write('cor,' + 
				reduce(lambda x,y: str(x)+','+str(y), [
						real_hour(cortix_hours[_hour]) if _hour in cortix_hours
						else "0:00"
						for _hour in output_keys 
				]) + '\n'
		)

		cortxf.write('corbiz,' + reduce(lambda x,y: str(x)+','+str(y), [real_hour(_hour) for _hour in corbiz_hours.values()]) + '\n')

		cortxf.write('vac,' + reduce(lambda x,y: str(x)+','+str(y), [real_hour(_hour) for _hour in vac_hours.values()]) + '\n')
		cortxf.write('sick,' + reduce(lambda x,y: str(x)+','+str(y), [real_hour(_hour) for _hour in sick_hours.values()]) + '\n')
		cortxf.write('use_rollover,' + reduce(lambda x,y: str(x)+','+str(y), [real_hour(_hour) for _hour in use_rollover_cortix_hours.values()]))


	if	int(parameters['start_day']) == 1:
		file_label = '1'
	else:
		file_label = '2'

	summary_file = '%s_%s.%s.%s_edit.%s' % (settings.SUMMARY_FILE, 
								parameters['month'],
								file_label,
								settings.YEAR,
								strftime("%m%d%y_%H%M.txt"))


	print '\n\n\n\n\n**** ~ **** ~ ****\n!!!!!! REMEMBER TO CHECK %s FOR WHAT DID NOT PARSE\nAnd wrote summary to %s.\n\n\n\n\n\n\n:)' \
		% (didntparse_file, summary_file)

	with open(summary_file, "w") as summaryf:

		summaryf.write (
			'By the end of %s/%s, want to finish about %d hours for this month. \
			\nAnd month totals are \n%s\n' \
				% (
					str(mo), 
					str(today), 
#					settings.TIMESHEET_PERIOD_END_DAY, 
					goal_hours_so_far,
					displayHours(total_hours)
				)
		)

		summaryf.write ( 
			sum_total_text + \
			'\n(rounding_totals: %s)\n'% rounding_totals  + \
			'cortix hours summary:\n%s' % cortix_output + \
			'vac hours summary:\n%s' % vac_output + \
			'cortix using rollover hours summary:\n%s' % userollover_output 
		)












