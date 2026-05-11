package com.shelfeed.backend.global.init;

import com.shelfeed.backend.domain.book.client.AladinApiClient;
import com.shelfeed.backend.domain.book.client.dto.AladinItem;
import com.shelfeed.backend.domain.book.client.dto.AladinSearchResponse;
import com.shelfeed.backend.domain.book.entity.Book;
import com.shelfeed.backend.domain.book.repository.BookRepository;
import com.shelfeed.backend.domain.follow.entity.Follow;
import com.shelfeed.backend.domain.follow.repository.FollowRepository;
import com.shelfeed.backend.domain.library.entity.LibraryBook;
import com.shelfeed.backend.domain.library.enums.ReadingStatus;
import com.shelfeed.backend.domain.library.repository.LibraryRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.review.entity.Review;
import com.shelfeed.backend.domain.review.enums.ReviewStatus;
import com.shelfeed.backend.domain.review.enums.ReviewVisibility;
import com.shelfeed.backend.domain.review.repository.ReviewRepository;
import com.shelfeed.backend.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final AladinApiClient aladinApiClient;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LibraryRepository libraryRepository;
    private final ReviewRepository reviewRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;

    private static final List<String> KEYWORDS = List.of(
            "소설", "자기계발", "과학", "역사", "철학"
    );

    private static final List<String[]> TEST_USERS = List.of(
            new String[]{"reader01@test.com", "독서광김철수", "책을 사랑하는 평범한 직장인"},
            new String[]{"reader02@test.com", "밤샘독서이영희", "잠자기 전 책 한 권은 필수"},
            new String[]{"reader03@test.com", "고전마니아박지훈", "고전문학을 좋아합니다"},
            new String[]{"reader04@test.com", "SF덕후최수현", "SF소설이라면 뭐든 환영"},
            new String[]{"reader05@test.com", "에세이러버정민준", "가볍게 읽는 에세이가 좋아요"},
            new String[]{"reader06@test.com", "역사탐구오지원", "역사 속에 답이 있다"},
            new String[]{"reader07@test.com", "철학청년김도현", "삶의 의미를 찾는 중"},
            new String[]{"reader08@test.com", "과학덕후장예린", "우주와 과학에 빠져삽니다"},
            new String[]{"reader09@test.com", "자기계발러나성호", "매일 조금씩 성장 중"},
            new String[]{"reader10@test.com", "다독가한미래", "장르 불문 뭐든 읽어요"}
    );

    private static final String[] CONTENTS = {
            "이 책을 읽고 나서 삶을 바라보는 시각이 완전히 달라졌습니다. 작가의 섬세한 문체가 인상적이었어요.",
            "처음에는 어렵게 느껴졌지만 읽다 보니 깊이 빠져들었습니다. 강력 추천합니다.",
            "주인공의 성장 과정이 너무 공감됐어요. 내 이야기 같아서 중간중간 울컥했습니다.",
            "전개가 빠르고 긴장감이 넘쳐서 한 번에 다 읽어버렸어요. 밤새 읽은 보람이 있었습니다.",
            "문장 하나하나가 시처럼 아름다웠습니다. 밑줄 친 구절이 너무 많아요.",
            "생각보다 훨씬 깊이 있는 내용이었어요. 여러 번 다시 읽고 싶은 책입니다.",
            "번역이 매끄럽고 원작의 느낌이 잘 살아있었습니다. 원서로도 읽어보고 싶어졌어요.",
            "이 작가의 다른 작품도 꼭 읽어봐야겠다고 생각했습니다. 정말 좋은 작가를 발견했어요.",
            "복잡한 내용을 쉽게 풀어쓴 점이 좋았습니다. 입문서로 강력히 추천합니다.",
            "결말이 너무 여운이 남아서 한동안 멍하니 있었습니다. 오래 기억에 남을 책이에요."
    };

    private static final String[] QUOTES = {
            "우리는 우리가 반복적으로 하는 것들의 산물이다.",
            "삶이 있는 곳에 희망이 있다.",
            "모든 위대한 여행은 첫 번째 발걸음에서 시작된다.",
            "독서는 완성된 인간을 만들고, 대화는 재치 있는 인간을 만든다.",
            "책 없는 방은 영혼 없는 몸과 같다.",
            "당신이 읽는 책이 당신을 만든다.",
            "가장 어두운 밤도 끝날 것이고, 태양은 떠오를 것이다."
    };

    @Override
    public void run(ApplicationArguments args) {
        log.info("[DataSeeder] 테스트 데이터 생성 시작");

        List<Book> books = fetchAndSaveBooks();
        log.info("[DataSeeder] 저장된 도서 수: {}", books.size());

        if (books.isEmpty()) {
            log.warn("[DataSeeder] 알라딘 API에서 도서를 가져오지 못했습니다. 중단합니다.");
            return;
        }

        List<Member> members = createMembers();
        log.info("[DataSeeder] 생성된 멤버 수: {}", members.size());

        // 기존 멤버 포함 전체 테스트 유저 로드
        List<Member> allMembers = TEST_USERS.stream()
                .flatMap(info -> memberRepository.findByEmail(info[0]).stream())
                .toList();

        createLibraryAndReviews(allMembers, books);
        createFollows(allMembers);
        log.info("[DataSeeder] 테스트 데이터 생성 완료");
    }

    private List<Book> fetchAndSaveBooks() {
        List<Book> result = new ArrayList<>();
        Random random = new Random();

        for (String keyword : KEYWORDS) {
            try {
                int start = random.nextInt(3) + 1;
                AladinSearchResponse response = aladinApiClient.search(keyword, start, 10);
                if (response == null || response.getItems() == null) continue;

                for (AladinItem item : response.getItems()) {
                    if (item.getIsbn13() == null || item.getIsbn13().isBlank()) continue;

                    Book book = bookRepository.findByIsbn13(item.getIsbn13()).orElseGet(() ->
                            bookRepository.save(Book.create(
                                    item.getIsbn13(),
                                    truncate(item.getTitle(), 500),
                                    truncate(item.getAuthor(), 50),
                                    truncate(item.getPublisher(), 200),
                                    item.getCover(),
                                    item.getDescription(),
                                    item.getSubInfo() != null ? item.getSubInfo().getItemPage() : null,
                                    parsePubDate(item.getPubDate()),
                                    item.getItemId() != null ? item.getItemId().toString() : null,
                                    truncate(item.getCategoryName(), 100),
                                    extractGenre(item.getCategoryName())
                            ))
                    );
                    result.add(book);
                }
            } catch (Exception e) {
                log.warn("[DataSeeder] '{}' 검색 실패: {}", keyword, e.getMessage());
            }
        }
        return result;
    }

    private List<Member> createMembers() {
        List<Member> members = new ArrayList<>();
        String encodedPw = passwordEncoder.encode("Test1234!");

        for (String[] info : TEST_USERS) {
            String email = info[0], nickname = info[1], bio = info[2];
            if (memberRepository.existsByEmail(email)) continue;

            Long userId = redisService.generateMemberUserId();
            Member member = Member.createLocal(userId, email, encodedPw, nickname, bio);
            member.verifyEmail();
            member.completeOnboarding();
            members.add(memberRepository.save(member));
        }
        return members;
    }

    private void createLibraryAndReviews(List<Member> members, List<Book> books) {
        Random random = new Random();
        ReadingStatus[] statuses = ReadingStatus.values();

        for (Member member : members) {
            List<Book> shuffled = new ArrayList<>(books);
            Collections.shuffle(shuffled, random);
            int count = 3 + random.nextInt(5); // 멤버당 3~7권
            List<Book> picked = shuffled.subList(0, Math.min(count, shuffled.size()));

            for (Book book : picked) {
                ReadingStatus status = statuses[random.nextInt(statuses.length)];

                if (libraryRepository.existsByMemberAndBook_BookId(member, book.getBookId())) continue;
                LibraryBook lb = libraryRepository.save(LibraryBook.create(member, book, status));

                boolean shouldReview = (status == ReadingStatus.FINISHED || status == ReadingStatus.READING)
                        && random.nextInt(10) < 7;
                if (!shouldReview) continue;
                if (reviewRepository.existsByMemberAndBook_BookIdAndIsDeletedFalse(member, book.getBookId())) continue;

                byte rating = (byte) (3 + random.nextInt(3)); // 3~5점
                String content = CONTENTS[random.nextInt(CONTENTS.length)];
                String quote = random.nextBoolean() ? QUOTES[random.nextInt(QUOTES.length)] : null;
                ReviewVisibility visibility = random.nextInt(10) < 8
                        ? ReviewVisibility.PUBLIC : ReviewVisibility.PRIVATE;

                reviewRepository.save(Review.create(
                        member, book, lb,
                        rating, content, quote,
                        false, book.getTotalPages(),
                        visibility, ReviewStatus.PUBLISHED
                ));
                memberRepository.increaseReviewCount(member.getMemberUserId());
            }
        }
    }

    private void createFollows(List<Member> members) {
        Random random = new Random();

        for (Member follower : members) {
            List<Member> candidates = new ArrayList<>(members);
            candidates.remove(follower); // 자기 자신 제외
            Collections.shuffle(candidates, random);

            int count = 3 + random.nextInt(4); // 3~6명 팔로우
            List<Member> targets = candidates.subList(0, Math.min(count, candidates.size()));

            for (Member followee : targets) {
                if (followRepository.existsByFollowerAndFollowee(follower, followee)) continue;

                followRepository.save(Follow.create(follower, followee));
                memberRepository.increaseFollowingCount(follower.getMemberUserId());
                memberRepository.increaseFollowerCount(followee.getMemberUserId());
            }
        }
        log.info("[DataSeeder] 팔로우 관계 생성 완료");
    }

    private LocalDate parsePubDate(String pubDate) {
        if (pubDate == null || pubDate.isBlank()) return null;
        try {
            return LocalDate.parse(pubDate);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String extractGenre(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) return null;
        String[] parts = categoryName.split(">");
        return truncate(parts[parts.length - 1].trim(), 100);
    }

    private String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() > max ? value.substring(0, max) : value;
    }
}
