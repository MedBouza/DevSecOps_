package tn.esprit.spring;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tn.esprit.spring.entities.Skier;
import tn.esprit.spring.entities.Subscription;
import tn.esprit.spring.entities.TypeSubscription;
import tn.esprit.spring.repositories.ISkierRepository;
import tn.esprit.spring.repositories.ISubscriptionRepository;
import tn.esprit.spring.services.SubscriptionServicesImpl;

import javax.swing.*;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@ActiveProfiles("test")
public class SubscriptionServiceTest {
    @Autowired
    private SubscriptionServicesImpl subscriptionService;
    @Autowired
    private ISubscriptionRepository subscriptionRepository;
    @Autowired
    private ISkierRepository skierRepository;

    private Subscription subscription;
    private Skier skier;

    private final LocalDate today = LocalDate.now();

    @Test
    public void testAddSubscription() {
        // création d'une subscription de type ANNUAL avec une date de début
        Subscription sub = new Subscription();
        sub.setTypeSub(TypeSubscription.ANNUAL);
        sub.setStartDate(LocalDate.of(2024, 4, 1));
        sub.setPrice(99.99f); // exemple de prix

        // Ajout de la subscription
        Subscription savedSub = subscriptionService.addSubscription(sub);

        // Vérifier que l'ID a bien été généré (non null)
        assertNotNull(savedSub.getNumSub());

        // Vérifier que la date de fin est bien 1 an après la date de début
        LocalDate expectedEndDate = sub.getStartDate().plusYears(1);
        assertEquals(expectedEndDate, savedSub.getEndDate());

        // Vérifier que le type est bien ANNUAL
        assertEquals(TypeSubscription.ANNUAL, savedSub.getTypeSub());

        // Vérifier que le prix est bien conservé
        assertEquals(99.99f, savedSub.getPrice());

    }
    @Test
    public void testUpdateSubscription() {
        // Création d'un abonnement avec des valeurs de test sans utiliser le builder
        Subscription subscription = new Subscription();
        subscription.setNumSub(1001L);
        subscription.setTypeSub(TypeSubscription.MONTHLY);
        subscription.setStartDate(LocalDate.parse("2025-01-01"));
        subscription.setEndDate(LocalDate.parse("2025-12-31"));
        subscription.setPrice(99.99F);

        // Ajout ou mise à jour de l'abonnement
        Subscription updatedSub = subscriptionService.updateSubscription(subscription);

        // Vérifier que l'abonnement a bien été ajouté ou mis à jour (id non null)
        Assertions.assertNotNull(updatedSub.getNumSub());

        // Vérifier que le type est bien enregistré
        Assertions.assertEquals(TypeSubscription.MONTHLY, updatedSub.getTypeSub());

        // Vérifier que les dates sont cohérentes
        Assertions.assertTrue(updatedSub.getEndDate().isAfter(updatedSub.getStartDate()));

        // Vérifier que le montant est positif
        Assertions.assertTrue(updatedSub.getPrice() > 0);

        // Nettoyer la base de données si nécessaire
    }
    @Test
    public void testRetrieveSubscriptionById() {
        // Création et ajout d'une subscription
        Subscription sub = new Subscription();
        sub.setTypeSub(TypeSubscription.ANNUAL);
        sub.setStartDate(LocalDate.of(2024, 4, 1));
        sub.setPrice(99.99f); // exemple de prix

        // Ajout de l'abonnement et récupération de l'ID généré
        Subscription savedSub = subscriptionService.addSubscription(sub);
        Long subscriptionId = savedSub.getNumSub();

        // Récupération de l'abonnement par ID
        Subscription retrievedSub = subscriptionService.retrieveSubscriptionById(subscriptionId);

        // Vérifier que l'abonnement récupéré n'est pas null
        assertNotNull(retrievedSub);

        // Vérifier que l'ID de l'abonnement récupéré est bien le même que l'ID initial
        assertEquals(subscriptionId, retrievedSub.getNumSub());

        // Vérifier que le type, la date de début et le prix sont les mêmes que ceux de l'abonnement ajouté
        assertEquals(TypeSubscription.ANNUAL, retrievedSub.getTypeSub());
        assertEquals(LocalDate.of(2024, 4, 1), retrievedSub.getStartDate());
        assertEquals(99.99f, retrievedSub.getPrice());
    }

    @Test
    public void testRetrieveSubscriptionByIdNotFound() {
        // Tentative de récupération d'un abonnement avec un ID inexistant
        Subscription retrievedSub = subscriptionService.retrieveSubscriptionById(9999L);

        // Vérifier que l'abonnement retourné est null
        assertNull(retrievedSub);
    }

    @Test
    public void testGetSubscriptionByType() {
        // Création de plusieurs abonnements de type MONTHLY
        Subscription sub1 = new Subscription();
        sub1.setTypeSub(TypeSubscription.MONTHLY);
        sub1.setStartDate(LocalDate.of(2024, 1, 1));
        sub1.setPrice(50f);
        subscriptionService.addSubscription(sub1);

        Subscription sub2 = new Subscription();
        sub2.setTypeSub(TypeSubscription.MONTHLY);
        sub2.setStartDate(LocalDate.of(2024, 3, 1));
        sub2.setPrice(60f);
        subscriptionService.addSubscription(sub2);

        Subscription sub3 = new Subscription();
        sub3.setTypeSub(TypeSubscription.ANNUAL);
        sub3.setStartDate(LocalDate.of(2024, 2, 1));
        sub3.setPrice(120f);
        subscriptionService.addSubscription(sub3);

        // Appel de la méthode à tester
        Set<Subscription> result = subscriptionService.getSubscriptionByType(TypeSubscription.MONTHLY);

        // Vérifier que seuls les abonnements de type MONTHLY sont retournés
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> s.getTypeSub() == TypeSubscription.MONTHLY));

        // Vérifier l'ordre des dates (sub1 doit précéder sub2)
        List<Subscription> resultList = new ArrayList<>(result);
        assertTrue(resultList.get(0).getStartDate().isBefore(resultList.get(1).getStartDate()));
    }
    @BeforeEach
    public void clearDatabase() {
        subscriptionRepository.deleteAll();
    }
    @Test
    void testRetrieveSubscriptionsByDates() {
        // Nettoyer les données existantes
        subscriptionRepository.deleteAll();

        LocalDate today = LocalDate.now();

        // Créer une souscription dans la plage de dates
        Subscription subscription = new Subscription();
        subscription.setStartDate(today);
        subscription.setTypeSub(TypeSubscription.MONTHLY); // Remplace selon ton enum
        subscription.setPrice(20.0f); // champs obligatoires selon ton modèle

        subscriptionRepository.save(subscription);

        // Définir la plage de dates (ex: de 5 jours avant aujourd’hui à aujourd’hui)
        LocalDate startDate = today.minusDays(5);
        LocalDate endDate = today;

        List<Subscription> result = subscriptionService.retrieveSubscriptionsByDates(startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(!result.get(0).getStartDate().isBefore(startDate) && !result.get(0).getStartDate().isAfter(endDate));
    }


}
