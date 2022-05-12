package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class CompensationServiceImpl implements CompensationService {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    @Autowired
    private CompensationRepository compensationRepository;

    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Creating compensation [{}]", compensation);

        return compensationRepository.insert(compensation);
    }

    @Override
    public Compensation[] read(String employeeId) {
        LOG.debug("Reading compensations for employee with ID [{}]", employeeId);

        return Arrays.stream(compensationRepository.findByEmployeeEmployeeId(employeeId))
                .sorted(Comparator.comparing(Compensation::getEffectiveDate))
                .toArray(Compensation[]::new);
    }
}
