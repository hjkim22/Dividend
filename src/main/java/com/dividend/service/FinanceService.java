package com.dividend.service;

import com.dividend.exception.impl.NoCompanyException;
import com.dividend.model.Company;
import com.dividend.model.Dividend;
import com.dividend.model.ScrapedResult;
import com.dividend.persist.CompanyRepository;
import com.dividend.persist.DividendRepository;
import com.dividend.persist.entity.CompanyEntity;
import com.dividend.persist.entity.DividendEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.dividend.model.constants.CacheKey.KEY_FINANCE;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    @Cacheable(key = "#companyName", value = KEY_FINANCE)
    public ScrapedResult getDividendByCompanyNames(String companyName) {
        log.info("search company -> " + companyName);

        // 1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new NoCompanyException());

        // 2. 조회된 회사 ID 로 배당금 정보 조회
        List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(
                company.getId());

        // 3. 결과 조합 후 반환
        List<Dividend> dividends = dividendEntities.stream()
                .map(e -> new Dividend(e.getDate(), e.getDividend()))
                .collect(Collectors.toList());

        return new ScrapedResult(new Company(company.getTicker(), company.getName()), dividends);
    }
}
