package com.maumbujeok.backend.domain.talisman.repository;

import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class TalismanRepositoryImpl implements TalismanRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Talisman> findTalismansWithCursor(
            String memberPhoneNumber,
            Long cursor,
            LocalDateTime startOfWeek,
            LocalDateTime endOfWeek,
            int size
    ) {
        StringBuilder jpql = new StringBuilder(
                "SELECT t FROM Talisman t WHERE t.member.phoneNumber = :memberPhoneNumber "
                + "AND t.createdAt >= :startOfWeek AND t.createdAt <= :endOfWeek "
        );

        if (cursor != null) {
            jpql.append("AND t.id < :cursor ");
        }

        jpql.append("ORDER BY t.id DESC");

        TypedQuery<Talisman> query = entityManager.createQuery(jpql.toString(), Talisman.class);
        query.setParameter("memberPhoneNumber", memberPhoneNumber);
        query.setParameter("startOfWeek", startOfWeek);
        query.setParameter("endOfWeek", endOfWeek);

        if (cursor != null) {
            query.setParameter("cursor", cursor);
        }

        // Fetch size + 1 to determine if next page exists
        query.setMaxResults(size + 1);

        return query.getResultList();
    }
}
