package com.maumbujeok.backend.domain.burn.domain;

import com.maumbujeok.backend.domain.burn.ai.BurningAiResult;
import com.maumbujeok.backend.domain.member.domain.Member;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BurningAnalysisTest {
    @Test
    void transitionsOnlyFromPendingToProcessingAndCompleted() {
        Member member = Member.builder().phoneNumber("01000000000").build();
        Burning burning = new Burning(member, BurningSourceType.DIRECT, null, "source", LocalDateTime.now());
        BurningAnalysis analysis = new BurningAnalysis(burning);

        assertTrue(analysis.markProcessing(1));
        assertFalse(analysis.markProcessing(1));
        assertTrue(analysis.complete(1, new BurningAiResult("comment", "type", "CALM", "fake")));
        assertEquals(BurningAnalysisStatus.COMPLETED, analysis.getStatus());
        assertEquals("comment", analysis.getComment());
        assertFalse(analysis.fail(1, "late-failure"));
    }

    @Test
    void ignoresStaleRevision() {
        Member member = Member.builder().phoneNumber("01000000000").build();
        Burning burning = new Burning(member, BurningSourceType.DIRECT, null, "source", LocalDateTime.now());
        BurningAnalysis analysis = new BurningAnalysis(burning);
        assertFalse(analysis.markProcessing(2));
        assertEquals(BurningAnalysisStatus.PENDING, analysis.getStatus());
    }
}