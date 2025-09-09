document.addEventListener("DOMContentLoaded", function() {
    // Đăng ký plugin hiển thị số liệu một lần duy nhất
    Chart.register(ChartDataLabels);

    // --- Hàm khởi tạo biểu đồ cho Tab 1: WIP ---
    function initWipChart() {
        const chartCanvas = document.getElementById('wipDonutChart');
        if (!chartCanvas) return;

        // Lấy dữ liệu từ thẻ script ẩn trong file dashboard.mustache
        const dataElement = document.getElementById('wipChartData');
        if (!dataElement) return;

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

    // --- Hàm khởi tạo các biểu đồ cho Tab 2: Performance ---
    function initPerformanceCharts() {
        // Biểu đồ Daily Throughput

        const throughputCanvas = document.getElementById('dailyThroughputChart');
        if (throughputCanvas) {
            const dataElement = document.getElementById('throughputChartData');
            const throughputData = JSON.parse(dataElement.textContent || '{}');

            // Kiểm tra nếu không có dữ liệu thì hiển thị thông báo
            if (!throughputData.labels || throughputData.labels.length === 0) {
                showNoDataMessage(throughputCanvas);
            } else {
                new Chart(throughputCanvas.getContext('2d'), {
                    type: 'bar',
                    data: {
                        labels: throughputData.labels, // Lấy nhãn từ DTO
                        datasets: [
                            // Dataset cho Cutting
                            {
                                label: 'Cutting',
                                data: throughputData.cuttingData,
                                backgroundColor: 'rgba(54, 162, 235, 0.8)', // Màu xanh dương
                                borderColor: 'rgba(54, 162, 235, 1)',
                                borderWidth: 1
                            },
                            // Dataset cho Sewing
                            {
                                label: 'Sewing',
                                data: throughputData.sewingData,
                                backgroundColor: 'rgba(255, 206, 86, 0.8)', // Màu vàng
                                borderColor: 'rgba(255, 206, 86, 1)',
                                borderWidth: 1
                            },
                            // Dataset cho Packing
                            {
                                label: 'Packing',
                                data: throughputData.packingData,
                                backgroundColor: 'rgba(75, 192, 192, 0.8)', // Màu xanh lá
                                borderColor: 'rgba(75, 192, 192, 1)',
                                borderWidth: 1
                            }
                        ]
                    },
                    options: {
                        scales: {
                            y: {
                                beginAtZero: true,
                                stacked: false // Đặt là false để có biểu đồ cột nhóm
                            },
                            x: {
                                stacked: false
                            }
                        },
                        plugins: {
                            legend: {
                                position: 'top' // Hiển thị chú thích ở trên
                            }
                        }
                    }
                });
            }
        }

        // Biểu đồ S-Curve
        const sCurveCanvas = document.getElementById('sCurveChart');
        if (sCurveCanvas) {
            const dataElement = document.getElementById('sCurveChartData');
            const sCurveData = JSON.parse(dataElement.textContent || '{}');

            if (!sCurveData.labels || sCurveData.labels.length === 0) {
                showNoDataMessage(sCurveCanvas);
            } else {
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
                                pointRadius: 1 // Làm cho các điểm dữ liệu nhỏ hơn
                            },
                            {
                                label: 'Actual',
                                data: sCurveData.actualData,
                                borderColor: 'rgba(78, 115, 223, 1)',
                                backgroundColor: 'rgba(78, 115, 223, 0.1)',
                                fill: false,
                                tension: 0.1,
                                pointRadius: 1 // Làm cho các điểm dữ liệu nhỏ hơn
                            }
                        ]
                    },
                    options: {
                        scales: {y: {beginAtZero: true}},
                        plugins: {
                            legend: {position: 'top'},

                            // --- THÊM VÀO ĐÂY ---
                            // Tắt plugin hiển thị số trên biểu đồ
                            datalabels: {
                                display: false
                            }
                            // --- KẾT THÚC PHẦN THÊM ---
                        }
                    }
                });
            }
        }
    }



    // --- Xử lý sự kiện chuyển tab ---
    // Khởi tạo biểu đồ cho tab đầu tiên (WIP) ngay khi tải trang
    initWipChart();

    const performanceTab = document.getElementById('performance-tab');
    let performanceChartsInitialized = false;

    if (performanceTab) {
        performanceTab.addEventListener('shown.bs.tab', function (event) {
            // Chỉ khởi tạo các biểu đồ của tab performance một lần duy nhất
            if (!performanceChartsInitialized) {
                initPerformanceCharts();
                performanceChartsInitialized = true;
            }
        });
    }
});