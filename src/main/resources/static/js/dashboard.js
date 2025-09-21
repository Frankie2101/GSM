document.addEventListener("DOMContentLoaded", function() {
    /**
     * Register the Chart.js datalabels plugin globally once.
     */
    Chart.register(ChartDataLabels);

    /**
     * Initializes the Donut Chart for the WIP (Work in Progress) tab.
     */
    function initWipChart() {
        const chartCanvas = document.getElementById('wipDonutChart');
        if (!chartCanvas) return;

        const dataElement = document.getElementById('wipChartData');
        if (!dataElement) return;

        // Get data from the hidden script tag.
        const wipData = JSON.parse(dataElement.textContent || '{}');
        const labels = Object.keys(wipData);
        const data = Object.values(wipData);

        const isAllZero = data.every(item => item === 0);
        if (labels.length === 0 || isAllZero) {
            const ctx = chartCanvas.getContext('2d');
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillText("No WIP data to display", chartCanvas.width / 2, chartCanvas.height / 2);
            return;
        }

        // Logic to show "No data" message if data is empty.
        new Chart(chartCanvas.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: ['#4e73df', '#1cc88a', '#36b9cc'],
                }],
            },
            options: {
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    datalabels: {
                        formatter: (value, context) => {
                            const label = context.chart.data.labels[context.dataIndex];
                            return value > 0 ? `${label}\n${value}` : null;
                        },
                        color: '#fff',
                        font: { weight: 'bold' },
                    }
                },
                cutout: '60%',
            },
        });
    }

    /**
     * Initializes all charts for the Performance tab (Throughput and S-Curve).
     */
    function initPerformanceCharts() {
        // Daily Throughput Chart
        const throughputCanvas = document.getElementById('dailyThroughputChart');
        if (throughputCanvas) {
            const dataElement = document.getElementById('throughputChartData');
            const throughputData = JSON.parse(dataElement.textContent || '{}');

            if (!throughputData.labels || throughputData.labels.length === 0) {
                showNoDataMessage(throughputCanvas);
            } else {
                // ... get data and check if empty ...
                new Chart(throughputCanvas.getContext('2d'), {
                    type: 'bar',
                    // ... Chart.js configuration for a grouped bar chart ...
                    data: {
                        labels: throughputData.labels,
                        datasets: [
                            {
                                label: 'Cutting',
                                data: throughputData.cuttingData,
                                backgroundColor: 'rgba(54, 162, 235, 0.8)',
                                borderColor: 'rgba(54, 162, 235, 1)',
                                borderWidth: 1
                            },
                            {
                                label: 'Sewing',
                                data: throughputData.sewingData,
                                backgroundColor: 'rgba(255, 206, 86, 0.8)',
                                borderColor: 'rgba(255, 206, 86, 1)',
                                borderWidth: 1
                            },
                            {
                                label: 'Packing',
                                data: throughputData.packingData,
                                backgroundColor: 'rgba(75, 192, 192, 0.8)',
                                borderColor: 'rgba(75, 192, 192, 1)',
                                borderWidth: 1
                            }
                        ]
                    },
                    options: {
                        scales: {
                            y: {
                                beginAtZero: true,
                                stacked: false
                            },
                            x: {
                                stacked: false
                            }
                        },
                        plugins: {
                            legend: {
                                position: 'top'
                            }
                        }
                    }
                });
            }
        }

        // S-Curve Chart
        const sCurveCanvas = document.getElementById('sCurveChart');
        if (sCurveCanvas) {
            const dataElement = document.getElementById('sCurveChartData');
            const sCurveData = JSON.parse(dataElement.textContent || '{}');

            if (!sCurveData.labels || sCurveData.labels.length === 0) {
                showNoDataMessage(sCurveCanvas);
            } else {
                // ... get data and check if empty ...
                new Chart(sCurveCanvas.getContext('2d'), {
                    type: 'line',
                    data: {
                        labels: sCurveData.labels,
                        datasets: [
                            {
                                label: 'Planned',
                                data: sCurveData.plannedData,
                                borderColor: 'rgba(28, 200, 138, 1)',
                                backgroundColor: 'rgba(28, 200, 138, 0.1)',
                                fill: false,
                                tension: 0.1,
                                pointRadius: 1
                            },
                            {
                                label: 'Actual',
                                data: sCurveData.actualData,
                                borderColor: 'rgba(78, 115, 223, 1)',
                                backgroundColor: 'rgba(78, 115, 223, 0.1)',
                                fill: false,
                                tension: 0.1,
                                pointRadius: 1
                            }
                        ]
                    },
                    options: {
                        scales: {y: {beginAtZero: true}},
                        plugins: {
                            legend: {position: 'top'},

                            datalabels: {
                                display: false
                            }
                        }
                    }
                });
            }
        }
    }



    /**
     * Handles the tab switching logic.
     * The WIP chart is initialized on page load. The performance charts are initialized
     * only once, the first time their tab is shown, for performance optimization.
     */
    initWipChart();

    const performanceTab = document.getElementById('performance-tab');
    let performanceChartsInitialized = false;

    if (performanceTab) {
        performanceTab.addEventListener('shown.bs.tab', function (event) {
            if (!performanceChartsInitialized) {
                initPerformanceCharts();
                performanceChartsInitialized = true;
            }
        });
    }
});