

/* Set the options for our chart */
var options = { segmentShowStroke : false,
								animateScale: true,
								percentageInnerCutout : 50,
                showToolTips: true,
                tooltipEvents: ["mousemove", "touchstart", "touchmove"],
                tooltipFontColor: "#fff",
								animationEasing : 'easeOutCirc'
              }


/* Set the inital data */
var data = [
  {
      value: 1,
      color:"#e74c3c",
      highlight: "#c0392b",
      label: "Red"
  },
  {
      value: 1,
      color: "#2ecc71",
      highlight: "#27ae60",
      label: "Green"
  },
  {
      value: 1,
      color: "#3498db",
      highlight: "#2980b9",
      label: "Blue"
  }
];

/* Create the context for applying the chart to the HTML canvas */
var ctx = $("#graph").get(0).getContext("2d");


graph = new Chart(ctx).Doughnut(data, options);


