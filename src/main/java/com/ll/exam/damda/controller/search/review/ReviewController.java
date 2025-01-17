package com.ll.exam.damda.controller.search.review;

import com.ll.exam.damda.entity.search.Review;
import com.ll.exam.damda.entity.search.Spot;
import com.ll.exam.damda.entity.user.SiteUser;
import com.ll.exam.damda.form.review.ReviewForm;
import com.ll.exam.damda.service.review.DataNotFoundException;
import com.ll.exam.damda.service.review.ReviewService;
import com.ll.exam.damda.service.review.ReviewTagService;
import com.ll.exam.damda.service.search.spot.SpotService;
import com.ll.exam.damda.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.security.Principal;


@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final SpotService spotService;
    private final ReviewTagService reviewTagService;

    @RequestMapping("/review")
    public String createReview() {
        return "review/createReview";
    }

    @RequestMapping("/review/show")
    public String readReview() {
        return "review/readReview";
    }


    @GetMapping("/review/show/{id}")
    public String detail(Model model, @PathVariable long id, ReviewForm reviewForm) {
        Review review = reviewService.getReview(id);

        model.addAttribute("review", review);

        return "review/readReview";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/review/modify/{id}")
    public String reviewModify(Principal principal, ReviewForm reviewForm, @PathVariable("id") long id) {
        Review review = this.reviewService.getReview(id);

        if ( review == null ) {
            throw new DataNotFoundException("해당 질문은 존재하지 않습니다.");
        }

        if(!review.getSiteUser().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        reviewForm.setTitle(review.getTitle());
        reviewForm.setContent(review.getContent());
        // 수정 시 초기화 없애는 작업 중
        reviewForm.setReviewTags(reviewTagService.getReviewTagList(reviewTagService.getReviewTagListByReview(review)));

        return "review/createReview";
    }

    @PostMapping("/review/modify/{id}")
    public String reviewModify(Principal principal, @Valid ReviewForm reviewForm, BindingResult bindingResult,
                               @PathVariable("id") long id) {

        if (bindingResult.hasErrors()) {
            return "review/createReview";
        }
        Review review = this.reviewService.getReview(id);

        if (review == null) {
            throw new DataNotFoundException("해당 리뷰는 존재하지 않습니다.");
        }

        if (!review.getSiteUser().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        reviewService.modify(review, reviewForm.getTitle(), reviewForm.getContent(), reviewForm.getReviewTags());

        return String.format("redirect:/review/show/%s", id);
    }
    @RequestMapping("/review/list")
    public String list(Model model, @RequestParam(defaultValue = "0") int page) {
        Page<Review> paging = reviewService.getList(page);
        model.addAttribute("paging", paging);

        return "review/allReviewList";
    }

    @RequestMapping("/review/spotReviewList/{spotId}")
    public String spotReviewList(Model model,@PathVariable Long spotId, @RequestParam(defaultValue = "0") int page) {
        Page<Review> paging = reviewService.getListBySpot(spotId, page);
        model.addAttribute("paging", paging);

        return "review/spotReviewList";
    }

    @RequestMapping("/review/myList")
    public String myList(Principal principal, Model model, @RequestParam(defaultValue = "0") int page) {
        String name = principal.getName();
        Page<Review> paging = reviewService.getListByUser(name, page);
        model.addAttribute("paging", paging);

        return "review/myReviewList";
    }


    @GetMapping("/review/create")
    public String reviewCreate(ReviewForm reviewForm) {
        //빈 객체라도 생기도록 써준 것 reviewForm과 같이 바인딩한 객체는 Model 객체로 전달하지 않아도 템플릿에서 사용이 가능하다.
        return "review/createReview";
    }

    @PostMapping("/review/create")
    public String reviewCreate(@RequestParam("spotId") Long spotId, Principal principal,
                               //@RequestParam(value = "checkedValue", defaultValue ="") List<String> checkedValue,
                               Model model, @Valid ReviewForm reviewForm,
                               BindingResult bindingResult) {
        Spot spot = spotService.getSpot(spotId);

        if (bindingResult.hasErrors()) {
            return "review/createReview";
        }

        SiteUser siteUser = userService.getUser(principal.getName());
        Review review= reviewService.create( reviewForm.getTitle(), reviewForm.getContent(), siteUser, spot);
        reviewService.saveReviewTag(review, reviewForm.getReviewTags());
      return "redirect:myList";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/review/delete/{id}")
    public String reviewDelete(Principal principal, @PathVariable("id") Integer id) {
        Review review = reviewService.getReview(id);

        if (review == null) {
            throw new DataNotFoundException("%d번 질문은 존재하지 않습니다.");
        }

        if (!review.getSiteUser().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }

        reviewService.delete(review);

        return "redirect:/review/myList";
    }


    @RequestMapping("/requestSpot")
    public String requestSpot() {
        return "/requestSpot/requestSpot";
    }

    @RequestMapping("/managing")
    public String managing() {
        return "/managing/managing";
    }

}