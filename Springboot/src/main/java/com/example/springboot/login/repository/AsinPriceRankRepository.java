package com.example.springboot.login.repository;

import com.example.springboot.login.entity.AsinPriceRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsinPriceRankRepository extends JpaRepository<AsinPriceRank, Long> {

    // 查询每个ASIN的最新价格数据
    @Query("SELECT apr FROM AsinPriceRank apr " +
            "WHERE (apr.asin, apr.crawlTime) IN (" +
            "    SELECT apr2.asin, MAX(apr2.crawlTime) " +
            "    FROM AsinPriceRank apr2 " +
            "    WHERE apr2.asin IN :asins " +
            "    GROUP BY apr2.asin" +
            ")")
    List<AsinPriceRank> findLatestByAsins(@Param("asins") List<String> asins);

    // 根据ASIN列表查询所有价格数据（不限制时间）
    List<AsinPriceRank> findByAsinIn(List<String> asins);

    // 按任务ID查询所有价格数据（包含所有时间的记录）
    List<AsinPriceRank> findByTaskId(Long taskId);
}
