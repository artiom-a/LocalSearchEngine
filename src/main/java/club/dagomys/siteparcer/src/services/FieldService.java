package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.Field;
import club.dagomys.siteparcer.src.repos.FieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FieldService {
    @Autowired
    private FieldRepository fieldRepository;

    public Field saveField(Field field) {
        return fieldRepository.save(field);
    }
}
