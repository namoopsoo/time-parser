

/* Set the options for our chart */
var options = { segmentShowStroke : false,
								animateScale: true,
								percentageInnerCutout : 50,
                showToolTips: true,
                tooltipEvents: ["mousemove", "touchstart", "touchmove"],
                tooltipFontColor: "#fff",
								animationEasing : 'easeOutCirc'
              }

randomColor = function() {
	min = 1; max = 255;
	return Math.floor(Math.random() * (max - min + 1)) + min
}

randomRGB = function() {
	r = randomColor();
	g = randomColor();
	b = randomColor();
	return 'rgba(' + r.toString() + ', ' + g.toString() + ', ' + b.toString() + ', 0.2)';
}


updateDoughnut = function(chart_id, response) {

	console.log(response);
	/* Create the context for applying the chart to the HTML canvas */
	var ctx = $(chart_id).get(0).getContext("2d");

	/* Set the inital data */
	var data = [];

	for (var i = 0; i < response.length; i++) {
		data.push({
					value: response[i]['time-length'],
					// color: "#3498db",
					color: randomRGB(), // 'rgba(255, 99, 132, 0.2)',
					// highlight: "#2980b9",
					label: response[i]['project-identifier']
				})
	}
	console.log('data for doughnut: ');
	console.log(data);

	// 
	graph = new Chart(ctx).Doughnut(data, options);

}

formatDateString = function(d) {
	var dd = d.getDate();
	var mm = d.getMonth()+1; //January is 0!
	var yyyy = d.getFullYear();

	if(dd<10) {
		dd='0'+dd
	} 

	if(mm<10) {
		mm='0'+mm
	} 

	return yyyy + '-' + mm + '-' + dd;
}

copyDate = function(d) {
	x = new Date(JSON.parse(JSON.stringify(d)));
	return x
}

dateMinusDelta = function(d, delta) {
	x = copyDate(d);
	x.setDate(x.getDate() - delta);
	return x;
}

changeDateDay = function(d, new_day) {
	x = copyDate(d);
	x.setDate(new_day);
	return x;
}

getThisWeekDateRange = function(today) {
	// Yesterday
	yesterday = dateMinusDelta(today, 1);
	yesterday_string = formatDateString(yesterday);
	
	// last monday..
	// today.getDay()
	if (today.getDay() == 1) { // if today is Monday, nothing to do.
		return;
	} else if (today.getDay() == 0) {
		var delta = 6;
	} else {
		var delta = today.getDay() - 1;
	}

	last_monday = dateMinusDelta(today, delta);
	last_monday_string = formatDateString(last_monday);

	return [last_monday_string, yesterday_string];
}

getLastWeekDateRange = function(today) {
	// Get prior Sunday.
	if (today.getDay() == 0) { // Sunday
		var delta = 7;
	} else {
		var delta = today.getDay();
	}
	last_sunday = dateMinusDelta(today, delta);
	last_sunday_string = formatDateString(last_sunday);

	prior_monday = dateMinusDelta(last_sunday, 6);
	prior_monday_string = formatDateString(prior_monday);

	return [prior_monday_string, last_sunday_string];
}

getThisMonthDateRange = function(today) {
	// If the 1st, then nothing to do.
	if (today.getDate() == 1) {
		return;}
   
	yesterday = dateMinusDelta(today, 1);
	start_of_month = changeDateDay(today, 1);
	return [
		formatDateString(start_of_month),
		formatDateString(yesterday)];
}


getLastMonthDateRange = function(today) {
	last_month_end = dateMinusDelta(
			changeDateDay(today, 1),
			1);
	last_month_start = changeDateDay(last_month_end, 1);
	return [
		formatDateString(last_month_start),
		formatDateString(last_month_end)];
}

function assert(condition, message) {
    if (!condition) {
        throw message || "Assertion failed";
    }
}

assertRangesMatch = function(d, out, want) {
	assert(((out[0] == want[0]) && (out[1] == want[1])),
			'For ' + formatDateString(d) + ' got ' + out + '  But want ' + want);
}

testGetThisWeekDateRange = function() {
	// 2017-03-17
	var d = new Date(2017, 2, 17);
	var out = getThisWeekDateRange(d);
	var want = ["2017-03-13", "2017-03-16"];
	assertRangesMatch(d, out, want);

	// 2017-03-12 
	var d = new Date(2017, 2, 12);  
	var out = getThisWeekDateRange(d);
	var want = ["2017-03-06", "2017-03-11"];
	assertRangesMatch(d, out, want);

	// 2017-01-01 
	var d = new Date(2017, 0, 1);  
	var want = ["2016-12-26", "2016-12-31"];
	var out = getThisWeekDateRange(d);
}

updateThisWeekDoughnut = function() {
	// Is today Monday? 
	var today = new Date();
	// If so, doughnut should be like a blurry placeholder gif.
	// Because this week not ready on Mondays.
	if (today.getDay() == 1) {
		console.log('Today is Monday so this week is empty so far.');
		return;
	} else {
		var out = getThisWeekDateRange(today);
		last_monday_string = out[0];
		yesterday_string = out[1];
	}
	parameters = {
		// ?end-date=2017-01-03&start-date=2017-01-01&summary-type=%3Acore-category
		'end-date': yesterday_string, // 'end-date': '2017-01-03',
		'start-date': last_monday_string, // 'start-date': '2017-01-01',
		'summary-type': // ':core-category'
			':core-category:project-identifier'
	}

	response = querySummaryWithParams(parameters, "#graph_this_week");

}

updateLastWeekDoughnut = function() {
	var today = new Date();

	var out = getLastWeekDateRange(today);
	start_string = out[0];
	end_string = out[1];

	parameters = {
		'end-date': end_string,
		'start-date': start_string,
		'summary-type':
			':core-category:project-identifier'
	}

	response = querySummaryWithParams(parameters, "#graph_last_week");
}

updateThisMonthDoughnut = function() {
	var today = new Date();

	var out = getThisMonthDateRange(today);
	start_string = out[0];
	end_string = out[1];

	parameters = {
		'end-date': end_string,
		'start-date': start_string,
		'summary-type':
			':core-category:project-identifier'
	}

	response = querySummaryWithParams(parameters, "#graph_this_month");
}

updateLastMonthDoughnut = function() {
	var today = new Date();

	var out = getLastMonthDateRange(today);
	start_string = out[0];
	end_string = out[1];

	parameters = {
		'end-date': end_string,
		'start-date': start_string,
		'summary-type':
			':core-category:project-identifier'
	}

	response = querySummaryWithParams(parameters, "#graph_last_month");
	// console.log('response: "' + response + '"');

}


querySummaryWithParams = function(parameters, chart_id) {
	// Create a new signer
	var config = {
		region: 'us-east-1',
		service: 'execute-api',
		// AWS IAM credentials, here some temporary credentials
		accessKeyId: 'AKIAJZRBX47EKWFJQ5UA',
		secretAccessKey: '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'
	};
	console.log("config: ");
	console.log(config);
	var signer = new awsSignWeb.AwsSigner(config);

	// Make request url
	var base_url = 'https://m8fe2knl2f.execute-api.us-east-1.amazonaws.com/staging/summary';
	var full_uri = base_url + '?end-date=' + parameters['end-date'] + '&start-date=' + parameters['start-date'] + '&summary-type=%3Acore-category%3Aproject-identifier';


	// var full_uri = base_url + '?end-date=2017-01-03&start-date=2017-01-01&summary-type=%3Acore-category';



	// Sign a request
	var request = {
		method: 'GET',
		// URL w/o querystring here.
		url: base_url,
		headers: {},
		params: parameters,
		data: null
	};
	var signed = signer.sign(request);

	// Make Request.
	$.get({
		url: full_uri,
		headers: {
			'Authorization': signed.Authorization,
			'Accept': signed.Accept,
			'x-amz-date': signed['x-amz-date'],
		},

		success: function( response ) {
			console.log('success'); // server response
			console.log(response); // server response

			updateDoughnut(chart_id, response);
			// return response;

		},
		error: function( response ) {
			console.log( 'error: ' + response ); // server response
		}
	});


}

// (function() { })();
	/*
	<script src="js/core.js">
	<script src="js/sha256.js">
	<script src="js/hmac-sha256.js">
	<script src="js/aws-sign-web.js">  */



updateThisWeekDoughnut();
updateLastWeekDoughnut();
updateThisMonthDoughnut();
updateLastMonthDoughnut();


