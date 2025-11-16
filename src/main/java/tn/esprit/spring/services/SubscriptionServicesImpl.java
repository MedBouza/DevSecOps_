package tn.esprit.spring.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.spring.entities.Skier;
import tn.esprit.spring.entities.Subscription;
import tn.esprit.spring.entities.TypeSubscription;
import tn.esprit.spring.repositories.ISkierRepository;
import tn.esprit.spring.repositories.ISubscriptionRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@AllArgsConstructor
@Service
public class SubscriptionServicesImpl implements ISubscriptionServices {

    private ISubscriptionRepository subscriptionRepository;
    private ISkierRepository skierRepository;

    @Override
    public Subscription addSubscription(Subscription subscription) {
        log.info("Adding new subscription: {}", subscription);
        // compute end date depending on type
        switch (subscription.getTypeSub()) {
            case ANNUAL:
                subscription.setEndDate(subscription.getStartDate().plusYears(1));
                break;
            case SEMESTRIEL:
                subscription.setEndDate(subscription.getStartDate().plusMonths(6));
                break;
            case MONTHLY:
                subscription.setEndDate(subscription.getStartDate().plusMonths(1));
                break;
            default:
                log.warn("Unknown subscription type: {}", subscription.getTypeSub());
        }
        // Let exceptions propagate so tests/CI can surface DB/schema errors instead of producing NPEs later
        Subscription savedSub = subscriptionRepository.save(subscription);
        log.info("Subscription added successfully: {}", savedSub);
        return savedSub;
    }

    @Override
    public Subscription updateSubscription(Subscription subscription) {
        log.info("Updating subscription with ID: {}", subscription.getNumSub());
        Subscription updated = subscriptionRepository.save(subscription);
        log.debug("Updated subscription details: {}", updated);
        return updated;
    }

    @Override
    public Subscription retrieveSubscriptionById(Long numSubscription) {
        log.debug("Retrieving subscription with ID: {}", numSubscription);
        return subscriptionRepository.findById(numSubscription).orElse(null);
    }

    @Override
    public Set<Subscription> getSubscriptionByType(TypeSubscription type) {
        log.info("Fetching subscriptions of type: {}", type);
        return subscriptionRepository.findByTypeSubOrderByStartDateAsc(type);
    }

    @Override
    public List<Subscription> retrieveSubscriptionsByDates(LocalDate startDate, LocalDate endDate) {
        log.info("Retrieving subscriptions between {} and {}", startDate, endDate);
        List<Subscription> result = subscriptionRepository.getSubscriptionsByStartDateBetween(startDate, endDate);
        log.debug("Found {} subscriptions in the given range", result.size());
        return result;
    }

    @Override
    @Scheduled(cron = "*/30 * * * * *")
    public void retrieveSubscriptions() {
        log.info("Running scheduled task: retrieveSubscriptions");
        List<Subscription> subscriptions = subscriptionRepository.findDistinctOrderByEndDateAsc();
        for (Subscription sub : subscriptions) {
            try {
                Skier aSkier = skierRepository.findBySubscription(sub);
                log.info("Subscription {} | End: {} | Skier: {} {}",
                        sub.getNumSub(), sub.getEndDate(), aSkier.getFirstName(), aSkier.getLastName());
            } catch (Exception e) {
                log.error("Error retrieving skier for subscription: {}", sub.getNumSub(), e);
            }
        }
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void showMonthlyRecurringRevenue() {
        log.info("Calculating Monthly Recurring Revenue (MRR)");
        try {
            Float monthlyRevenue = safeGetRevenue(TypeSubscription.MONTHLY);
            Float semestrielRevenue = safeGetRevenue(TypeSubscription.SEMESTRIEL) / 6;
            Float annualRevenue = safeGetRevenue(TypeSubscription.ANNUAL) / 12;

            Float totalRevenue = monthlyRevenue + semestrielRevenue + annualRevenue;
            log.info("Monthly Revenue = {}", totalRevenue);
        } catch (Exception e) {
            log.error("Error calculating monthly revenue", e);
        }
    }

    private Float safeGetRevenue(TypeSubscription type) {
        Float value = subscriptionRepository.recurringRevenueByTypeSubEquals(type);
        return (value != null) ? value : 0f;
    }

}
