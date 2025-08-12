package com.example.springboot.login.specification;

import com.example.springboot.login.entity.CrawlerTask;
import com.example.springboot.login.entity.User;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class CrawlerTaskSpecifications {

    public static Specification<CrawlerTask> byUser(User user) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user"), user);
    }

    public static Specification<CrawlerTask> byStatus(String status) {
        return (root, query, criteriaBuilder) ->
                status == null || status.isEmpty()
                        ? criteriaBuilder.conjunction() // 空条件
                        : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<CrawlerTask> byTimeCycle(String timeCycle) {
        return (root, query, criteriaBuilder) ->
                timeCycle == null || timeCycle.isEmpty()
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("timeCycle"), timeCycle);
    }

    public static Specification<CrawlerTask> byPlatform(String platform) {
        return (root, query, criteriaBuilder) ->
                platform == null || platform.isEmpty()
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("platform"), platform);
    }

    public static Specification<CrawlerTask> byKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("processName")),
                    "%" + keyword.toLowerCase() + "%"));
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("asinList")),
                    "%" + keyword.toLowerCase() + "%"));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }
}