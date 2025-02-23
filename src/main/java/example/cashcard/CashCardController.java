package example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {
    private final CashCardRepository cashCardRepository;

    private CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping
    private ResponseEntity<List<CashCard>> index(Pageable pageable, Principal principal) {
        Page<CashCard> page = cashCardRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSort()
                ));

        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> show(@PathVariable Long requestedId, Principal principal) {
        CashCard cashCard = findCashCard(requestedId, principal);

        if (cashCard == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(cashCard);

    }

    @PostMapping
    private ResponseEntity<Void> store(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb, Principal principal) {
        CashCard cashCardWithOwner = new CashCard(null, newCashCardRequest.amount(), principal.getName());
        CashCard savedCashCard = cashCardRepository.save(cashCardWithOwner);

        URI locationOfNewCashCard = ucb
                .path("cashcards/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();

        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> update(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate, Principal principal) {
        CashCard cashCard = findCashCard(requestedId, principal);

        if (cashCard == null) {
            return ResponseEntity.notFound().build();
        }

        CashCard updatedCashCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), principal.getName());

        cashCardRepository.save(updatedCashCard);

        return ResponseEntity.noContent().build();
    }

    private CashCard findCashCard(Long requestedId, Principal principal) {
        return cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
    }
}
