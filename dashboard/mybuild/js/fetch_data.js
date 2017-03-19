

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


updateDoughnut = function(response) {

	console.log(response);
	/* Create the context for applying the chart to the HTML canvas */
	var ctx = $("#graph").get(0).getContext("2d");

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


updateChartWithTimeData = function(parameters) {
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
	var full_uri = base_url + '?end-date=2017-01-03&start-date=2017-01-01&summary-type=%3Acore-category%3Aproject-identifier';
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
			console.log( response ); // server response

			updateDoughnut(response);
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



parameters = {
	// ?end-date=2017-01-03&start-date=2017-01-01&summary-type=%3Acore-category
	'end-date': '2017-01-03',
	'start-date': '2017-01-01',
	'summary-type': // ':core-category'
		':core-category:project-identifier'
}

updateChartWithTimeData(parameters);



