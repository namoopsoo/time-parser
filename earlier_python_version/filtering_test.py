

def filter_line(line, garbage_prepending_sequences):
	for seq in garbage_prepending_sequences:
		try:
			return re.search(seq, line).group(1)
		except AttributeError:
			pass
	else:
		return line


for i in range (len(lines)):
	if lines[i] != filtered_lines[i]:
		print 'from \n%s\nto\n%s' % (lines[i],filtered_lines[i])

for i in range (len(lines)):
	if filtered_lines[i] != filtered_lines_2[i]:
		print 'from \n%s\nto\n%s' % (lines[i],filtered_lines_2[i])



for line in lines :
			if re.search( garbage_prepending_sequences[0], line ).group(1):
				try:
					filtered_lines.append(re.search( garbage_prepending_sequences[0], line ).group(1))
				except AttributeError:
					filtered_lines.append(line)
			elif re.search( garbage_prepending_sequences[1], line ).group(1):
				try:
					filtered_lines.append(re.search( garbage_prepending_sequences[0], line ).group(1))
				except AttributeError:
					filtered_lines.append(line)

			else:
					filtered_lines.append(line)




