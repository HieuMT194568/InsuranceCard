package com.insurancecard.service;

import com.insurancecard.model.dto.DashboardDtos.CustomerDashboard;
import com.insurancecard.model.dto.DashboardDtos.StaffDashboard;

public interface DashboardService {

    CustomerDashboard forCustomer(Long customerId);

    StaffDashboard forStaff();
}
