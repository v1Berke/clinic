package com.phermacyrepo.service;

import com.phermacyrepo.dto.SaleDTO;
import com.phermacyrepo.dto.SaleCreateDTO;

import java.util.*;

public interface SaleService {

    SaleDTO processSale(SaleCreateDTO saleCreateDTO);
    SaleDTO getSaleById(int id);
    List<SaleDTO> getAllSales();

}
