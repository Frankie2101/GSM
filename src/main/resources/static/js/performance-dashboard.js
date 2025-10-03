document.addEventListener("DOMContentLoaded", function() {

    const throughputCanvas = document.getElementById('dailyThroughputChart');
    if (throughputCanvas) {
        const throughputData = JSON.parse(throughputCanvas.dataset.chartData || '{}');
        new Chart(throughputCanvas.getContext('2d'), {
            type: 'bar',
            data: {
                labels: Object.keys(throughputData),
                datasets: [{
                    label: 'Packed Quantity',
                    data: Object.values(throughputData),
                    backgroundColor: 'rgba(78, 115, 223, 0.8)',
                    borderColor: 'rgba(78, 115, 223, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                scales: { y: { beginAtZero: true } },
                plugins: { legend: { display: false } }
            }
        });
    }

    const sCurveCanvas = document.getElementById('sCurveChart');
    if (sCurveCanvas) {
        const sCurveData = JSON.parse(sCurveCanvas.dataset.chartData || '{}');
        new Chart(sCurveCanvas.getContext('2d'), {
            type: 'line',
            data: {
                labels: sCurveData.labels,
                datasets: [
                    {
                        label: 'Planned Cumulative Qty',
                        data: sCurveData.plannedData,
                        borderColor: 'rgba(28, 200, 138, 1)',
                        backgroundColor: 'rgba(28, 200, 138, 0.1)',
                        fill: true,
                        tension: 0.1
                    },
                    {
                        label: 'Actual Cumulative Qty',
                        data: sCurveData.actualData,
                        borderColor: 'rgba(78, 115, 223, 1)',
                        backgroundColor: 'rgba(78, 115, 223, 0.1)',
                        fill: true,
                        tension: 0.1
                    }
                ]
            },
            options: {
                scales: { y: { beginAtZero: true } },
                plugins: { legend: { position: 'top' } }
            }
        });
    }
});