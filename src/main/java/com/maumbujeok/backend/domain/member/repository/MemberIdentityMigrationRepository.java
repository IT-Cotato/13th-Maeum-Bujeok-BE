package com.maumbujeok.backend.domain.member.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class MemberIdentityMigrationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public void migratePhoneNumber(String oldPhoneNumber, String newPhoneNumber, String newName, String newBirthDate,
                                   LocalDateTime updatedAt) {
        entityManager.createNativeQuery("""
                insert into members (
                    phone_number,
                    name,
                    email,
                    password_hash,
                    birth_date,
                    provider,
                    provider_id,
                    terms_agreed_at,
                    privacy_agreed_at,
                    sensitive_data_agreed_at,
                    marketing_agreed_at,
                    onboarding_completed_at,
                    role,
                    created_at,
                    updated_at
                )
                select
                    :newPhoneNumber,
                    :newName,
                    email,
                    password_hash,
                    :newBirthDate,
                    provider,
                    provider_id,
                    terms_agreed_at,
                    privacy_agreed_at,
                    sensitive_data_agreed_at,
                    marketing_agreed_at,
                    onboarding_completed_at,
                    role,
                    created_at,
                    :updatedAt
                from members
                where phone_number = :oldPhoneNumber
                """)
                .setParameter("oldPhoneNumber", oldPhoneNumber)
                .setParameter("newPhoneNumber", newPhoneNumber)
                .setParameter("newName", newName)
                .setParameter("newBirthDate", newBirthDate)
                .setParameter("updatedAt", updatedAt)
                .executeUpdate();

        for (String tableName : MemberReferenceTables.MEMBER_REFERENCE_TABLES) {
            moveMemberReference(tableName, oldPhoneNumber, newPhoneNumber, updatedAt);
        }

        entityManager.createNativeQuery("""
                update refresh_tokens
                set user_key = :newPhoneNumber,
                    updated_at = :updatedAt
                where user_key = :oldPhoneNumber
                """)
                .setParameter("oldPhoneNumber", oldPhoneNumber)
                .setParameter("newPhoneNumber", newPhoneNumber)
                .setParameter("updatedAt", updatedAt)
                .executeUpdate();

        entityManager.createNativeQuery("delete from members where phone_number = :oldPhoneNumber")
                .setParameter("oldPhoneNumber", oldPhoneNumber)
                .executeUpdate();
    }

    private void moveMemberReference(String tableName, String oldPhoneNumber, String newPhoneNumber,
                                     LocalDateTime updatedAt) {
        entityManager.createNativeQuery("""
                update %s
                set member_phone_number = :newPhoneNumber,
                    updated_at = :updatedAt
                where member_phone_number = :oldPhoneNumber
                """.formatted(tableName))
                .setParameter("oldPhoneNumber", oldPhoneNumber)
                .setParameter("newPhoneNumber", newPhoneNumber)
                .setParameter("updatedAt", updatedAt)
                .executeUpdate();
    }
}
