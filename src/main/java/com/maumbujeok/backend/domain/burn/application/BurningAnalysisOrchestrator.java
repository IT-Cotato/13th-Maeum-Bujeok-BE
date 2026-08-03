package com.maumbujeok.backend.domain.burn.application;
import com.maumbujeok.backend.domain.burn.ai.*;
import com.maumbujeok.backend.domain.burn.domain.*;
import com.maumbujeok.backend.domain.burn.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Slf4j
public class BurningAnalysisOrchestrator {
 private final BurningAnalysisRepository analyses; private final BurningRepository burnings; private final BurningAiClient client;
 public void analyze(Long analysisId,int revision){ if(!begin(analysisId,revision)) return; try{BurningAnalysis a=analyses.findById(analysisId).orElseThrow(); BurningAiResult result=client.analyze(new BurningAiRequest(a.getBurning().getSourceContent())); complete(analysisId,revision,result);}catch(RuntimeException e){fail(analysisId,revision,e.getClass().getSimpleName()); log.warn("Burning analysis failed analysisId={} revision={}",analysisId,revision,e);} }
 @Transactional public boolean begin(Long id,int rev){return analyses.findByIdForUpdate(id).map(a->a.markProcessing(rev)).orElse(false);}
 @Transactional public boolean complete(Long id,int rev,BurningAiResult r){return analyses.findByIdForUpdate(id).map(a->a.complete(rev,r)).orElse(false);}
 @Transactional public boolean fail(Long id,int rev,String code){return analyses.findByIdForUpdate(id).map(a->a.fail(rev,code)).orElse(false);}
}