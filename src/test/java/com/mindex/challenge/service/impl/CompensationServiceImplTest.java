package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    private String compensationPostUrl;
    private String compensationGetUrl;

    @Autowired
    private CompensationService compensationService;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        compensationPostUrl = "http://localhost:" + port + "/compensation";
        compensationGetUrl = "http://localhost:" + port + "/compensation/{id}";
    }

    @Test
    public void testCreateRead() {
        //Given a set of two compensations associated to the same employee
        Employee testEmployee = employeeService.create(getTestEmployee());
        Compensation testCompensation1 = getTestCompensation(testEmployee);
        Compensation testCompensation2 = getTestCompensation(testEmployee);

        //When the compensations are posted to the create endpoint
        Compensation savedTestCompensation1 = restTemplate.postForEntity(
                compensationPostUrl, testCompensation1, Compensation.class).getBody();

        Compensation savedTestCompensation2 = restTemplate.postForEntity(
                compensationPostUrl, testCompensation2, Compensation.class).getBody();

        //Then the compensations should be persisted properly
        assertEquals(testCompensation1, savedTestCompensation1);
        assertEquals(testCompensation2, savedTestCompensation2);

        //When the compensations are retrieved via a get to the read endpoint
        Compensation[] retrievedCompensations = restTemplate.getForEntity(
                compensationGetUrl, Compensation[].class, testEmployee.getEmployeeId()).getBody();

        //Then a response is returned
        Objects.requireNonNull(retrievedCompensations);

        //And the response contains the previously posted compensations
        assert (retrievedCompensations.length == 2);

        assert (Arrays.asList(retrievedCompensations)
                .contains(savedTestCompensation1));

        assert (Arrays.asList(retrievedCompensations)
                .contains(savedTestCompensation2));
    }

    private Employee getTestEmployee() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        return testEmployee;
    }

    private Compensation getTestCompensation(Employee employee) {
        Compensation testCompensation = new Compensation();
        testCompensation.setEmployee(employee);
        testCompensation.setSalary(BigDecimal.valueOf(Math.random()));
        testCompensation.setEffectiveDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        return testCompensation;
    }

}
