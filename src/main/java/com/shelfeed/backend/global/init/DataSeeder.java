package com.shelfeed.backend.global.init;

import com.shelfeed.backend.domain.book.client.AladinApiClient;
import com.shelfeed.backend.domain.book.client.dto.AladinItem;
import com.shelfeed.backend.domain.book.client.dto.AladinSearchResponse;
import com.shelfeed.backend.domain.book.entity.Book;
import com.shelfeed.backend.domain.book.repository.BookRepository;
import com.shelfeed.backend.domain.feed.entity.Feed;
import com.shelfeed.backend.domain.feed.repository.FeedRepository;
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
    private final FeedRepository feedRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;

    private static final List<String> KEYWORDS = List.of(
            "소설", "자기계발", "과학", "역사", "철학"
    );

    private static final List<String[]> TEST_USERS = List.of(
            new String[]{"yuna.books@test.com", "책속의유나", "소설 속 세계가 현실보다 더 좋아요"},
            new String[]{"minjae.read@test.com", "새벽독서민재", "새벽 2시 책 읽는 게 취미"},
            new String[]{"sora.page@test.com", "페이지터너소라", "한 번 잡으면 끝까지 읽어버려요"},
            new String[]{"hyunjun.lit@test.com", "문학청년현준", "한국 현대문학을 사랑합니다"},
            new String[]{"jiwon.story@test.com", "이야기꾼지원", "좋은 이야기는 삶을 바꿔요"},
            new String[]{"eunji.novel@test.com", "로맨스마스터은지", "설레는 로맨스 소설이 전문"},
            new String[]{"taehoon.sci@test.com", "과학탐험가태훈", "과학책으로 우주를 여행해요"},
            new String[]{"minji.essay@test.com", "에세이수집가민지", "짧고 깊은 에세이가 좋아요"},
            new String[]{"junho.history@test.com", "역사덕후준호", "역사 속 인물들이 친구같아요"},
            new String[]{"areum.world@test.com", "세계문학아름", "번역소설로 세계 여행 중"},
            new String[]{"dongwoo.think@test.com", "생각하는동우", "철학책이 삶의 나침반이에요"},
            new String[]{"yerin.grow@test.com", "성장하는예린", "자기계발로 매일 1% 성장"},
            new String[]{"seojun.crime@test.com", "범죄소설매니아서준", "추리 없이는 못 살아요"},
            new String[]{"hana.poem@test.com", "시집수집가하나", "시 한 편으로 하루를 시작해요"},
            new String[]{"kangmin.bio@test.com", "위인전광팬강민", "위인들의 삶에서 용기를 얻어요"},
            new String[]{"suji.healing@test.com", "힐링북수지", "지친 하루엔 따뜻한 책 한 권"},
            new String[]{"woojin.fantasy@test.com", "판타지세계주민우진", "마법과 용이 등장하는 책만 봐요"},
            new String[]{"nayeon.art@test.com", "예술책애호가나연", "미술과 문학의 경계를 탐험 중"},
            new String[]{"jihoon.econ@test.com", "경제책독자지훈", "숫자로 세상을 이해하고 싶어요"},
            new String[]{"chaeyoung.read@test.com", "북클럽장채영", "매달 한 권 완독이 목표예요"}
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
            "결말이 너무 여운이 남아서 한동안 멍하니 있었습니다. 오래 기억에 남을 책이에요.",
            "기대하지 않고 읽었는데 생각보다 훨씬 감동적이었어요. 주변 친구들에게도 선물하고 싶네요.",
            "어려운 지식을 실생활 사례와 연결해서 설명해 주니 이해가 쏙쏙 되더라고요. 입문자에게 딱입니다.",
            "SF 장르인데도 인간의 본질에 대해 깊이 있게 다뤄서 많은 생각을 하게 만들었습니다.",
            "책장을 덮고 나서도 한참 동안 가슴이 먹먹했습니다. 올해 읽은 책 중 단연 최고예요.",
            "위로가 필요한 순간에 만난 선물 같은 책입니다. 지친 마음을 따뜻하게 다독여주네요.",
            "반전의 반전을 거듭하는 전개 덕분에 마지막까지 긴장의 끈을 놓을 수 없었습니다.",
            "이론적인 내용뿐만 아니라 실천적인 팁도 가득해서 정말 유용하게 읽었습니다.",
            "아이와 함께 읽었는데 어른인 저에게도 깊은 울림을 주더군요. 전 세대가 읽기 좋습니다.",
            "역사적인 사실을 바탕으로 풍부한 상상력을 더해 풀어낸 이야기가 정말 흥미진진했습니다.",
            "담백한 어조로 풀어내는 일상의 이야기들이 오히려 더 큰 위로와 공감을 불러일으킵니다.",
            "자존감이 낮아진 시기에 읽고 큰 힘을 얻었습니다. 다시 시작할 용기가 생겼어요.",
            "제목에 끌려 샀는데 내용도 제목만큼이나 매력적이고 깊이가 있었습니다.",
            "작가의 통찰력에 감탄하며 읽었습니다. 세상을 보는 프레임이 하나 더 늘어난 기분이에요.",
            "챕터마다 요약이 잘 되어 있어서 가독성이 뛰어나고 내용을 정리하기 좋았습니다.",
            "주인공의 심리 묘사가 탁월해서 마치 제가 소설 속 인물이 된 것 같은 몰입감을 느꼈어요.",
            "방대한 자료와 데이터를 바탕으로 설득력 있게 주장을 펼쳐서 읽는 내내 신뢰가 갔습니다.",
            "여행을 떠나지 못하는 답답함을 이 책 한 권으로 해소할 수 있었습니다. 풍경 묘사가 예술이에요.",
            "처음엔 두꺼운 두께에 압도당했지만, 한 장 한 장 넘기다 보니 시간 가는 줄 몰랐네요.",
            "철학적인 질문들을 끊임없이 던지며 스스로를 돌아보게 하는 힘이 있는 책입니다.",
            "가볍게 읽기 시작했는데 다 읽고 나니 묵직한 메시지가 가슴 속에 오래도록 남았습니다."
    };

    private static final String[] QUOTES = {
            "우리는 우리가 반복적으로 하는 것들의 산물이다.",
            "삶이 있는 곳에 희망이 있다.",
            "모든 위대한 여행은 첫 번째 발걸음에서 시작된다.",
            "독서는 완성된 인간을 만들고, 대화는 재치 있는 인간을 만든다.",
            "책 없는 방은 영혼 없는 몸과 같다.",
            "당신이 읽는 책이 당신을 만든다.",
            "가장 어두운 밤도 끝날 것이고, 태양은 떠오를 것이다.",
            "오늘의 독자가 내일의 리더가 된다.",
            "배움은 결코 마음을 지치게 하지 않는다.",
            "지혜는 경이로움에서 시작된다.",
            "독서는 세상을 향한 가장 조용한 여행이다.",
            "한 권의 책이 한 사람의 인생을 바꿀 수 있다.",
            "인내심은 쓰지만 그 열매는 달다.",
            "아는 것이 힘이며 책은 그 힘의 원천이다.",
            "성공은 최종적인 것이 아니며 실패는 치명적인 것이 아니다.",
            "교육은 세상을 바꾸기 위해 사용할 수 있는 가장 강력한 무기다.",
            "행복은 이미 만들어져 있는 것이 아니라 당신의 행동에서 나온다.",
            "책은 주머니 속에 담고 다닐 수 있는 정원이다.",
            "독서는 우리가 어디에 있든 다른 곳으로 갈 수 있는 티켓이다.",
            "책만큼 충실한 친구는 어디에도 없다.",
            "인생은 한 권의 책과 같아서 읽지 않는 사람은 한 페이지만을 읽는 것과 같다.",
            "책이 없는 집은 창문이 없는 방과 같다.",
            "당신이 반드시 알아야 할 단 한 가지는 도서관의 위치다.",
            "독서는 우리가 머물러야 할 때 우리에게 갈 곳을 마련해준다.",
            "말은 적절히 사용될 때 엑스레이처럼 모든 것을 꿰뚫는다.",
            "고전이란 누구나 찬양하지만 아무도 읽지 않는 책이 아니라 영원히 읽히는 책이다.",
            "좋은 친구 좋은 책 그리고 잠든 양심 이것이 이상적인 삶이다."
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

        if (members.isEmpty()) {
            log.info("[DataSeeder] 신규 생성된 멤버가 없습니다. 서재/팔로우 생성을 건너뜁니다.");
            return;
        }

        createLibraryAndReviews(members, books);
        createFollows(members);
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
                //다 읽은 사람 중 70% 만 감상 작성
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
            List<Member> candidates = new ArrayList<>(members);//복사
            candidates.remove(follower); // 자기 자신 제외
            Collections.shuffle(candidates, random);

            int count = 3 + random.nextInt(4); // 3~6명 팔로우
            List<Member> targets = candidates.subList(0, Math.min(count, candidates.size()));

            for (Member followee : targets) {
                if (followRepository.existsByFollowerAndFollowee(follower, followee)) continue;//중복은 패스

                followRepository.save(Follow.create(follower, followee));
                memberRepository.increaseFollowingCount(follower.getMemberUserId());
                memberRepository.increaseFollowerCount(followee.getMemberUserId());

                // followee의 PUBLIC+PUBLISHED 감상을 follower 피드에 소급 추가
                List<Review> recentReviews = reviewRepository.findUserReviews(
                        followee, null, org.springframework.data.domain.PageRequest.of(0, 30));
                if (!recentReviews.isEmpty()) {
                    feedRepository.saveAll(recentReviews.stream()
                            .map(r -> Feed.create(follower, r))
                            .toList());
                }
            }
        }
        log.info("[DataSeeder] 팔로우 관계 생성 완료");
    }
    //출간일 없어도 서버 실행 중단 방지
    private LocalDate parsePubDate(String pubDate) {
        if (pubDate == null || pubDate.isBlank()) return null;
        try {
            return LocalDate.parse(pubDate);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    // 장르 선택(가장 말단 장르)
    private String extractGenre(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) return null;
        String[] parts = categoryName.split(">");
        return truncate(parts[parts.length - 1].trim(), 100);
    }
    // 길이 자르기
    private String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() > max ? value.substring(0, max) : value;
    }
}
