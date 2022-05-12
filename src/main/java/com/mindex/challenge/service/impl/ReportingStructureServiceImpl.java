package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.ReportingStructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReportingStructureServiceImpl implements ReportingStructureService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public ReportingStructure getReportingStructure(String employeeId) {
        ReportingStructure reportingStructure = new ReportingStructure();
        Employee employee = employeeRepository.findByEmployeeId(employeeId);

        //Working under the assumption that an employee could not report to themselves
        //even if there was a cycle that pointed back to the root
        //Look to discuss in code review, if this is a desired behavior we can simply inline
        //the call to the recursive method and forgo the var here.
        Set<String> reports = new HashSet<>();
        getReportsRecursive(reports, employee, true);
        reports.remove(employeeId);

        reportingStructure.setEmployee(employee);
        reportingStructure.setNumberOfReports(reports.size());

        return reportingStructure;
    }

    private void getReportsRecursive(Set<String> employeeIDs, Employee current, Boolean isRoot) {

        String currentEmployeeId = current.getEmployeeId();

        //If we haven't already traversed the current employee
        if (!employeeIDs.contains(currentEmployeeId)) {

            //If this is the first time calling the recursive function (The root call) do not include
            //the currentEmployeeId as we are looking for reports to the root
            if(!isRoot) {
                employeeIDs.add(currentEmployeeId);
            }

            //Recursively call on current employees reports if they exist
            List<Employee> currentDirectReports = current.getDirectReports();

            if(currentDirectReports != null) {
                currentDirectReports.forEach(report ->
                                getReportsRecursive(
                                        employeeIDs,
                                        employeeRepository.findByEmployeeId(report.getEmployeeId()),
                                        false
                                )
                );
            }
        }
    }

}
