package com.maumbujeok.backend.domain.burn.application;

import com.maumbujeok.backend.domain.burn.domain.*;
import com.maumbujeok.backend.domain.burn.dto.*;
import com.maumbujeok.backend.domain.burn.repository.*;
import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.talisman.repository.TalismanRepository;
import com.maumbujeok.backend.global.error.ErrorCode;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service @RequiredArgsConstructor
public class BurningService {
    private static final int DEFAULT_SIZE=20, MAX_SIZE=50;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final BurningRepository burningRepository;
    private final BurningAnalysisRepository analysisRepository;
    private final TalismanRepository talismanRepository;
    private final Clock serviceClock;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreateBurningResponse create(String phone, CreateBurningRequest request) {
        validate(request);
        LocalDateTime now=LocalDateTime.now(serviceClock);
        Member member=memberRepository.getReferenceById(phone);
        Diary diary=null; Long diaryId=null; String content=request.content();
        if(request.sourceType()==BurningSourceType.DIARY){
            diary=diaryRepository.findByIdAndMemberPhoneNumber(request.diaryId(),phone).orElseThrow(()->error(ErrorCode.BURNING_NOT_FOUND,"Diary not found"));
            if(diary.isBurned() || burningRepository.existsByMemberPhoneNumberAndDiaryId(phone, diary.getId())) throw error(ErrorCode.DIARY_ALREADY_BURNED,"Diary has already been burned");
            diaryId=diary.getId(); content=diary.getContent();
        } else { content=content.trim(); }
        Burning burning=burningRepository.saveAndFlush(new Burning(member,request.sourceType(),diaryId,content,now));
        if(diary!=null){ diary.markBurned(burning.getId(),now); diaryRepository.flush(); }
        BurningAnalysis analysis=analysisRepository.save(new BurningAnalysis(burning));
        eventPublisher.publishEvent(new BurningAnalysisRequestedEvent(analysis.getId(), analysis.getInputRevision()));
        return new CreateBurningResponse(burning.getId(),burning.getSourceType(),burning.getBurnedAt(),analysis.getStatus());
    }

    @Transactional(readOnly=true)
    public BurningListResponse getPage(String phone, Long cursor, Integer requestedSize){
        int size=requestedSize==null?DEFAULT_SIZE:requestedSize; if(size<1||size>MAX_SIZE) throw error(ErrorCode.INVALID_BURNING_REQUEST,"size must be between 1 and 50");
        List<Burning> fetched=burningRepository.findCursor(phone,cursor,PageRequest.of(0,size+1)); boolean has=fetched.size()>size; List<Burning> page=has?fetched.subList(0,size):fetched;
        List<BurningListItemResponse> items=page.stream().map(b->{ BurningAnalysis a=analysisRepository.findByBurningId(b.getId()).orElseThrow(); return new BurningListItemResponse(b.getId(),b.getSourceType(),b.getBurnedAt(),a.getStatus(),talismanRepository.existsByBurnRitualId(b.getId())); }).toList();
        return new BurningListResponse(items,has?page.get(page.size()-1).getId():null,has);
    }

    @Transactional(readOnly=true)
    public BurningDetailResponse get(String phone, Long id){ Burning b=owned(phone,id); BurningAnalysis a=analysisRepository.findByBurningId(id).orElseThrow(); boolean talisman=talismanRepository.existsByBurnRitualId(id); return new BurningDetailResponse(id,b.getSourceType(),b.getBurnedAt(),a.getStatus(),a.getComment(),a.getTalismanType(),a.getTalismanText(),talisman); }
    @Transactional(readOnly=true)
    public BurningAnalysisResponse getAnalysis(String phone, Long id){ owned(phone,id); BurningAnalysis a=analysisRepository.findByBurningId(id).orElseThrow(); return new BurningAnalysisResponse(id,a.getStatus(),a.getInputRevision(),a.getComment(),a.getTalismanType(),a.getTalismanText(),a.getCompletedAt()); }
    private Burning owned(String phone,Long id){ return burningRepository.findByIdAndMemberPhoneNumber(id,phone).orElseThrow(()->error(ErrorCode.BURNING_NOT_FOUND,"Burning record not found")); }
    private void validate(CreateBurningRequest r){ if(r==null||r.sourceType()==null) throw error(ErrorCode.INVALID_BURNING_REQUEST,"sourceType is required"); if(r.sourceType()==BurningSourceType.DIRECT && (!StringUtils.hasText(r.content())||r.diaryId()!=null)) throw error(ErrorCode.INVALID_BURNING_REQUEST,"DIRECT requires content only"); if(r.sourceType()==BurningSourceType.DIARY && (r.diaryId()==null||StringUtils.hasText(r.content()))) throw error(ErrorCode.INVALID_BURNING_REQUEST,"DIARY requires diaryId only"); }
    private BurningRequestException error(ErrorCode c,String m){return new BurningRequestException(c,m);}
}
