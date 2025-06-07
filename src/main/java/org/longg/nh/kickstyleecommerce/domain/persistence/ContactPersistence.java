package org.longg.nh.kickstyleecommerce.domain.persistence;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.repository.IBaseRepository;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Contact;
import org.longg.nh.kickstyleecommerce.domain.repositories.ContactRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactPersistence implements IBasePersistence<Contact, Long> {

    private final ContactRepository contactRepository;

    @Override
    public IBaseRepository<Contact, Long> getIBaseRepository() {
        return contactRepository;
    }
} 