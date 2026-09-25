package com.phermacyrepo.service;

import com.phermacyrepo.dto.SalesReportDTO;
import com.phermacyrepo.dto.StockReportDTO;
import java.time.LocalDate;

public interface ReportService {

    SalesReportDTO getDailySalesReport(LocalDate date);
    StockReportDTO getDailyStockReport(LocalDate date);
}
