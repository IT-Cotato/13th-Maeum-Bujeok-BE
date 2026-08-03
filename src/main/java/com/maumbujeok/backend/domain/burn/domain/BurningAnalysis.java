package com.maumbujeok.backend.domain.burn.domain;
import com.maumbujeok.backend.domain.burn.ai.BurningAiResult;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
@Entity @Table(name="burning_analyses") @Getter @NoArgsConstructor(access=AccessLevel.PROTECTED)
public class BurningAnalysis {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @OneToOne(fetch=FetchType.EAGER,optional=false) @JoinColumn(name="burning_id",nullable=false,unique=true) private Burning burning;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private BurningAnalysisStatus status;
 @Column(nullable=false) private int inputRevision;
 @Column(name="comment",columnDefinition="TEXT") private String comment;
 @Column(name="talisman_type",length=50) private String talismanType;
 @Column(name="talisman_text",length=100) private String talismanText;
 @Column(name="model_name",length=100) private String modelName;
 @Column(name="failure_code",length=100) private String failureCode;
 @Column(name="completed_at") private LocalDateTime completedAt;
 public BurningAnalysis(Burning burning){this.burning=burning;this.status=BurningAnalysisStatus.PENDING;this.inputRevision=1;}
 public boolean markProcessing(int revision){if(status!=BurningAnalysisStatus.PENDING||inputRevision!=revision)return false;status=BurningAnalysisStatus.PROCESSING;return true;}
 public boolean complete(int revision,BurningAiResult result){if(status!=BurningAnalysisStatus.PROCESSING||inputRevision!=revision)return false;comment=result.comment();talismanType=result.talismanType();talismanText=result.talismanText();modelName=result.modelName();status=BurningAnalysisStatus.COMPLETED;completedAt=LocalDateTime.now();return true;}
 public boolean fail(int revision,String code){if(inputRevision!=revision||status==BurningAnalysisStatus.COMPLETED)return false;status=BurningAnalysisStatus.FAILED;failureCode=code;return true;}
}