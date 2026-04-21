package com.shelfeed.backend.domain.admin.service;

import com.shelfeed.backend.domain.admin.dto.response.AdminDashboardResponse;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.report.enums.ReportStatus;
import com.shelfeed.backend.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;

    public AdminDashboardResponse getDashboard() {
        long totalMembers = memberRepository.count();
        long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);
        long totalReports = reportRepository.count();
        return AdminDashboardResponse.of(totalMembers, pendingReports, totalReports);
    }
}
