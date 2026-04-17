package com.pesu.bookrental.vennela.service;

import com.pesu.bookrental.vennela.dto.ReviewForm;
import com.pesu.bookrental.domain.enums.RentalStatus;
import com.pesu.bookrental.domain.model.RentalTransaction;
import com.pesu.bookrental.domain.model.Review;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.repository.ReviewRepository;
import com.pesu.bookrental.repository.RentalTransactionRepository;
import com.pesu.bookrental.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RentalTransactionRepository rentalTransactionRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         RentalTransactionRepository rentalTransactionRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.rentalTransactionRepository = rentalTransactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Review> findReceivedReviews(User user) {
        return reviewRepository.findReceivedReviews(user);
    }

    @Transactional
    public void submitReview(Long rentalId, ReviewForm form, User reviewer) {
        RentalTransaction rentalTransaction = rentalTransactionRepository.findDetailedById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental transaction not found."));

        if (rentalTransaction.getStatus() != RentalStatus.COMPLETED) {
            throw new IllegalArgumentException("Reviews can only be submitted after rental completion.");
        }
        if (!rentalTransaction.getRenter().getId().equals(reviewer.getId())
                && !rentalTransaction.getLender().getId().equals(reviewer.getId())) {
            throw new IllegalArgumentException("You can only review your own rental transactions.");
        }
        if (reviewRepository.findByRentalTransactionAndReviewer(rentalTransaction, reviewer).isPresent()) {
            throw new IllegalArgumentException("You have already reviewed this transaction.");
        }

        User reviewedUser = rentalTransaction.getRenter().getId().equals(reviewer.getId())
                ? rentalTransaction.getLender()
                : rentalTransaction.getRenter();

        Review review = new Review();
        review.setRentalTransaction(rentalTransaction);
        review.setReviewer(reviewer);
        review.setReviewedUser(reviewedUser);
        review.setRating(form.getRating());
        review.setComment(form.getComment() == null ? null : form.getComment().trim());
        reviewRepository.save(review);

        recalculateAverageRating(reviewedUser);
    }

    private void recalculateAverageRating(User reviewedUser) {
        List<Review> reviews = reviewRepository.findReceivedReviews(reviewedUser);
        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        reviewedUser.setRatingAverage(Math.round(average * 100.0) / 100.0);
        userRepository.save(reviewedUser);
    }
}
