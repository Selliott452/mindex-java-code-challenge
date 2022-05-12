package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import com.mindex.challenge.service.ReportingStructureService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportingStructureServiceImplTest {

    private String reportingStructureGetUrl;

    @Autowired
    private ReportingStructureService reportingStructureService;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        reportingStructureGetUrl = "http://localhost:" + port + "/reportingStructure/{id}";
    }

    @Test
    public void testReadNoCycles() {
        //Given a reporting structure with no cycles
        //leafs
        Employee testEmployee1 = employeeService.create(getTestEmployee(null));
        Employee testEmployee2 = employeeService.create(getTestEmployee(null));
        Employee testEmployee3 = employeeService.create(getTestEmployee(null));

        //nodes
        Employee testEmployee4 = employeeService.create(getTestEmployee(Arrays.asList(testEmployee1, testEmployee2)));
        Employee testEmployee5 = employeeService.create(getTestEmployee(Collections.singletonList(testEmployee3)));

        //root
        Employee testEmployee6 = employeeService.create(getTestEmployee(Arrays.asList(testEmployee4, testEmployee5)));

        //When the reporting structure is retrieved for the root employee via a get to the read endpoint
        ReportingStructure retrievedReportingStructure = restTemplate.getForEntity(
                reportingStructureGetUrl,
                ReportingStructure.class,
                testEmployee6.getEmployeeId()).getBody();

        //then a response is returned
        Objects.requireNonNull(retrievedReportingStructure);

        //And the response contains the information on the passed employee and the correct number of reports
        assertEquals(retrievedReportingStructure.getEmployee(), testEmployee6);
        assertEquals(retrievedReportingStructure.getNumberOfReports(), 5);
    }

    @Test
    public void testReadWithCycles() {
        //Given a reporting structure with a cycle
        //create two employees through the service to generate ids
        Employee testEmployee1 = employeeService.create(getTestEmployee(null));
        Employee testEmployee2 = employeeService.create(getTestEmployee(null));

        //update employees to point to each other in reporting structure
        testEmployee1.setDirectReports(stripNonIDFields(Collections.singletonList(testEmployee2)));
        testEmployee2.setDirectReports(stripNonIDFields(Collections.singletonList(testEmployee1)));
        employeeService.update(testEmployee1);
        employeeService.update(testEmployee2);

        //When the reporting structure is retrieved for one of the employees in the cycle
        ReportingStructure retrievedReportingStructure = restTemplate.getForEntity(
                reportingStructureGetUrl,
                ReportingStructure.class,
                testEmployee1.getEmployeeId()).getBody();

        //then a response is returned
        Objects.requireNonNull(retrievedReportingStructure);

        //And the response contains the information on the passed employee
        assertEquals(retrievedReportingStructure.getEmployee(), testEmployee1);

        //And the correct number of reports, not including themselves
        assertEquals(retrievedReportingStructure.getNumberOfReports(), 1);

    }

    private Employee getTestEmployee(List<Employee> reports) {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");
        testEmployee.setDirectReports(stripNonIDFields(reports));

        return testEmployee;
    }

    //Takes a nullable list of employees and returns a list of employees
    //with only the corresponding ID field populated
    private List<Employee> stripNonIDFields(List<Employee> employees) {
        //A sign that we really shouldn't be storing the full Employee entity
        //in the reporting structure but probably just the strings of employeeIDs
        //to prevent the possibility of recursive references and stack overflows
        //Looking to discuss the liberal use of employee entities as a field on other entities as part of code review
        if (employees != null) {
            employees = employees.stream().map(employee -> {
                        Employee idOnlyEmployee = new Employee();
                        idOnlyEmployee.setEmployeeId(employee.getEmployeeId());
                        return idOnlyEmployee;
                    }
            ).collect(Collectors.toList());
        }

        return employees;
    }

}
