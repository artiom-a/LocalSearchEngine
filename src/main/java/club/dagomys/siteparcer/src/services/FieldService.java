package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.Field;
import club.dagomys.siteparcer.src.entity.FieldSelector;
import club.dagomys.siteparcer.src.repos.FieldRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FieldService {
    @Autowired
    private FieldRepository fieldRepository;
    private final Logger mainLogger = LogManager.getLogger(FieldService.class);

    public List<Field> getAllFields() {
        return new ArrayList<>(fieldRepository.findAll());
    }
    @Async("taskExecutor")
    public Field saveField(Field field) {
        return fieldRepository.save(field);
    }

    public Field getFieldByName(FieldSelector selector) {
        return fieldRepository.findAll().stream().filter(field -> field.getName().equalsIgnoreCase(selector.name())).findFirst().orElseThrow(NoSuchElementException::new);
    }

    /**
     * @param fieldService
     * @return Добавляет 2 статические записи для полей на страницах сайтов со значениями по умолчанию
     */
    @Bean
    public CommandLineRunner run(FieldService fieldService) throws Exception {
        return (String[] args) -> {
            Field title = new Field("title", FieldSelector.TITLE, 1f);
            Field body = new Field("body", FieldSelector.BODY, 0.8f);
            if (this.fieldRepository.findAll().size() < 2) {
                this.fieldRepository.save(title);
                this.fieldRepository.save(body);
            } else {
                mainLogger.info("Данные уже добавлены ранее");
                fieldRepository.findAll().forEach(mainLogger::info);
            }
        };
    }
}

