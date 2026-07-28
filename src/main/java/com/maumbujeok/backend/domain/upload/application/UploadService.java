package com.maumbujeok.backend.domain.upload.application;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.upload.domain.DiaryUpload;
import com.maumbujeok.backend.domain.upload.domain.UploadState;
import com.maumbujeok.backend.domain.upload.dto.DiaryImageResponse;
import com.maumbujeok.backend.domain.upload.dto.PresignedUrlRequest;
import com.maumbujeok.backend.domain.upload.dto.PresignedUrlResponse;
import com.maumbujeok.backend.domain.upload.repository.DiaryUploadRepository;
import com.maumbujeok.backend.domain.upload.storage.ObjectNotUploadedException;
import com.maumbujeok.backend.domain.upload.storage.ObjectStorage;
import com.maumbujeok.backend.domain.upload.storage.ObjectStorageException;
import com.maumbujeok.backend.domain.upload.storage.PresignedObjectUrl;
import com.maumbujeok.backend.domain.upload.storage.StorageProperties;
import com.maumbujeok.backend.domain.upload.storage.StoredObject;
import com.maumbujeok.backend.global.error.CustomException;
import com.maumbujeok.backend.global.error.ErrorCode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final int MAX_IMAGES = 5;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final DiaryUploadRepository uploadRepository;
    private final MemberRepository memberRepository;
    private final ObjectStorage objectStorage;
    private final StorageProperties properties;

    @Transactional
    public PresignedUrlResponse issue(String phoneNumber, PresignedUrlRequest request) {
        if (request == null || !ALLOWED_TYPES.contains(request.contentType())) {
            throw new CustomException(ErrorCode.INVALID_UPLOAD_REQUEST);
        }
        if (request.fileSize() <= 0 || request.fileSize() > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.INVALID_UPLOAD_REQUEST);
        }
        UUID uploadId = UUID.randomUUID();
        String objectKey = "diary-images/" + UUID.randomUUID();
        Member member = memberRepository.getReferenceById(phoneNumber);
        PresignedObjectUrl signed = objectStorage.prepareUpload(
                objectKey, request.contentType(), request.fileSize(), properties.getUploadUrlValidity());
        uploadRepository.save(new DiaryUpload(
                uploadId, member, objectKey, request.contentType(), request.fileSize()));
        return new PresignedUrlResponse(uploadId, signed.url().toString(), signed.expiresAt());
    }

    @Transactional
    public void syncAttachments(String phoneNumber, Diary diary, List<UUID> requestedIds) {
        if (requestedIds == null) return;
        if (requestedIds.size() > MAX_IMAGES || new HashSet<>(requestedIds).size() != requestedIds.size()) {
            throw new CustomException(ErrorCode.INVALID_UPLOAD_REQUEST);
        }
        List<DiaryUpload> current = uploadRepository.findAllByDiaryIdAndStateOrderBySortOrderAsc(
                diary.getId(), UploadState.ATTACHED);
        Set<UUID> targetIds = new HashSet<>(requestedIds);
        current.stream()
                .filter(upload -> !targetIds.contains(upload.getId()))
                .forEach(DiaryUpload::markDeletionPending);
        if (requestedIds.isEmpty()) return;

        List<DiaryUpload> requested = uploadRepository.findAllByIdInAndMemberPhoneNumber(
                requestedIds, phoneNumber);
        if (requested.size() != requestedIds.size()) {
            throw new CustomException(ErrorCode.UPLOAD_NOT_FOUND);
        }
        for (int index = 0; index < requestedIds.size(); index++) {
            UUID id = requestedIds.get(index);
            DiaryUpload upload = requested.stream()
                    .filter(candidate -> candidate.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.UPLOAD_NOT_FOUND));
            if (!upload.canAttachTo(diary)) {
                throw new CustomException(ErrorCode.INVALID_UPLOAD_REQUEST);
            }
            StoredObject object;
            try {
                object = objectStorage.head(upload.getObjectKey());
            } catch (ObjectNotUploadedException exception) {
                throw new CustomException(ErrorCode.INVALID_UPLOAD_REQUEST);
            } catch (ObjectStorageException exception) {
                throw new CustomException(ErrorCode.UPLOAD_STORAGE_ERROR);
            }
            if (!upload.getContentType().equals(object.contentType()) || upload.getFileSize() != object.fileSize()) {
                throw new CustomException(ErrorCode.INVALID_UPLOAD_REQUEST);
            }
            upload.attach(diary, index);
        }
    }

    @Transactional(readOnly = true)
    public List<DiaryImageResponse> getImages(Long diaryId) {
        return uploadRepository.findAllByDiaryIdAndStateOrderBySortOrderAsc(diaryId, UploadState.ATTACHED)
                .stream()
                .map(upload -> new DiaryImageResponse(
                        upload.getId(),
                        objectStorage.createDownloadUrl(
                                upload.getObjectKey(), properties.getDownloadUrlValidity()).toString(),
                        upload.getSortOrder()))
                .toList();
    }

    @Transactional
    public void delete(String phoneNumber, UUID uploadId) {
        uploadRepository.findByIdAndMemberPhoneNumber(uploadId, phoneNumber)
                .ifPresent(DiaryUpload::markDeletionPending);
    }

    @Transactional
    public void markDiaryImagesForDeletion(Long diaryId) {
        uploadRepository.findAllByDiaryIdAndStateOrderBySortOrderAsc(diaryId, UploadState.ATTACHED)
                .forEach(DiaryUpload::markDeletionPending);
    }

    @Transactional
    public void markExpiredOrphans() {
        LocalDateTime cutoff = LocalDateTime.now().minus(properties.getOrphanRetention());
        uploadRepository.findTop100ByStateAndCreatedAtBeforeOrderByCreatedAtAsc(UploadState.PENDING, cutoff)
                .forEach(DiaryUpload::markDeletionPending);
    }

    @Transactional
    public void retryPendingDeletions() {
        for (DiaryUpload upload : uploadRepository.findTop100ByStateOrderByCreatedAtAsc(
                UploadState.DELETION_PENDING)) {
            try {
                objectStorage.delete(upload.getObjectKey());
                uploadRepository.delete(upload);
            } catch (RuntimeException exception) {
                log.warn("Upload deletion retry failed uploadId={}", upload.getId(), exception);
            }
        }
    }
}
