package com.shelfeed.backend.global.common.entity;
//엔티티에 createdAt, updatedAt 넣기 위한 부모 클래스
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter //클래스 속 메서드의 get을 알아서 만들어줌(getCreatedAt()를)
@MappedSuperclass //JAVA : 상속을 통해 중복 코드 제거, DB : 독립적인 테이블 안만듦. 각 자식 테이블에 부모 컬럼을 추가
@EntityListeners(AuditingEntityListener.class)//이벤트 발생 시 감지(각 자식 테이블 당 생성 수정일 자동화) 단, 메인 실행 파일 클레스에 @EnableJpaAuditing 달아야 함, 클래스는 실제 이벤트를 찾는 역할
public class BaseTimeEntity {

    @CreatedDate // INSERT 시점에 현재 시각을 자동으로 넣어줌. 이후 UPDATE가 일어나도 절대 안바뀜
    @Column(nullable = false, updatable = false)// NOTNULL추가, UPDATE 쿼리에 이 컬럼 포함 안함(최초 INSERT 이후 DB에서 변경 불가)
    private LocalDateTime createdAt;

    @LastModifiedDate// INSERT, UPDATE 할 때마다 현재 시각으로 자동 갱신
    @Column(nullable = false)
    private LocalDateTime updatedAt;

}
