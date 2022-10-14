package club.dagomys.siteparcer.src.services;

import club.dagomys.lemmatisator.scr.LemmaCounter;
import club.dagomys.siteparcer.src.entity.Field;
import club.dagomys.siteparcer.src.entity.FieldSelector;
import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.repos.FieldRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.util.*;

@Service
public class FieldService {
    @Autowired
    private FieldRepository fieldRepository;
    private Logger mainLogger = LogManager.getLogger(FieldService.class);

    public List<Field> getAllFields() {
        List<Field> fields = new ArrayList<>();
        for (Field field : fieldRepository.findAll()) {
            fields.add(field);
        }
        return fields;
    }

    public Field saveField(Field field) {
        return fieldRepository.save(field);
    }

    public Field getFieldByName(FieldSelector selector) {
        return getAllFields().stream().filter(field -> field.getName().equalsIgnoreCase(selector.name())).findFirst().orElseThrow(NoSuchElementException::new);
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
            if (this.getAllFields().size() < 2) {
                this.saveField(title);
                this.saveField(body);
            } else {
                mainLogger.info("Данные уже добавлены ранее");
                fieldService.getAllFields().forEach(field -> mainLogger.info(field));
            }
        };
    }
}

