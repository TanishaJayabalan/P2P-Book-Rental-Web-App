package com.pesu.bookrental.repository;

import com.pesu.bookrental.domain.enums.ReportStatus;
import com.pesu.bookrental.domain.model.Report;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("""
            select r from Report r
            join fetch r.reportedBy rb
            left join fetch r.targetUser tu
            order by r.id desc
            """)
    List<Report> findAllDetailed();

    long countByStatus(ReportStatus status);
}
