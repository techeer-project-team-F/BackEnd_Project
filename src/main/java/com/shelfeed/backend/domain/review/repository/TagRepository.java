package com.shelfeed.backend.domain.review.repository;

import com.shelfeed.backend.domain.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByTagName(String tagName);

    List<Tag> findByTagNameIn(List<String> tagNames);
}
